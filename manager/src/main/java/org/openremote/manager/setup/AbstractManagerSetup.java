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
package org.openremote.manager.setup;

import org.openremote.agent.protocol.macro.MacroAction;
import org.openremote.agent.protocol.macro.MacroAgent;
import org.openremote.agent.protocol.timer.CronExpressionParser;
import org.openremote.agent.protocol.timer.TimerAgent;
import org.openremote.agent.protocol.timer.TimerValue;
import org.openremote.container.util.UniqueIdentifierGenerator;
import org.openremote.manager.asset.AssetProcessingService;
import org.openremote.manager.asset.AssetStorageService;
import org.openremote.manager.concurrent.ManagerExecutorService;
import org.openremote.manager.datapoint.AssetDatapointService;
import org.openremote.manager.persistence.ManagerPersistenceService;
import org.openremote.manager.predicted.AssetPredictedDatapointService;
import org.openremote.manager.rules.RulesetStorageService;
import org.openremote.manager.security.ManagerIdentityService;
import org.openremote.model.Constants;
import org.openremote.model.Container;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.asset.impl.*;
import org.openremote.model.attribute.*;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.value.ValueType;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.openremote.model.Constants.UNITS_TEMPERATURE_CELSIUS;
import static org.openremote.model.value.MetaItemType.*;
import static org.openremote.model.value.ValueType.*;

public abstract class AbstractManagerSetup implements Setup {

    final protected ManagerExecutorService executorService;
    final protected ManagerPersistenceService persistenceService;
    final protected ManagerIdentityService identityService;
    final protected AssetStorageService assetStorageService;
    final protected AssetProcessingService assetProcessingService;
    final protected AssetDatapointService assetDatapointService;
    final protected AssetPredictedDatapointService assetPredictedDatapointService;
    final protected RulesetStorageService rulesetStorageService;
    final protected SetupService setupService;

    public AbstractManagerSetup(Container container) {
        this.executorService = container.getService(ManagerExecutorService.class);
        this.persistenceService = container.getService(ManagerPersistenceService.class);
        this.identityService = container.getService(ManagerIdentityService.class);
        this.assetStorageService = container.getService(AssetStorageService.class);
        this.assetProcessingService = container.getService(AssetProcessingService.class);
        this.assetDatapointService = container.getService(AssetDatapointService.class);
        this.assetPredictedDatapointService = container.getService(AssetPredictedDatapointService.class);
        this.rulesetStorageService = container.getService(RulesetStorageService.class);
        this.setupService = container.getService(SetupService.class);
    }

    // ################################ Demo apartment with complex scenes ###################################

    protected BuildingAsset createDemoApartment(Asset parent, String name, GeoJSONPoint location) {
        BuildingAsset apartment = new BuildingAsset(name);

        apartment.setParentId(parent.getParentId());
        apartment.setAttributes(
            new Attribute<>("alarmEnabled", BOOLEAN)
                .addMeta(
                    new MetaItem<>(LABEL, "Alarm enabled"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true)
                ),
            new Attribute<>("presenceDetected", BOOLEAN)
                .addMeta(
                    new MetaItem<>(LABEL, "Presence detected"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(STORE_DATA_POINTS, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true)
                ),
            new Attribute<>("vacationUntil", TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "Vacation until"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(RULE_STATE, true)
                ),
            new Attribute<>("lastExecutedScene", ValueType.STRING)
                .addMeta(
                    new MetaItem<>(LABEL, "Last executed scene"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true)
                ),
            new Attribute<>(Asset.LOCATION, location)
                    .addMeta(new MetaItem<>(ACCESS_RESTRICTED_READ, true))
            /* TODO Unused, can be removed? Port schedule prediction from DRL...
            new Attribute<>("autoSceneSchedule", ValueType.BOOLEAN)
                .addMeta(
                    new MetaItem<>(LABEL, "Automatic scene schedule"),
                    new MetaItem<>(DESCRIPTION, "Predict presence and automatically adjust scene schedule"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(RULE_STATE, true)
                ),
            new Attribute<>("lastDetectedScene", ValueType.STRING)
                .addMeta(
                    new MetaItem<>(LABEL, "Last detected scene by rules"),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true)
                )
            */
        );
        return apartment;
    }

    protected RoomAsset createDemoApartmentRoom(Asset apartment, String name) {
        RoomAsset room = new RoomAsset(name);
        room.setParentId(apartment.getId());
        return room;
    }

    protected void addDemoApartmentRoomMotionSensor(RoomAsset room, boolean shouldBeLinked, Supplier<AgentLink> agentLinker) {
        room.getAttributes().addOrReplace(
            new Attribute<>("motionSensor", INTEGER)
                .addMeta(
                    new MetaItem<>(LABEL, "Motion sensor"),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null),
            new Attribute<>("presenceDetected", BOOLEAN)
                .addMeta(
                    new MetaItem<>(LABEL, "Presence detected"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(STORE_DATA_POINTS, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true)
                ),
            new Attribute<>("firstPresenceDetected", TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "First time movement was detected"),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true)
                ),
            new Attribute<>("lastPresenceDetected", TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "Last time movement was detected"),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true)
                )
        );
    }

    protected void addDemoApartmentRoomCO2Sensor(RoomAsset room, boolean shouldBeLinked, Supplier<AgentLink> agentLinker) {
        room.getAttributes().addOrReplace(
            new Attribute<>("co2Level", POSITIVE_INTEGER.addOrReplaceMeta(new MetaItem<>(UNIT_TYPE, Constants.UNITS_DENSITY_PARTS_MILLION)))
                .addMeta(
                    new MetaItem<>(LABEL, "CO2 level"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(RULE_EVENT, true),
                    new MetaItem<>(RULE_EVENT_EXPIRES, "45m"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true),
                    new MetaItem<>(FORMAT, "%4d ppm"),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null)
        );
    }

    protected void addDemoApartmentRoomHumiditySensor(RoomAsset room, boolean shouldBeLinked, Supplier<AgentLink> agentLinker) {
        room.getAttributes().addOrReplace(
            new Attribute<>("humidity", POSITIVE_INTEGER)
                .addMeta(
                    new MetaItem<>(LABEL, "Humidity"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(RULE_EVENT, true),
                    new MetaItem<>(RULE_EVENT_EXPIRES, "45m"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true),
                    new MetaItem<>(FORMAT, "%3d %%"),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null)
        );
    }

    protected void addDemoApartmentRoomThermometer(RoomAsset room,
                                                   boolean shouldBeLinked,
                                                   Supplier<AgentLink> agentLinker) {
        room.getAttributes().addOrReplace(
            new Attribute<>("currentTemperature", NUMBER.addOrReplaceMeta(new MetaItem<>(UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)))
                .addMeta(
                    new MetaItem<>(LABEL, "Current temperature"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true),
                    new MetaItem<>(FORMAT, "%0.1f° C"),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null)
        );
    }

    protected void addDemoApartmentTemperatureControl(RoomAsset room,
                                                      boolean shouldBeLinked,
                                                      Supplier<AgentLink> agentLinker) {
        room.getAttributes().addOrReplace(
            new Attribute<>("targetTemperature", NUMBER)
                .addMeta(
                    new MetaItem<>(LABEL, "Target temperature"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true),
                    new MetaItem<>(UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS),
                    new MetaItem<>(FORMAT, "%0f° C"),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null)
        );
    }

    protected void addDemoApartmentSmartSwitch(RoomAsset room,
                                               String switchName,
                                               boolean shouldBeLinked,
                                               // Integer represents attribute:
                                               // 0 = Mode
                                               // 1 = Time
                                               // 2 = StartTime
                                               // 3 = StopTime
                                               // 4 = Enabled
                                               Function<Integer, MetaItem[]> agentLinker) {

        room.getAttributes().addOrReplace(
            // Mode
            new Attribute<>("smartSwitchMode" + switchName, STRING)
                .addMeta(
                    new MetaItem<>(LABEL, "Smart Switch mode " + switchName),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(RULE_EVENT, true),
                    new MetaItem<>(RULE_EVENT_EXPIRES, "48h")
                ).addMeta(shouldBeLinked ? agentLinker.apply(0) : null),
            // Time
            new Attribute<>("smartSwitchBeginEnd" + switchName, TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "Smart Switch begin/end cycle " + switchName),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(RULE_STATE, true)
                ).addMeta(shouldBeLinked ? agentLinker.apply(1) : null),
            // StartTime
            new Attribute<>("smartSwitchStartTime" + switchName, TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "Smart Switch actuator earliest start time " + switchName),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(UNIT_TYPE, "SECONDS"),
                    new MetaItem<>(RULE_STATE, true)
                ).addMeta(shouldBeLinked ? agentLinker.apply(2) : null),
            // StopTime
            new Attribute<>("smartSwitchStopTime" + switchName, TIMESTAMP)
                .addMeta(
                    new MetaItem<>(LABEL, "Smart Switch actuator latest stop time " + switchName),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(UNIT_TYPE, "SECONDS"),
                    new MetaItem<>(RULE_STATE, true)
                ).addMeta(shouldBeLinked ? agentLinker.apply(3) : null),
            // Enabled
            new Attribute<>("smartSwitchEnabled" + switchName, NUMBER)
                .addMeta(
                    new MetaItem<>(LABEL, "Smart Switch actuator enabled " + switchName),
                    new MetaItem<>(READ_ONLY, true),
                    new MetaItem<>(RULE_STATE, true)
                ).addMeta(shouldBeLinked ? agentLinker.apply(4) : null)
        );
    }

    protected void addDemoApartmentVentilation(BuildingAsset apartment,
                                               boolean shouldBeLinked,
                                               Supplier<AgentLink> agentLinker) {
        apartment.getAttributes().addOrReplace(
            new Attribute<>("ventilationLevel", NUMBER)
                .addMeta(
                    new MetaItem<>(LABEL, "Ventilation level"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(FORMAT, "%d"),
                    new MetaItem<>(STORE_DATA_POINTS)
                ).addMeta(shouldBeLinked ? new MetaItem<>(AGENT_LINK, agentLinker.get()) : null),
            new Attribute<>("ventilationAuto", BOOLEAN)
                .addMeta(
                    new MetaItem<>(LABEL, "Ventilation auto"),
                    new MetaItem<>(RULE_STATE, true),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true)
                )
        );
    }

    public static class Scene {

        final String attributeName;
        final String sceneName;
        final String internalName;
        final String startTime;
        final boolean alarmEnabled;
        final double targetTemperature;

        public Scene(String attributeName,
                String sceneName,
                String internalName,
                String startTime,
                boolean alarmEnabled,
                double targetTemperature) {
            this.attributeName = attributeName;
            this.sceneName = sceneName;
            this.internalName = internalName;
            this.startTime = startTime;
            this.alarmEnabled = alarmEnabled;
            this.targetTemperature = targetTemperature;
        }

        MacroAgent createSceneAgent(BuildingAsset apartment, RoomAsset... rooms) {
            MacroAgent sceneAgent = new MacroAgent("Scene agent " + sceneName);
            sceneAgent.setId(UniqueIdentifierGenerator.generateId());
            sceneAgent.setParentId(apartment.getId());

            List<MacroAction> actions = new ArrayList<>();
            actions.add(new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "alarmEnabled"), alarmEnabled)));

            for (RoomAsset room : rooms) {
                if (room.hasAttribute("targetTemperature")) {
                    actions.add(
                        new MacroAction(new AttributeState(new AttributeRef(room.getId(), "targetTemperature"), targetTemperature))
                    );
                }
            }

            actions.add(
                new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "lastExecutedScene"), internalName))
            );

            sceneAgent.setMacroActions(actions.toArray(new MacroAction[0]));

            return sceneAgent;
        }

        List<TimerAgent> createTimerAgents(String macroAgentId, BuildingAsset apartment) {
            List<TimerAgent> agents = new ArrayList<>();

            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                // "MONDAY" => "Monday"
                String dayOfWeekLabel = dayOfWeek.name().substring(0, 1) + dayOfWeek.name().substring(1).toLowerCase(Locale.ROOT);
                // "0 0 7 ? *" => "0 0 7 ? * MON *"
                String timePattern = startTime + " " + dayOfWeek.name().substring(0, 3).toUpperCase(Locale.ROOT) + " *";
                TimerAgent timerAgent = new TimerAgent("Timer agent " + sceneName + " " + dayOfWeek.name());
                timerAgent.setParentId(apartment.getId());
                timerAgent.setId(UniqueIdentifierGenerator.generateId());
                timerAgent.setTimerAction(
                    new AttributeState(macroAgentId, MacroAgent.MACRO_STATUS.getName(), "REQUEST_START")
                );
                timerAgent.setTimerCronExpression(new CronExpressionParser(timePattern));
                agents.add(timerAgent);
            }
            return agents;
        }
    }

    public static List<Agent<?, ?, ?>> createDemoApartmentScenes(AssetStorageService assetStorageService, BuildingAsset apartment, Scene[] scenes, RoomAsset... rooms) {

        List<Agent<?, ?, ?>> agents = new ArrayList<>();

        for (Scene scene : scenes) {
            MacroAgent sceneAgent = scene.createSceneAgent(apartment, rooms);
            agents.add(sceneAgent);
            agents.addAll(scene.createTimerAgents(sceneAgent.getId(), apartment));
        }

        addDemoApartmentSceneEnableDisableTimer(apartment, agents, scenes);
        linkDemoApartmentWithSceneAgent(apartment, agents, scenes);
        agents.forEach(assetStorageService::merge);
        return agents;
    }

    protected static void addDemoApartmentSceneEnableDisableTimer(BuildingAsset apartment, List<Agent<?,?,?>> agents, Scene[] scenes) {

        MacroAgent enableSceneAgent = new MacroAgent("Scene agent enable");
        MacroAgent disableSceneAgent = new MacroAgent("Scene agent disable");
        List<MacroAction> enableActions = new ArrayList<>();
        List<MacroAction> disableActions = new ArrayList<>();

        enableSceneAgent.setParentId(apartment.getId());
        enableSceneAgent.setId(UniqueIdentifierGenerator.generateId());
        disableSceneAgent.setParentId(apartment.getId());
        disableSceneAgent.setId(UniqueIdentifierGenerator.generateId());

        for (Scene scene : scenes) {
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                String sceneAttributeName = scene.attributeName + "Enabled" + dayOfWeek;
                enableActions.add(
                    new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), sceneAttributeName), true))
                );
                disableActions.add(
                    new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), sceneAttributeName), false))
                );
            }
        }
        enableActions.add(
            new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "sceneTimerEnabled"), true))
        );
        disableActions.add(
            new MacroAction(new AttributeState(new AttributeRef(apartment.getId(), "sceneTimerEnabled"), false))
        );
        enableSceneAgent.setMacroActions(enableActions.toArray(new MacroAction[0]));
        disableSceneAgent.setMacroActions(disableActions.toArray(new MacroAction[0]));

        agents.add(1, enableSceneAgent);
        agents.add(2, disableSceneAgent);
    }

    protected static void linkDemoApartmentWithSceneAgent(Asset apartment, List<Agent<?,?,?>> agents, Scene[] scenes) {

        MacroAgent sceneAgent = (MacroAgent) agents.get(0);
        MacroAgent enableSceneAgent = (MacroAgent) agents.get(1);
        MacroAgent disableSceneAgent = (MacroAgent) agents.get(2);

        for (Scene scene : scenes) {

            apartment.getAttributes().addOrReplace(
                new Attribute<>(scene.attributeName, ValueType.STRING, AttributeExecuteStatus.READY.name())
                    .addMeta(
                        new MetaItem<>(LABEL, scene.sceneName),
                        new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                        new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                        new MetaItem<>(AGENT_LINK, new MacroAgent.MacroAgentLink(sceneAgent.getId()))
                    ),
                new Attribute<>(scene.attributeName + "AlarmEnabled", BOOLEAN)
                    .addMeta(
                        new MetaItem<>(LABEL, scene.sceneName + " alarm enabled"),
                        new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                        new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                        new MetaItem<>(AGENT_LINK, new MacroAgent.MacroAgentLink(sceneAgent.getId()).setActionIndex(0))
                    ),
                new Attribute<>(scene.attributeName + "TargetTemperature", ValueType.NUMBER)
                    .addMeta(
                        new MetaItem<>(LABEL, scene.sceneName + " target temperature"),
                        new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                        new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                        new MetaItem<>(AGENT_LINK, new MacroAgent.MacroAgentLink(sceneAgent.getId()).setActionIndex(1))
                    )
            );
            int i = 3;
            for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
                // "MONDAY" => "Monday"
                String dayOfWeekLabel = dayOfWeek.name().substring(0, 1) + dayOfWeek.name().substring(1).toLowerCase(Locale.ROOT);
                apartment.getAttributes().addOrReplace(
                    new Attribute<>(scene.attributeName + "Time" + dayOfWeek.name(), ValueType.STRING)
                        .addMeta(
                            new MetaItem<>(LABEL, scene.sceneName + " time " + dayOfWeekLabel),
                            new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                            new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                            new MetaItem<>(RULE_STATE, true),
                            new MetaItem<>(AGENT_LINK, new TimerAgent.TimerAgentLink(agents.get(i).getId()).setTimerValue(TimerValue.TIME))
                        ),
                    new Attribute<>(scene.attributeName + "Enabled" + dayOfWeek.name(), BOOLEAN)
                        .addMeta(
                            new MetaItem<>(LABEL, scene.sceneName + " enabled " + dayOfWeekLabel),
                            new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                            new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                            new MetaItem<>(AGENT_LINK, new TimerAgent.TimerAgentLink(agents.get(i).getId()).setTimerValue(TimerValue.ACTIVE))
                        )
                );
                i++;
            }
        }
        apartment.getAttributes().addOrReplace(
            new Attribute<>("sceneTimerEnabled", BOOLEAN, true) // The scene timer is enabled when the timer protocol starts
                .addMeta(
                    new MetaItem<>(LABEL, "Scene timer enabled"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(SHOW_ON_DASHBOARD, true),
                    new MetaItem<>(READ_ONLY, true)
                ),
            new Attribute<>("enableSceneTimer", EXECUTION_STATUS)
                .addMeta(
                    new MetaItem<>(LABEL, "Enable scene timer"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(AGENT_LINK, new MacroAgent.MacroAgentLink(enableSceneAgent.getId()))
                ),
            new Attribute<>("disableSceneTimer", EXECUTION_STATUS)
                .addMeta(
                    new MetaItem<>(LABEL, "Disable scene timer"),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    new MetaItem<>(AGENT_LINK, new MacroAgent.MacroAgentLink(disableSceneAgent.getId()))
                )
        );
    }

    protected PeopleCounterAsset createDemoPeopleCounterAsset(String name, Asset area, GeoJSONPoint location, Supplier<AgentLink> agentLinker) {
        PeopleCounterAsset peopleCounterAsset = new PeopleCounterAsset(name);
        peopleCounterAsset.setParentId(area.getId());
        peopleCounterAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );
        peopleCounterAsset.getAttribute("peopleCountIn").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });
        peopleCounterAsset.getAttribute("peopleCountOut").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });
        peopleCounterAsset.getAttribute("peopleCountInMinute").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });
        peopleCounterAsset.getAttribute("peopleCountOutMinute").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });
        peopleCounterAsset.getAttribute("peopleCountTotal").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });

        return peopleCounterAsset;
    }

    protected MicrophoneAsset createDemoMicrophoneAsset(String name, Asset area, GeoJSONPoint location, Supplier<AgentLink> agentLinker) {
        MicrophoneAsset microphoneAsset = new MicrophoneAsset(name);
        microphoneAsset.setParentId(area.getId());
        microphoneAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );
        microphoneAsset.getAttribute("microphoneLevel").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem<>(RULE_STATE),
                new MetaItem<>(STORE_DATA_POINTS)
            );
            if (agentLinker != null) {
                assetAttribute.addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
            }
        });


        return microphoneAsset;
    }

    protected EnvironmentSensorAsset createDemoEnvironmentAsset(String name, Asset area, GeoJSONPoint location, Supplier<AgentLink> agentLinker) {
        EnvironmentSensorAsset environmentAsset = new EnvironmentSensorAsset(name);
        environmentAsset.setParentId(area.getId());
        environmentAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.TEMPERATURE)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.NO2)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.RELATIVE_HUMIDITY)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.PM1)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.PM2_5)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));
        environmentAsset.getAttributes().getOrCreate(EnvironmentSensorAsset.PM10)
            .addMeta(new MetaItem<>(AGENT_LINK, agentLinker.get()));

        return environmentAsset;
    }

    protected LightAsset createDemoLightAsset(String name, Asset area, GeoJSONPoint location) {
        LightAsset lightAsset = new LightAsset(name);
        lightAsset.setParentId(area.getId());
        lightAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location).addMeta(new MetaItem<>(SHOW_ON_DASHBOARD, true))
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.ON_OFF).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.BRIGHTNESS).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.COLOUR_RGBW).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate("groupNumber", POSITIVE_INTEGER).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate("scenario", STRING).addMeta(
            new MetaItem<>(RULE_STATE)
        );

        return lightAsset;
    }

    protected LightAsset createDemoLightControllerAsset(String name, Asset area, GeoJSONPoint location) {
        LightAsset lightAsset = new LightAsset(name);
        lightAsset.setParentId(area.getId());
        lightAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.ON_OFF).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.BRIGHTNESS).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate(LightAsset.COLOUR_RGBW).addMeta(
            new MetaItem<>(RULE_STATE),
            new MetaItem<>(STORE_DATA_POINTS)
        );
        lightAsset.getAttributes().getOrCreate("scenario", STRING).addMeta(
            new MetaItem<>(RULE_STATE)
        );

        return lightAsset;
    }

    protected ElectricityStorageAsset createDemoElectricityStorageAsset(String name, Asset area, GeoJSONPoint location) {
        ElectricityStorageAsset electricityStorageAsset = new ElectricityStorageAsset(name);
        electricityStorageAsset.setParentId(area.getId());
        electricityStorageAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );

    return electricityStorageAsset;
    }

    protected ElectricityProducerAsset createDemoElectricityProducerAsset(String name, Asset area, GeoJSONPoint location) {
        ElectricityProducerAsset electricityProducerAsset = new ElectricityProducerAsset(name);
        electricityProducerAsset.setParentId(area.getId());
        electricityProducerAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );

    return electricityProducerAsset;
    }

    protected ElectricityConsumerAsset createDemoElectricityConsumerAsset(String name, Asset area, GeoJSONPoint location) {
        ElectricityConsumerAsset electricityConsumerAsset = new ElectricityConsumerAsset(name);
        electricityConsumerAsset.setParentId(area.getId());
        electricityConsumerAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );

    return electricityConsumerAsset;
    }

    protected ElectricityChargerAsset createDemoElectricityChargerAsset(String name, Asset area, GeoJSONPoint location) {
        ElectricityChargerAsset electricityChargerAsset = new ElectricityChargerAsset(name);
        electricityChargerAsset.setParentId(area.getId());
        electricityChargerAsset.getAttributes().addOrReplace(
                new Attribute<>(Asset.LOCATION, location)
        );

        return electricityChargerAsset;
    }

    protected GroundwaterSensorAsset createDemoGroundwaterAsset(String name, Asset area, GeoJSONPoint location) {
        GroundwaterSensorAsset groundwaterAsset = new GroundwaterSensorAsset(name);
        groundwaterAsset.setParentId(area.getId());
        groundwaterAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );

    return groundwaterAsset;
    }

    protected ParkingAsset createDemoParkingAsset(String name, Asset area, GeoJSONPoint location) {
        ParkingAsset parkingAsset = new ParkingAsset(name);
        parkingAsset.setParentId(area.getId());
        parkingAsset.getAttributes().addOrReplace(
            new Attribute<>(Asset.LOCATION, location)
        );

    return parkingAsset;
    }

    protected ShipAsset createDemoShipAsset(String name, Asset area, GeoJSONPoint location) {
        ShipAsset shipAsset = new ShipAsset(name);
        shipAsset.setParentId(area.getId());
        shipAsset.getAttributes().addOrReplace(
                new Attribute<>(Asset.LOCATION, location)
        );

        return shipAsset;
    }
}
