/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.agent;

import org.apache.camel.builder.RouteBuilder;
import org.openremote.agent.protocol.ProtocolAssetService;
import org.openremote.model.Container;
import org.openremote.container.message.MessageBrokerService;
import org.openremote.container.persistence.PersistenceEvent;
import org.openremote.container.timer.TimerService;
import org.openremote.manager.asset.*;
import org.openremote.manager.event.ClientEventService;
import org.openremote.manager.gateway.GatewayService;
import org.openremote.manager.security.ManagerIdentityService;
import org.openremote.manager.web.ManagerWebService;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetType;
import org.openremote.model.asset.agent.*;
import org.openremote.model.attribute.*;
import org.openremote.model.attribute.AttributeEvent.Source;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.LogicGroup;
import org.openremote.model.query.filter.*;
import org.openremote.model.query.filter.RefPredicate;
import org.openremote.model.util.Pair;
import org.openremote.model.util.TextUtil;
import org.openremote.model.v2.MetaType;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openremote.container.concurrent.GlobalLock.withLock;
import static org.openremote.container.concurrent.GlobalLock.withLockReturning;
import static org.openremote.container.persistence.PersistenceEvent.*;
import static org.openremote.manager.asset.AssetProcessingService.ASSET_QUEUE;
import static org.openremote.model.AbstractValueTimestampHolder.VALUE_TIMESTAMP_FIELD_NAME;
import static org.openremote.model.asset.AssetType.AGENT;
import static org.openremote.model.asset.agent.AgentLink.getAgentLink;
import static org.openremote.model.asset.agent.ConnectionStatus.*;
import static org.openremote.model.asset.agent.Protocol.ACTUATOR_TOPIC;
import static org.openremote.model.asset.agent.Protocol.SENSOR_QUEUE;
import static org.openremote.model.attribute.Attribute.attributesFromJson;
import static org.openremote.model.attribute.Attribute.getAddedOrModifiedAttributes;
import static org.openremote.model.attribute.AttributeEvent.HEADER_SOURCE;
import static org.openremote.model.attribute.AttributeEvent.Source.GATEWAY;
import static org.openremote.model.attribute.AttributeEvent.Source.SENSOR;
import static org.openremote.model.util.TextUtil.isValidURN;

/**
 * Handles life cycle and communication with {@link Protocol}s.
 * <p>
 * Finds all {@link Agent} assets and manages their {@link Protocol} instances.
 */
public class AgentService extends RouteBuilder implements ContainerService, AssetUpdateProcessor, ProtocolAssetService {

    private static final Logger LOG = Logger.getLogger(AgentService.class.getName());
    public static final int PRIORITY = DEFAULT_PRIORITY + 100; // Start quite late to ensure asset model etc. are initialised
    protected TimerService timerService;
    protected ManagerIdentityService identityService;
    protected AssetProcessingService assetProcessingService;
    protected AssetStorageService assetStorageService;
    protected MessageBrokerService messageBrokerService;
    protected ClientEventService clientEventService;
    protected GatewayService gatewayService;
    protected Map<String, Agent> agentMap = new HashMap<>();
    protected final Map<String, Protocol> protocolInstanceMap = new HashMap<>();
    protected final Map<String, List<Consumer<PersistenceEvent<Asset>>>> childAssetSubscriptions = new HashMap<>();
    protected final Map<String, List<Attribute<?>>> linkedAttributes = new HashMap<>();
    protected boolean initDone;
    protected Container container;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void init(Container container) throws Exception {
        this.container = container;
        timerService = container.getService(TimerService.class);
        identityService = container.getService(ManagerIdentityService.class);
        assetProcessingService = container.getService(AssetProcessingService.class);
        assetStorageService = container.getService(AssetStorageService.class);
        messageBrokerService = container.getService(MessageBrokerService.class);
        clientEventService = container.getService(ClientEventService.class);
        gatewayService = container.getService(GatewayService.class);

        if (initDone) {
            return;
        }

        container.getService(ManagerWebService.class).getApiSingletons().add(
            new AgentResourceImpl(
                container.getService(TimerService.class),
                container.getService(ManagerIdentityService.class),
                assetStorageService,
                this)
        );

        initDone = true;
    }

    @Override
    public void start(Container container) throws Exception {
        container.getService(MessageBrokerService.class).getContext().addRoutes(this);

        // Load all enabled agents and instantiate a protocol instance for each
        LOG.fine("Loading agents...");
        Collection<Agent> agents = getAgents().values();
        LOG.fine("Found enabled agent count = " + agents.size());

        agents.forEach(agent -> {
            startAgent(agent);
        });

        /// For all agents, go through their protocol configurations and find
        // assets that are linked to them, to create the binding on startup
        for (Asset agent : agents) {
            linkProtocolConfigurations(agent, agent.getAttributesStream()
                .filter(ProtocolConfiguration::isProtocolConfiguration)
                .collect(Collectors.toList())
            );
        }
    }

    @Override
    public void stop(Container container) throws Exception {
        agentMap.values().forEach(agent ->
            stopAgent(
                agent,
                agent.getAttributesStream()
                    .filter(ProtocolConfiguration::isProtocolConfiguration)
                    .collect(Collectors.toList())));
        agentMap.clear();
        protocolInstanceMap.clear();
    }

    @Override
    public void configure() throws Exception {
        from(PERSISTENCE_TOPIC)
            .routeId("AgentPersistenceChanges")
            .filter(isPersistenceEventForEntityType(Asset.class))
            .process(exchange -> {
                @SuppressWarnings("unchecked")
                PersistenceEvent<Asset> persistenceEvent = (PersistenceEvent<Asset>)exchange.getIn().getBody(PersistenceEvent.class);
                Asset asset = persistenceEvent.getEntity();
                if (isPersistenceEventForAssetType(AGENT).matches(exchange)) {
                    processAgentChange(asset, persistenceEvent);
                } else {
                    processAssetChange(asset, persistenceEvent);
                }
            });

        // A protocol wants to write a new sensor value
        from(SENSOR_QUEUE)
            .routeId("FromSensorUpdates")
            .filter(body().isInstanceOf(AttributeEvent.class))
            .setHeader(HEADER_SOURCE, () -> SENSOR)
            .to(ASSET_QUEUE);
    }

    @Override
    public Asset mergeAsset(Asset asset) {
        Objects.requireNonNull(asset.getId());
        Objects.requireNonNull(asset.getParentId());

        // Do basic check that parent is at least an agent...doesn't confirm its' the correct agent so
        // that's up to the protocol to guarantee
        if (!getAgents().containsKey(asset.getParentId())) {
            String msg = "Cannot merge protocol-provided asset as the parent ID is not a valid agent ID: " + asset;
            LOG.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        // TODO: Define access permissions for merged asset (user asset links inherit from parent agent?)
        LOG.fine("Merging asset with protocol-provided: " + asset);
        return assetStorageService.merge(asset, true);
    }

    @Override
    public boolean deleteAsset(String assetId) {
        LOG.fine("Deleting protocol-provided: " + assetId);
        return assetStorageService.delete(Collections.singletonList(assetId), false);
    }

    @Override
    public Asset findAsset(String assetId) {
        LOG.fine("Getting protocol-provided: " + assetId);
        return assetStorageService.find(assetId);
    }

    @Override
    public <T extends Asset> T findAsset(String assetId, Class<T> assetType) {
        LOG.fine("Getting protocol-provided: " + assetId);
        return assetStorageService.find(assetId, assetType);
    }

    @Override
    public List<Asset> findAssets(String assetId, AssetQuery assetQuery) {
        if (TextUtil.isNullOrEmpty(assetId) || assetQuery == null) {
            return Collections.emptyList();
        }

        // Ensure agent ID is injected into each path predicate
        if (assetQuery.paths != null) {
            for (PathPredicate pathPredicate : assetQuery.paths) {
                int len = pathPredicate.path.length;
                pathPredicate.path = Arrays.copyOf(pathPredicate.path, len+1);
                pathPredicate.path[len] = assetId;
            }
        } else {
            assetQuery.paths(new PathPredicate(assetId));
        }

        return assetStorageService.findAll(assetQuery);
    }

    @Override
    public void sendAttributeEvent(AttributeEvent attributeEvent) {
        assetProcessingService.sendAttributeEvent(attributeEvent);
    }

    /**
     * Looks for new, modified and obsolete protocol configurations and links / unlinks any associated attributes
     */
    protected void processAgentChange(Asset agent, PersistenceEvent<?> persistenceEvent) {

        LOG.finest("Processing agent persistence event: " + persistenceEvent.getCause());

        switch (persistenceEvent.getCause()) {
            case CREATE:
                if (!addReplaceAgent(agent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }
                linkProtocolConfigurations(
                    agent,
                    agent.getAttributesStream()
                        .filter(ProtocolConfiguration::isProtocolConfiguration)
                        .collect(Collectors.toList())
                );
                break;
            case UPDATE:
                if (!addReplaceAgent(agent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }
                // Check if any protocol config attributes have been added/removed or modified
                int attributesIndex = Arrays.asList(persistenceEvent.getPropertyNames()).indexOf("attributes");
                if (attributesIndex < 0) {
                    return;
                }

                // Attributes have possibly changed so need to compare old and new state to determine
                // which protocol configs are affected
                List<Attribute> oldProtocolConfigurations =
                    attributesFromJson(
                        (ObjectValue) persistenceEvent.getPreviousState()[attributesIndex],
                        agent.getId()
                    )
                        .filter(ProtocolConfiguration::isProtocolConfiguration)
                        .collect(Collectors.toList());

                List<Attribute> newProtocolConfigurations =
                    attributesFromJson(
                        (ObjectValue) persistenceEvent.getCurrentState()[attributesIndex],
                        agent.getId()
                    )
                        .filter(ProtocolConfiguration::isProtocolConfiguration)
                        .collect(Collectors.toList());

                // Compare protocol configurations by JSON value
                // Unlink protocols that are in oldConfigs but not in newConfigs
                stopAgent(agent, oldProtocolConfigurations
                    .stream()
                    .filter(oldProtocolAttribute -> newProtocolConfigurations
                        .stream()
                        .noneMatch(oldProtocolAttribute::equals)
                    )
                    .collect(Collectors.toList())
                );

                // Link protocols that are in newConfigs but not in oldConfigs
                linkProtocolConfigurations(agent, newProtocolConfigurations
                    .stream()
                    .filter(newProtocolAttribute -> oldProtocolConfigurations
                        .stream()
                        .noneMatch(newProtocolAttribute::equals)
                    )
                    .collect(Collectors.toList())
                );

                break;
            case DELETE:
                if (!removeAgent(agent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }
                // Unlink any attributes that have an agent link to this agent
                stopAgent(agent, agent.getAttributesStream()
                    .filter(ProtocolConfiguration::isProtocolConfiguration)
                    .collect(Collectors.toList())
                );
                break;
        }
    }

    /**
     * Looks for new, modified and obsolete AGENT_LINK attributes and links / unlinks them
     * with the protocol
     */
    protected void processAssetChange(Asset asset, PersistenceEvent<Asset> persistenceEvent) {
        LOG.finest("Processing asset persistence event: " + persistenceEvent.getCause());

        switch (persistenceEvent.getCause()) {
            case CREATE:

                // Check if asset parent is a gateway or a gateway descendant, if so ignore it
                // Need to look at parent as this asset may not have been acknowledged by the gateway service yet
                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), asset.getParentId()) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                // Asset insert persistence events can be fired before the agent insert persistence event
                // so need to check that all protocol configs exist - any that don't we will exclude here
                // and handle in agent insert

                // If an agent insert just occurred then we will end up trying to link the attribute again
                // so we keep track of linked attributes to avoid this

                // Link any AGENT_LINK attributes to their referenced agent asset
                Map<Agent, List<Attribute>> groupedAgentLinksAttributes =
                    getGroupedAgentLinkAttributes(
                        asset.getAttributesStream(),
                        attribute -> true,
                        attribute -> LOG.warning("Linked protocol configuration not found: " + attribute)
                    );
                groupedAgentLinksAttributes.forEach((agent, attributes) -> this.linkAttributes(agent, asset, attributes));

                break;
            case UPDATE:

                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                List<String> propertyNames = Arrays.asList(persistenceEvent.getPropertyNames());

                // Check if attributes of the asset have been modified
                int attributesIndex = propertyNames.indexOf("attributes");
                if (attributesIndex < 0) {
                    return;
                }

                // Attributes have possibly changed so need to compare old and new state to determine any changes to
                // AGENT_LINK attributes
                List<Attribute> oldAgentLinkedAttributes =
                    attributesFromJson(
                        (ObjectValue) persistenceEvent.getPreviousState()[attributesIndex],
                        asset.getId()
                    )
                        .filter(assetAttribute ->
                            // Exclude attributes without agent link or with agent link to not recognised agents (could be gateway agents)
                            assetAttribute.getMetaItem(MetaItemType.AGENT_LINK)
                                .flatMap(agentLinkMetaItem -> AttributeRef.fromValue(agentLinkMetaItem.getValue().orElse(null)))
                                .map(agentLinkRef -> getAgents().containsKey(agentLinkRef.getEntityId()))
                                .orElse(false))
                        .collect(Collectors.toList());

                List<Attribute> newAgentLinkedAttributes =
                    attributesFromJson(
                        (ObjectValue) persistenceEvent.getCurrentState()[attributesIndex],
                        asset.getId())
                        .filter(assetAttribute ->
                            // Exclude attributes without agent link or with agent link to not recognised agents (could be gateway agents)
                            assetAttribute.getMetaItem(MetaItemType.AGENT_LINK)
                                .flatMap(agentLinkMetaItem -> AttributeRef.fromValue(agentLinkMetaItem.getValue().orElse(null)))
                                .map(agentLinkRef -> getAgents().containsKey(agentLinkRef.getEntityId()))
                                .orElse(false))
                        .collect(Collectors.toList());

                // Unlink thing attributes that are in old but not in new
                getGroupedAgentLinkAttributes(
                    getAddedOrModifiedAttributes(newAgentLinkedAttributes, oldAgentLinkedAttributes, key -> key.equals(VALUE_TIMESTAMP_FIELD_NAME)),
                    attribute -> true
                ).forEach(this::unlinkAttributes);

                // Link thing attributes that are in new but not in old
                getGroupedAgentLinkAttributes(
                    getAddedOrModifiedAttributes(oldAgentLinkedAttributes, newAgentLinkedAttributes, key -> key.equals(VALUE_TIMESTAMP_FIELD_NAME)),
                    attribute -> true,
                    attribute -> LOG.warning("Linked protocol configuration not found: " + attribute)
                ).forEach(this::linkAttributes);

                break;
            case DELETE: {

                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                // Unlink any AGENT_LINK attributes from the referenced protocol
                Map<Attribute, List<Attribute>> groupedAgentLinkAndProtocolAttributes =
                    getGroupedAgentLinkAttributes(asset.getAttributesStream(), attribute -> true);
                groupedAgentLinkAndProtocolAttributes
                    .forEach(
                        this::unlinkAttributes
                    );
                break;
            }
        }

        String parentAgentId = getAgentAncestorId(asset);
        if (parentAgentId != null) {
            notifyChildAssetChange(parentAgentId, persistenceEvent);
        }
    }

    protected String getAgentAncestorId(Asset asset) {
        if (asset.getPath() == null) {
            // Fully load
            Asset fullyLoaded = assetStorageService.find(asset.getId());
            if (fullyLoaded != null) {
                asset = fullyLoaded;
            } else if (!TextUtil.isNullOrEmpty(asset.getParentId())) {
                fullyLoaded = assetStorageService.find(asset.getParentId());
                List<String> path = new ArrayList<>(Arrays.asList(fullyLoaded.getPath()));
                path.add(0, asset.getId());
                asset.setPath(path.toArray(new String[0]));
            }
        }

        if (asset.getPath() == null) {
            return null;
        }

        return Arrays.stream(asset.getPath())
                .filter(assetId -> getAgents().containsKey(assetId))
                .findFirst()
                .orElse(null);
    }

    protected void startAgent(Agent agent) {
        Protocol protocol = agent.getProtocolInstance();
        protocolInstanceMap.put(agent.getId(), protocol);
        try {
            LOG.fine("Starting protocol instance: " + protocol);
            protocol.start(container);
            LOG.fine("Started protocol instance:" + protocol);

            LOG.fine("Linking attributes to protocol instance: " + protocol);

            // Get all assets that have attributes with agent link meta to this agent
            List<Asset> assets = assetStorageService.findAll(
                new AssetQuery()
                    .attributes(
                        new AttributePredicate().meta(
                            new NameValuePredicate(new StringPredicate(MetaType.AGENT_LINK.getName()), new StringPredicate(agent.getId()))
                        )
                    )
            );

            LOG.fine("Found '" + assets.size() + "' asset(s) with attributes linked to this protocol instance: " + protocol);

            assets.forEach(
                asset ->
                    getGroupedAgentLinkAttributes(
                        asset.getAttributesStream(),
                        assetAttribute -> getAgentLink(assetAttribute)
                            .map(attributeRef -> attributeRef.equals(protocolAttributeRef))
                            .orElse(false),
                        attribute -> LOG.warning("Linked protocol configuration not found: " + attribute)
                    ).forEach(this::linkAttributes)
            );


        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start protocol instance: " + protocol);
        }
    }

    protected void stopAgent(Asset agent) {
        withLock(getClass().getSimpleName() + "::unlinkProtocolConfigurations", () -> configurations.forEach(configuration -> {
            AttributeRef protocolAttributeRef = configuration.getReferenceOrThrow();

            // Unlink all linked attributes for this protocol configuration
            List<Attribute> protocolLinkedAttributes = linkedAttributes.remove(protocolAttributeRef);

            if (protocolLinkedAttributes != null && !protocolLinkedAttributes.isEmpty()) {
                unlinkAttributes(configuration, protocolLinkedAttributes);
            }

            Protocol protocol = getProtocol(configuration);

            // Unlink the protocol configuration from the protocol
            try {
                protocol.disconnect();
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Protocol threw an exception during protocol configuration unlinking", e);
            }

            protocolConfigurations.remove(protocolAttributeRef);

            // Check if there are any remaining configs for the agent
            String agentId = configurations.get(0).getReferenceOrThrow().getEntityId();
            if (protocolConfigurations.keySet().stream().noneMatch(protocolConfigRef -> protocolConfigRef.getEntityId().equals(agentId))) {
                childAssetSubscriptions.remove(agentId);
            }
        }));
    }

    public ConnectionStatus getProtocolConnectionStatus(AttributeRef protocolRef) {
        return withLockReturning(getClass().getSimpleName() + "::getProtocolConnectionStatus", () ->
            Optional.ofNullable(protocolConfigurations.get(protocolRef))
                .map(pair -> pair.value)
                .orElse(null));
    }

    protected void linkAttributes(Agent agent, Asset asset, Collection<Attribute> attributes) {
        withLock(getClass().getSimpleName() + "::linkAttributes", () -> {
            LOG.fine("Linking all attributes that use protocol attribute: " + protocolConfiguration);
            Protocol protocol = getProtocol(protocolConfiguration);

            if (protocol == null) {
                LOG.severe("Cannot link protocol attributes as protocol is null: " + protocolConfiguration);
                return;
            }

            this.linkedAttributes.compute(
                protocolConfiguration.getReferenceOrThrow(),
                (protocolRef, linkedAttrs) -> {
                    if (linkedAttrs == null) {
                        linkedAttrs = new ArrayList<>(attributes.size());
                    }
                    linkedAttrs.addAll(attributes);
                    return linkedAttrs;
                });

            try {
                LOG.finest("Linking protocol attributes to: " + protocol.getProtocolName());
                protocol.linkAttribute(, attributes);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Ignoring error on linking attributes to protocol: " + protocol.getProtocolName(), ex);
                // Update the status of this protocol configuration to error
                publishProtocolConnectionStatus(protocolConfiguration.getReferenceOrThrow(), ERROR);
            }
        });
    }

    protected void unlinkAttributes(Agent agent, Asset asset, Collection<Attribute> attributes) {
        withLock(getClass().getSimpleName() + "::unlinkAttributes", () -> {
            LOG.fine("Unlinking attributes that use protocol attribute: " + protocolConfiguration);
            Protocol protocol = getProtocol(protocolConfiguration);

            if (protocol == null) {
                LOG.severe("Cannot unlink protocol attributes as protocol is null: " + protocolConfiguration);
                return;
            }

            linkedAttributes.computeIfPresent(
                protocolConfiguration.getReferenceOrThrow(),
                (protocolRef, linkedAttrs) -> {
                    linkedAttrs.removeIf(attr -> attributes.stream().anyMatch(a -> a.getReferenceOrThrow().equals(attr.getReferenceOrThrow())));
                    return linkedAttrs.isEmpty() ? null : linkedAttrs;
                }
            );

            try {
                LOG.finest("Unlinking protocol attributes from: " + protocol.getProtocolName());
                protocol.unlinkAttribute(, attributes);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Ignoring error on unlinking attributes from protocol: " + protocol.getProtocolName(), ex);
                // Update the status of this protocol configuration to error
                publishProtocolConnectionStatus(protocolConfiguration.getReferenceOrThrow(), ERROR);
            }
        });
    }

    /**
     * If this is an update from a sensor, or if the changed attribute is not linked to an agent's protocol
     * configuration, it's ignored.
     * <p>
     * Otherwise push the update to the attributes' linked protocol to handle and prevent any further
     * processing of this event by the processing chain. The protocol should raise sensor updates as
     * required (i.e. the protocol is responsible for synchronising state with the database).
     */
    @Override
    public boolean processAssetUpdate(EntityManager entityManager,
                                      Asset asset,
                                      Attribute attribute,
                                      Source source) throws AssetProcessingException {

        if (source == SENSOR || source == GATEWAY) {
            return false;
        }

        Boolean result = withLockReturning(getClass().getSimpleName() + "::processAssetUpdate", () ->
            AgentLink.getAgentLink(attribute)
                .map(ref ->
                    getProtocolConfiguration(ref)
                        .orElseThrow(() -> new AssetProcessingException(AssetProcessingException.Reason.INVALID_AGENT_LINK))
                )
                .map(protocolConfiguration -> {
                    // Its' a send to actuator - push the update to the protocol
                    AttributeEvent attributeEvent = new AttributeEvent(new AttributeState(asset.getId(), attribute));
                    LOG.fine("Sending to actuator topic: " + attributeEvent);
                    messageBrokerService.getProducerTemplate().sendBodyAndHeader(
                        ACTUATOR_TOPIC,
                        attributeEvent,
                        Protocol.ACTUATOR_TOPIC_TARGET_PROTOCOL,
                        protocolConfiguration.getValueAsString().orElse("")
                    );
                    return true; // Processing complete, skip other processors
                })
                .orElse(false) // This is a regular attribute so allow the processing to continue
        );
        return result != null ? result : false;
    }

    /**
     * Gets all agent link attributes and their linked protocol configuration and groups them by Protocol Configuration
     */
    protected Map<Attribute, List<Attribute>> getGroupedAgentLinkAttributes(Stream<Attribute> attributes,
                                                                                      Predicate<Attribute> filter) {

        return getGroupedAgentLinkAttributes(attributes, filter, null);
    }

    protected Map<Agent, List<Attribute<?>>> getGroupedAgentLinkAttributes(Stream<Attribute<?>> attributes,
                                                                                      Predicate<Attribute<?>> filter,
                                                                                      Consumer<Attribute<?>> notFoundConsumer) {
        Map<Attribute, List<Attribute>> result = new HashMap<>();
        attributes
            .filter(assetAttribute ->
                // Exclude attributes without agent link or with agent link to not recognised agents (could be gateway agents)
                assetAttribute.getMetaItem(MetaItemType.AGENT_LINK)
                    .flatMap(agentLinkMetaItem -> AttributeRef.fromValue(agentLinkMetaItem.getValue().orElse(null)))
                    .map(agentLinkRef -> getAgents().containsKey(agentLinkRef.getEntityId()))
                    .orElse(false))
            .filter(filter)
            .map(attribute -> new Pair<>(attribute, getAgentLink(attribute)))
            .filter(pair -> pair.value.isPresent())
            .map(pair -> new Pair<>(pair.key, getProtocolConfiguration(pair.value.get())))
            .filter(pair -> {
                if (pair.value.isPresent()) {
                    return true;
                } else if (notFoundConsumer != null) {
                    notFoundConsumer.accept(pair.key);
                }
                return false;
            })
            .forEach(pair -> result.computeIfAbsent(pair.value.get(), newProtocolConfiguration -> new ArrayList<>())
                .add(pair.key)
            );
        return result;
    }

    public String toString() {
        return getClass().getSimpleName() + "{" +
            '}';
    }

    public Optional<Attribute> getProtocolConfiguration(AttributeRef protocolRef) {
        return withLockReturning(getClass().getSimpleName() + "::getProtocolConfiguration", () -> {
            Pair<Attribute, ConnectionStatus> deploymentStatusPair = protocolConfigurations.get(protocolRef);
            return deploymentStatusPair == null ? Optional.empty() : Optional.of(deploymentStatusPair.key);
        });
    }

    public Optional<AgentConnector> getAgentConnector(Asset agent) {
        if (agent == null || agent.getWellKnownType() != AGENT) {
            return Optional.empty();
        }

        return Optional.of(localAgentConnector);
    }

    protected boolean addReplaceAgent(Asset agent) {
        // Fully load agent asset
        final Asset loadedAgent = assetStorageService.find(agent.getId(), true);
        if (gatewayService.getLocallyRegisteredGatewayId(agent.getId(), agent.getParentId()) != null) {
            return false;
        }
        withLock(getClass().getSimpleName() + "::addReplaceAgent", () -> getAgents().put(loadedAgent.getId(), loadedAgent));
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    protected boolean removeAgent(Asset agent) {
        return withLockReturning(getClass().getSimpleName() + "::removeAgent", () -> getAgents().remove(agent.getId()) != null);
    }

    public Map<String, Agent> getAgents() {
        return withLockReturning(getClass().getSimpleName() + "::getAgents", () -> {
            if (agentMap == null) {
                agentMap = assetStorageService.findAll(
                        new AssetQuery().types(Agent.class).attributes(
                            new LogicGroup<>(
                                LogicGroup.Operator.OR,
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName())).mustNotExist(),
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName()), new ValueEmptyPredicate()),
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName()), new BooleanPredicate(false))
                            )
                        )
                    )
                    .stream()
                    .filter(asset -> gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) == null)
                    .collect(Collectors.toMap(Asset::getId, agent -> (Agent)agent));
            }
            return agentMap;
        });
    }

    @Override
    public void subscribeChildAssetChange(String agentId, Consumer<PersistenceEvent<Asset>> assetChangeConsumer) {
        if (protocolConfigurations.keySet().stream().noneMatch(attributeRef -> attributeRef.getEntityId().equals(agentId))) {
            LOG.info("Attempt to subscribe to child asset changes with an invalid agent ID: " +agentId);
            return;
        }

        withLock(getClass().getSimpleName() + "::subscribeChildAssetChange", () -> {
            List<Consumer<PersistenceEvent<Asset>>> consumerList = childAssetSubscriptions
                .computeIfAbsent(agentId, (id) -> new ArrayList<>());
            if (!consumerList.contains(assetChangeConsumer)) {
                consumerList.add(assetChangeConsumer);
            }
        });
    }

    @Override
    public void unsubscribeChildAssetChange(String agentId, Consumer<PersistenceEvent<Asset>> assetChangeConsumer) {
        withLock(getClass().getSimpleName() + "::unsubscribeChildAssetChange", () ->
            childAssetSubscriptions.computeIfPresent(agentId, (id, consumerList) -> {
                consumerList.remove(assetChangeConsumer);
                return consumerList.isEmpty() ? null : consumerList;
            }));
    }

    protected void notifyChildAssetChange(String agentId, PersistenceEvent<Asset> assetPersistenceEvent) {
        withLock(getClass().getSimpleName() + "::notifyChildAssetChange", () ->
            childAssetSubscriptions.computeIfPresent(agentId, (id, consumerList) -> {
                LOG.fine("Notifying child asset change consumers of change to agent child asset: Agent ID=" + id + ", Asset ID=" + assetPersistenceEvent.getEntity().getId());
                try {
                    consumerList.forEach(consumer -> consumer.accept(assetPersistenceEvent));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Child asset change consumer threw an exception: Agent ID=" + id + ", Asset ID=" + assetPersistenceEvent.getEntity().getId(), e);
                }
                return consumerList;
            }));
    }
}
