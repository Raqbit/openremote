package org.openremote.setup;

import org.openremote.agent.protocol.tcp.TcpClientProtocol;
import org.openremote.container.Container;
import org.openremote.manager.setup.AbstractManagerSetup;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetAttribute;
import org.openremote.model.asset.AssetType;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.filter.StringPredicate;
import org.openremote.model.value.Values;

import java.util.logging.Logger;

import static org.openremote.model.Constants.MASTER_REALM;
import static org.openremote.model.asset.agent.ProtocolConfiguration.initProtocolConfiguration;
import static org.openremote.model.attribute.AttributeValueType.STRING;
import static org.openremote.model.attribute.MetaItemType.AGENT_LINK;

/**
 * Here we create assets and protocols that we want; currently this creates a {@link TcpClientProtocol} instance that
 * tries to connect to a TCP server at tcp://localhost:12345. A TCP server should be implemented that binds to this
 * address and the server should send simple String messages (UTF-8 encoded) - see
 * {@link org.openremote.agent.protocol.tcp.TcpStringServer} for an example that is used in unit testing.
 * <p>
 * A thousand {@link Asset}s are also created that link to this protocol instance, each asset has a single attribute
 * that links to the protocol instance and is configured to expect a TCP packet in the format  "N:M;" where:
 * <ul>
 * <li>N = Asset number (each asset has been numbered (i.e. 1,2,3....N)</li>
 * <li>M = Value to push into the attribute (any string value - could be rotated in load testing to allow easy
 * <li>; = Delimiter to indicate the end of a message frame</li>
 * identification of value changes)</li>
 * </ul>
 */
public class LoadTestManagerSetup extends AbstractManagerSetup {

    private static final Logger LOG = Logger.getLogger(LoadTestManagerSetup.class.getName());

    public LoadTestManagerSetup(Container container) {
        super(container);
    }

    @Override
    public void onStart() throws Exception {
        super.onStart();

        Asset tcpAgent = new Asset("TCP Agent", AssetType.AGENT);
        tcpAgent.setRealm(MASTER_REALM);
        tcpAgent.addAttributes(
            initProtocolConfiguration(new AssetAttribute("protocolConfig"), TcpClientProtocol.PROTOCOL_NAME)
                .addMeta(
                    new MetaItem(TcpClientProtocol.META_PROTOCOL_HOST, Values.create("localhost")),
                    new MetaItem(TcpClientProtocol.META_PROTOCOL_PORT, Values.create(12345)),
                    new MetaItem(TcpClientProtocol.META_PROTOCOL_DELIMITER, Values.create(";")),
                    new MetaItem(TcpClientProtocol.META_PROTOCOL_STRIP_DELIMITER, Values.create(true))
                )
        );
        tcpAgent = assetStorageService.merge(tcpAgent);

        // Create the test assets and link to the TCP agent
        for (int i = 1; i <= 1000; i++) {
            Asset thingAsset = new Asset("TCP Asset " + i, AssetType.THING, null, MASTER_REALM)
                .addAttributes(
                    new AssetAttribute("tcpAttribute", STRING)
                        .addMeta(
                            new MetaItem(AGENT_LINK, new AttributeRef(tcpAgent.getId(), "protocolConfig").toArrayValue()),
                            new MetaItem(TcpClientProtocol.META_ATTRIBUTE_MATCH_PREDICATE, new StringPredicate(AssetQuery.Match.BEGIN, Integer.toString(i)).toModelValue())
                        )
                );
            assetStorageService.merge(thingAsset);
        }
    }
}
