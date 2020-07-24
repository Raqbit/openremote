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
package org.openremote.manager.setup.builtin;

import org.openremote.agent.protocol.http.HttpClientProtocol;
import org.openremote.agent.protocol.simulator.SimulatorProtocol;
import org.openremote.container.Container;
import org.openremote.container.util.UniqueIdentifierGenerator;
import org.openremote.manager.security.UserConfiguration;
import org.openremote.manager.setup.AbstractManagerSetup;
import org.openremote.model.apps.ConsoleAppConfig;
import org.openremote.model.asset.*;
import org.openremote.model.attribute.*;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.security.Tenant;
import org.openremote.model.simulator.element.ColorSimulatorElement;
import org.openremote.model.simulator.element.NumberSimulatorElement;
import org.openremote.model.simulator.element.SwitchSimulatorElement;
import org.openremote.model.simulator.element.ReplaySimulatorElement;
import org.openremote.model.value.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.time.LocalTime;

import static java.time.temporal.ChronoField.SECOND_OF_DAY;
import static org.openremote.agent.protocol.http.HttpClientProtocol.*;
import static org.openremote.manager.datapoint.AssetDatapointService.DATA_POINTS_MAX_AGE_DAYS_DEFAULT;
import static org.openremote.model.Constants.*;
import static org.openremote.model.asset.AssetType.*;
import static org.openremote.model.asset.agent.ProtocolConfiguration.initProtocolConfiguration;
import static org.openremote.model.attribute.AttributeValueType.*;
import static org.openremote.model.attribute.MetaItemType.*;

public class ManagerDemoSetup extends AbstractManagerSetup {

    // Update these numbers whenever you change a RULE_STATE flag in test data
    public static final int DEMO_RULE_STATES_APARTMENT_1 = 44;
    public static final int DEMO_RULE_STATES_APARTMENT_2 = 13;
    public static final int DEMO_RULE_STATES_APARTMENT_3 = 0;
    public static final int DEMO_RULE_STATES_SMART_OFFICE = 1;
    public static final int DEMO_RULE_STATES_SMART_BUILDING = DEMO_RULE_STATES_APARTMENT_1 + DEMO_RULE_STATES_APARTMENT_2 + DEMO_RULE_STATES_APARTMENT_3;
    public static final int DEMO_RULE_STATES_SMART_CITY = 44;
    public static final int DEMO_RULE_STATES_GLOBAL = DEMO_RULE_STATES_SMART_BUILDING + DEMO_RULE_STATES_SMART_OFFICE + DEMO_RULE_STATES_SMART_CITY;
    public static final int DEMO_RULE_STATES_APARTMENT_1_WITH_SCENES = DEMO_RULE_STATES_APARTMENT_1 + 28;
    public static final int DEMO_RULE_STATES_SMART_BUILDING_WITH_SCENES = DEMO_RULE_STATES_APARTMENT_1_WITH_SCENES + DEMO_RULE_STATES_APARTMENT_2 + DEMO_RULE_STATES_APARTMENT_3;
    public static GeoJSONPoint SMART_OFFICE_LOCATION = new GeoJSONPoint(5.460315214821094, 51.44541688237109);
    public static GeoJSONPoint SMART_BUILDING_LOCATION = new GeoJSONPoint(5.454027, 51.446308);
    public static GeoJSONPoint SMART_CITY_LOCATION = new GeoJSONPoint(5.3814711, 51.4484647);
    public static GeoJSONPoint AREA_1_LOCATION = new GeoJSONPoint(5.478478, 51.439272);
    public static GeoJSONPoint AREA_2_LOCATION = new GeoJSONPoint(5.473829, 51.438744);
    public static GeoJSONPoint AREA_3_LOCATION = new GeoJSONPoint(5.487478, 51.446979);
    public static GeoJSONPoint STATIONSPLEIN_LOCATION = new GeoJSONPoint(4.470175, 51.923464);
    public static final String agentProtocolConfigName = "simulator123";
    public static final String thingLightToggleAttributeName = "light1Toggle";
    public static final Scene[] DEMO_APARTMENT_SCENES = new Scene[] {
        new Scene("morningScene", "Morning scene", "MORNING", "0 0 7 ? *", false, 21d),
        new Scene("dayScene", "Day scene", "DAY", "0 30 8 ? *", true, 15d),
        new Scene("eveningScene", "Evening scene", "EVENING", "0 30 17 ? *", false, 22d),
        new Scene("nightScene", "Night scene", "NIGHT", "0 0 22 ? *", true, 19d)
    };
    final protected boolean importDemoScenes;
    public String smartOfficeId;
    public String groundFloorId;
    public String lobbyId;
    public String agentId;
    public String thingId;
    public String smartBuildingId;
    public String apartment1Id;
    public String apartment1SceneAgentId;
    public String apartment1ServiceAgentId;
    public String apartment1LivingroomId = UniqueIdentifierGenerator.generateId("apartment1LivingroomId");
    public String apartment1KitchenId;
    public String apartment1HallwayId;
    public String apartment1Bedroom1Id;
    public String apartment1BathroomId;
    public String apartment2Id;
    public String apartment3Id;
    public String apartment2LivingroomId;
    public String apartment2BathroomId;
    public String apartment3LivingroomId;
    public String masterRealm;
    public String realmBuildingTenant;
    public String realmCityTenant;
    public String smartCityServiceAgentId;
    public String area1Id;
    public String microphone1Id;
    public String peopleCounter3AssetId;
    public String smartcitySimulatorAgentId;
    public String energyManagementId;
    public String weatherId;
    public String weatherHttpApiAgentId;

    private final long halfHourInMillis = Duration.ofMinutes(30).toMillis();

    public ManagerDemoSetup(Container container, boolean importDemoScenes) {
        super(container);
        this.importDemoScenes = importDemoScenes;
    }

    @Override
    public void onStart() throws Exception {

        KeycloakDemoSetup keycloakDemoSetup = setupService.getTaskOfType(KeycloakDemoSetup.class);
        Tenant masterTenant = keycloakDemoSetup.masterTenant;
        Tenant tenantBuilding = keycloakDemoSetup.tenantBuilding;
        Tenant tenantCity = keycloakDemoSetup.tenantCity;
        masterRealm = masterTenant.getRealm();
        this.realmBuildingTenant = tenantBuilding.getRealm();
        this.realmCityTenant = tenantCity.getRealm();

        // ################################ Demo assets for 'master' realm ###################################

        ObjectValue locationValue = SMART_OFFICE_LOCATION.toValue();

        Asset smartOffice = new Asset();
        smartOffice.setRealm(masterRealm);
        smartOffice.setName("Smart Office");
        smartOffice.setType(BUILDING);
        List<AssetAttribute> smartOfficeAttributes = Arrays.asList(
            new AssetAttribute(AttributeType.LOCATION, locationValue),
            new AssetAttribute(AttributeType.GEO_STREET, Values.create("Torenallee 20")),
            new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("5617")),
            new AssetAttribute(AttributeType.GEO_CITY, Values.create("Eindhoven")),
            new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
        );

        smartOffice.setAttributes(smartOfficeAttributes);
        smartOffice = assetStorageService.merge(smartOffice);
        smartOfficeId = smartOffice.getId();

        Asset groundFloor = new Asset("Ground Floor", FLOOR, smartOffice)
            .addAttributes(new AssetAttribute(AttributeType.LOCATION, locationValue));
        groundFloor = assetStorageService.merge(groundFloor);
        groundFloorId = groundFloor.getId();

        Asset lobby = new Asset("Lobby", ROOM, groundFloor)
            .addAttributes(new AssetAttribute(AttributeType.LOCATION, locationValue));
        lobby.addAttributes(
            new AssetAttribute("lobbyLocations", AttributeValueType.ARRAY)
        );

        lobby = assetStorageService.merge(lobby);
        lobbyId = lobby.getId();

        Asset agent = new Asset("Demo Agent", AGENT, lobby);
        agent.addAttributes(
            new AssetAttribute(AttributeType.LOCATION, locationValue),
            initProtocolConfiguration(new AssetAttribute(agentProtocolConfigName), SimulatorProtocol.PROTOCOL_NAME)
                .addMeta(
                    new MetaItem(
                        SimulatorProtocol.CONFIG_MODE,
                        Values.create(SimulatorProtocol.Mode.WRITE_THROUGH_DELAYED.toString())
                    ),
                    new MetaItem(
                        SimulatorProtocol.CONFIG_WRITE_DELAY_MILLISECONDS,
                        Values.create(500)
                    ))
        );

        agent = assetStorageService.merge(agent);
        agentId = agent.getId();

        Asset thing = new Asset("Demo Thing", THING, agent)
            .addAttributes(new AssetAttribute(AttributeType.LOCATION, locationValue)
                               .setMeta(new MetaItem(RULE_STATE, Values.create(true))));
        thing.addAttributes(
            new AssetAttribute(thingLightToggleAttributeName, BOOLEAN, Values.create(true))
                .setMeta(new Meta(
                    new MetaItem(
                        LABEL,
                        Values.create("Light 1 Toggle")),
                    new MetaItem(
                        DESCRIPTION,
                        Values.create("Switch for living room light")),
                    new MetaItem(
                        STORE_DATA_POINTS,
                        Values.create(true)),
                    new MetaItem(
                        DATA_POINTS_MAX_AGE_DAYS,
                        Values.create(DATA_POINTS_MAX_AGE_DAYS_DEFAULT*7)
                    ),
                    new MetaItem(
                        AGENT_LINK,
                        new AttributeRef(agent.getId(), agentProtocolConfigName).toArrayValue()),
                    new MetaItem(
                        SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(SwitchSimulatorElement.ELEMENT_NAME)
                    ))
                ),
            new AssetAttribute("light1Dimmer", PERCENTAGE) // No initial value!
                .setMeta(new Meta(
                             new MetaItem(
                                 LABEL,
                                 Values.create("Light 1 Dimmer")),
                             new MetaItem(
                                 DESCRIPTION,
                                 Values.create("Dimmer for living room light")),
                             new MetaItem(
                                 RANGE_MIN,
                                 Values.create(0)),
                             new MetaItem(
                                 RANGE_MAX,
                                 Values.create(100)),
                             new MetaItem(
                                 AGENT_LINK,
                                 new AttributeRef(agent.getId(), agentProtocolConfigName).toArrayValue()),
                             new MetaItem(
                                 SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME_RANGE)),
                             new MetaItem(
                                 SimulatorProtocol.CONFIG_MODE,
                                 Values.create(SimulatorProtocol.Mode.WRITE_THROUGH_DELAYED.toString()))
                         )
                ),
            new AssetAttribute("light1Color", COLOR_RGB, new ColorRGB(88, 123, 88).asArrayValue())
                .setMeta(new Meta(
                             new MetaItem(
                                 LABEL,
                                 Values.create("Light 1 Color")),
                             new MetaItem(
                                 DESCRIPTION,
                                 Values.create("Color of living room light")),
                             new MetaItem(
                                 AGENT_LINK,
                                 new AttributeRef(agent.getId(), agentProtocolConfigName).toArrayValue()),
                             new MetaItem(
                                 SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ColorSimulatorElement.ELEMENT_NAME))
                         )
                ),
            new AssetAttribute("light1PowerConsumption", ENERGY, Values.create(12.345))
                .setMeta(new Meta(
                             new MetaItem(
                                 LABEL,
                                 Values.create("Light 1 Usage")),
                             new MetaItem(
                                 DESCRIPTION,
                                 Values.create("Total energy consumption of living room light")),
                             new MetaItem(
                                 READ_ONLY,
                                 Values.create(true)),
                             new MetaItem(
                                 FORMAT,
                                 Values.create("%3d kWh")),
                             new MetaItem(
                                 AGENT_LINK,
                                 new AttributeRef(agent.getId(), agentProtocolConfigName).toArrayValue()),
                             new MetaItem(
                                 SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME)),
                             new MetaItem(
                                 STORE_DATA_POINTS, Values.create(true))
                         )
                )
        );
        thing = assetStorageService.merge(thing);
        thingId = thing.getId();

        // ################################ Demo assets for 'building' realm ###################################

        Asset smartBuilding = new Asset();
        smartBuilding.setRealm(this.realmBuildingTenant);
        smartBuilding.setName("Smart Building");
        smartBuilding.setType(BUILDING);
        smartBuilding.setAttributes(
            new AssetAttribute(AttributeType.LOCATION, SMART_BUILDING_LOCATION.toValue()).addMeta(SHOW_ON_DASHBOARD),
            new AssetAttribute(AttributeType.GEO_STREET, Values.create("Kastanjelaan 500")),
            new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("5616")),
            new AssetAttribute(AttributeType.GEO_CITY, Values.create("Eindhoven")),
            new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
        );
        smartBuilding = assetStorageService.merge(smartBuilding);
        smartBuildingId = smartBuilding.getId();

        // The "Apartment 1" is the demo apartment with complex scenes
        Asset apartment1 = createDemoApartment(smartBuilding, "Apartment 1", new GeoJSONPoint(5.454233, 51.446800));
        apartment1 = assetStorageService.merge(apartment1);
        apartment1Id = apartment1.getId();

        Asset apartment1ServiceAgent = new Asset("Service Agent (Simulator)", AGENT, apartment1);
        apartment1ServiceAgent.addAttributes(
            initProtocolConfiguration(new AssetAttribute("apartmentSimulator"), SimulatorProtocol.PROTOCOL_NAME)
                .addMeta(
                    new MetaItem(
                        SimulatorProtocol.CONFIG_MODE,
                        Values.create(SimulatorProtocol.Mode.WRITE_THROUGH_IMMEDIATE.toString())
                    ))
        );
        apartment1ServiceAgent = assetStorageService.merge(apartment1ServiceAgent);
        apartment1ServiceAgentId = apartment1ServiceAgent.getId();

        /* ############################ ROOMS ############################## */

        Asset apartment1Livingroom = createDemoApartmentRoom(apartment1, "Living Room 1")
            .addAttributes(
                    new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454213, 51.446884).toValue()),
                    new AssetAttribute("lightsCeiling", NUMBER, Values.create(0))
                            .setMeta(
                                    new MetaItem(RANGE_MIN, Values.create(0)),
                                    new MetaItem(RANGE_MAX, Values.create(100)),
                                    new MetaItem(LABEL, Values.create("Ceiling lights (range)")),
                                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true))
                            ),
                    new AssetAttribute("lightsStand", AttributeValueType.BOOLEAN, Values.create(true))
                            .setMeta(
                                    new MetaItem(LABEL, Values.create("Floor stand lights (on/off)")),
                                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                                    new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true))
                            )
            );
        addDemoApartmentRoomMotionSensor(apartment1Livingroom, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentRoomCO2Sensor(apartment1Livingroom, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentRoomHumiditySensor(apartment1Livingroom, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentRoomThermometer(apartment1Livingroom, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentTemperatureControl(apartment1Livingroom, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });

        apartment1Livingroom.setId(apartment1LivingroomId);
        apartment1Livingroom = assetStorageService.merge(apartment1Livingroom);
        apartment1LivingroomId = apartment1Livingroom.getId();

        Asset apartment1Kitchen = createDemoApartmentRoom(apartment1, "Kitchen 1")
            .addAttributes(
                    new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454122, 51.446800).toValue()),
                    new AssetAttribute("lights", AttributeValueType.BOOLEAN, Values.create(true))
                        .addMeta(new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)))
                        .addMeta(new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)))
            );
        addDemoApartmentRoomMotionSensor(apartment1Kitchen, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });

        for (String switchName : new String[]{"A", "B", "C"}) {
            addDemoApartmentSmartSwitch(apartment1Kitchen, switchName, true, attributeIndex -> {
                switch (attributeIndex) {
                    case 2:
                        return new MetaItem[]{
                            new MetaItem(MetaItemType.AGENT_LINK,
                                         new AttributeRef(apartment1ServiceAgentId,
                                                          "apartmentSimulator").toArrayValue()),
                            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT,
                                         Values.create(NumberSimulatorElement.ELEMENT_NAME))
                        };
                    case 3:
                        return new MetaItem[]{
                            new MetaItem(MetaItemType.AGENT_LINK,
                                         new AttributeRef(apartment1ServiceAgentId,
                                                          "apartmentSimulator").toArrayValue()),
                            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT,
                                         Values.create(NumberSimulatorElement.ELEMENT_NAME))
                        };
                    case 4:
                        return new MetaItem[]{
                            new MetaItem(MetaItemType.AGENT_LINK,
                                         new AttributeRef(apartment1ServiceAgentId,
                                                          "apartmentSimulator").toArrayValue()),
                            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT,
                                         Values.create(NumberSimulatorElement.ELEMENT_NAME))
                        };
                }
                return null;
            });
        }

        apartment1Kitchen = assetStorageService.merge(apartment1Kitchen);
        apartment1KitchenId = apartment1Kitchen.getId();

        Asset apartment1Hallway = createDemoApartmentRoom(apartment1, "Hallway 1")
            .addAttributes(
                    new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454342, 51.446762).toValue()),
                    new AssetAttribute("lights", AttributeValueType.BOOLEAN, Values.create(true))
                            .addMeta(new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)))
                            .addMeta(new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)))
            );
        addDemoApartmentRoomMotionSensor(apartment1Hallway, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });

        apartment1Hallway = assetStorageService.merge(apartment1Hallway);
        apartment1HallwayId = apartment1Hallway.getId();

        Asset apartment1Bedroom1 = createDemoApartmentRoom(apartment1, "Bedroom 1")
                .addAttributes(
                        new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454332, 51.446830).toValue()),
                        new AssetAttribute("lights", AttributeValueType.BOOLEAN, Values.create(true))
                                .addMeta(new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)))
                                .addMeta(new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true)))
                );
        addDemoApartmentRoomCO2Sensor(apartment1Bedroom1, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentRoomHumiditySensor(apartment1Bedroom1, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentRoomThermometer(apartment1Bedroom1, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentTemperatureControl(apartment1Bedroom1, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });

        apartment1Bedroom1 = assetStorageService.merge(apartment1Bedroom1);
        apartment1Bedroom1Id = apartment1Bedroom1.getId();

        Asset apartment1Bathroom = new Asset("Bathroom 1", ROOM, apartment1);
        apartment1Bathroom.addAttributes(
            new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454227,51.446753).toValue()),
            new AssetAttribute("lights", AttributeValueType.BOOLEAN, Values.create(true))
                    .setMeta(
                            new MetaItem(RULE_STATE, Values.create(true)),
                            new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true)),
                            new MetaItem(ACCESS_RESTRICTED_WRITE, Values.create(true))
                    )
        );
        addDemoApartmentRoomThermometer(apartment1Bathroom, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        addDemoApartmentTemperatureControl(apartment1Bathroom, true, () -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        apartment1Bathroom = assetStorageService.merge(apartment1Bathroom);
        apartment1BathroomId = apartment1Bathroom.getId();


        addDemoApartmentVentilation(apartment1, true, () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(apartment1ServiceAgentId, "apartmentSimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });

        apartment1 = assetStorageService.merge(apartment1);
        apartment1Id = apartment1.getId();

        if (importDemoScenes) {
            Asset demoApartment1SceneAgent = createDemoApartmentScenes(
                assetStorageService, apartment1, DEMO_APARTMENT_SCENES, apartment1Livingroom, apartment1Kitchen, apartment1Hallway);
            apartment1SceneAgentId = demoApartment1SceneAgent.getId();
        }

        Asset apartment2 = new Asset("Apartment 2", RESIDENCE, smartBuilding);
        apartment2.addAttributes(
            new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454053, 51.446603).toValue()),
            new AssetAttribute("allLightsOffSwitch", AttributeValueType.BOOLEAN, Values.create(true))
                .setMeta(
                    new MetaItem(LABEL, Values.create("All Lights Off Switch")),
                    new MetaItem(DESCRIPTION, Values.create("When triggered, turns all lights in the apartment off")),
                    new MetaItem(RULE_EVENT, Values.create(true)),
                    new MetaItem(RULE_EVENT_EXPIRES, Values.create("3s"))
                )
        );
        apartment2 = assetStorageService.merge(apartment2);
        apartment2Id = apartment2.getId();

        Asset apartment2Livingroom = new Asset("Living Room 2", ROOM, apartment2);
        apartment2Livingroom.addAttributes(
            new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454109, 51.446631).toValue()),
            new AssetAttribute("motionSensor", AttributeValueType.BOOLEAN, Values.create(false))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Motion Sensor")),
                    new MetaItem(DESCRIPTION, Values.create("PIR sensor that sends 'true' when motion is sensed")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(RULE_EVENT, Values.create(true))
                ),
            new AssetAttribute("presenceDetected", AttributeValueType.BOOLEAN, Values.create(false))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Presence Detected")),
                    new MetaItem(DESCRIPTION, Values.create("Someone is currently present in the room")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("firstPresenceDetected", AttributeValueType.TIMESTAMP)
                .setMeta(
                    new MetaItem(LABEL, Values.create("First Presence Timestamp")),
                    new MetaItem(DESCRIPTION, Values.create("Timestamp of the first detected presence")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lastPresenceDetected", AttributeValueType.TIMESTAMP)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Last Presence Timestamp")),
                    new MetaItem(DESCRIPTION, Values.create("Timestamp of last detected presence")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("co2Level", AttributeValueType.CO2, Values.create(350))
                .setMeta(
                    new MetaItem(LABEL, Values.create("CO2 Level")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lightSwitch", AttributeValueType.BOOLEAN, Values.create(true))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Light Switch")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("windowOpen", AttributeValueType.BOOLEAN, Values.create(false))
                .setMeta(
                    new MetaItem(ACCESS_RESTRICTED_READ, Values.create(true))
                ),
            new AssetAttribute("lightSwitchTriggerTimes", ARRAY, Values.createArray().add(Values.create("1800")).add(Values.create("0830")))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Lightswitch Trigger Times")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("plantsWaterLevels", OBJECT, Values.createObject().put("cactus", 0.8))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Water levels of the plants")),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );
        apartment2Livingroom = assetStorageService.merge(apartment2Livingroom);
        apartment2LivingroomId = apartment2Livingroom.getId();

        Asset apartment2Bathroom = new Asset("Bathroom 2", ROOM, apartment2);
        apartment2Bathroom.addAttributes(
            new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.454015, 51.446665).toValue()),
            new AssetAttribute("motionSensor", AttributeValueType.BOOLEAN, Values.create(false))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Motion Sensor")),
                    new MetaItem(DESCRIPTION, Values.create("PIR sensor that sends 'true' when motion is sensed")),
                    new MetaItem(RULE_STATE, Values.create(true)),
                    new MetaItem(RULE_EVENT, Values.create(true))
                ),
            new AssetAttribute("presenceDetected", AttributeValueType.BOOLEAN, Values.create(false))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Presence Detected")),
                    new MetaItem(DESCRIPTION, Values.create("Someone is currently present in the room")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("firstPresenceDetected", AttributeValueType.TIMESTAMP)
                .setMeta(
                    new MetaItem(LABEL, Values.create("First Presence Timestamp")),
                    new MetaItem(DESCRIPTION, Values.create("Timestamp of the first detected presence")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lastPresenceDetected", AttributeValueType.TIMESTAMP)
                .setMeta(
                    new MetaItem(LABEL, Values.create("Last Presence Timestamp")),
                    new MetaItem(DESCRIPTION, Values.create("Timestamp of last detected presence")),
                    new MetaItem(RULE_STATE, Values.create(true))
                ),
            new AssetAttribute("lightSwitch", AttributeValueType.BOOLEAN, Values.create(true))
                .setMeta(
                    new MetaItem(LABEL, Values.create("Light Switch")),
                    new MetaItem(RULE_STATE, Values.create(true))
                )
        );
        apartment2Bathroom = assetStorageService.merge(apartment2Bathroom);
        apartment2BathroomId = apartment2Bathroom.getId();

        Asset apartment3 = new Asset("Apartment 3", RESIDENCE, smartBuilding)
            .addAttributes(new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.453859, 51.446379).toValue()));
        apartment3 = assetStorageService.merge(apartment3);
        apartment3Id = apartment3.getId();

        Asset apartment3Livingroom = new Asset("Living Room 3", ROOM, apartment3)
            .addAttributes(new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(5.453932, 51.446422).toValue()));
        apartment3Livingroom.addAttributes(
            new AssetAttribute("lightSwitch", AttributeValueType.BOOLEAN)
        );

        apartment3Livingroom = assetStorageService.merge(apartment3Livingroom);
        apartment3LivingroomId = apartment3Livingroom.getId();

        // ################################ Link demo users and assets ###################################

        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1Id));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1LivingroomId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1KitchenId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1Bedroom1Id));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1BathroomId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                                                         keycloakDemoSetup.testuser3Id,
                                                         apartment1HallwayId));

        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1Id));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1LivingroomId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1KitchenId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1Bedroom1Id));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1BathroomId));
        assetStorageService.storeUserAsset(new UserAsset(keycloakDemoSetup.tenantBuilding.getRealm(),
                keycloakDemoSetup.buildingUserId,
                apartment1HallwayId));

        // ################################ Make users restricted ###################################

        UserConfiguration testuser3Config = identityService.getUserConfiguration(keycloakDemoSetup.testuser3Id);
        testuser3Config.setRestricted(true);
        testuser3Config = identityService.mergeUserConfiguration(testuser3Config);

        UserConfiguration buildingUserConfig = identityService.getUserConfiguration(keycloakDemoSetup.buildingUserId);
        testuser3Config.setRestricted(true);
        buildingUserConfig = identityService.mergeUserConfiguration(buildingUserConfig);

        // ################################ Realm smartcity ###################################

        Asset smartcitySimulatorAgent = new Asset("Simulator Agent", AssetType.AGENT);
        smartcitySimulatorAgent.setRealm(this.realmCityTenant);
        smartcitySimulatorAgent
            .addAttributes(
                initProtocolConfiguration(new AssetAttribute("inputSimulator"), SimulatorProtocol.PROTOCOL_NAME)
                    .addMeta(
                        new MetaItem(
                            SimulatorProtocol.CONFIG_MODE,
                            Values.create(SimulatorProtocol.Mode.WRITE_THROUGH_IMMEDIATE.toString())
                    ))
            )
            .addAttributes(
                initProtocolConfiguration(new AssetAttribute("replaySimulator"), SimulatorProtocol.PROTOCOL_NAME)
                    .addMeta(
                        new MetaItem(
                            SimulatorProtocol.CONFIG_MODE,
                            Values.create(SimulatorProtocol.Mode.REPLAY.toString())
                        )
                )
			);
        smartcitySimulatorAgent = assetStorageService.merge(smartcitySimulatorAgent);
        smartcitySimulatorAgentId = smartcitySimulatorAgent.getId();

        LocalTime midnight = LocalTime.of(0, 0);

        // ################################ Realm smartcity - Energy Management ###################################
        
        Asset energyManagement = new Asset();
        energyManagement.setRealm(this.realmCityTenant);
        energyManagement.setName("Energy Management");
        energyManagement.setType(THING);
        energyManagement.addAttributes(
            new AssetAttribute("totalPowerProducers", POWER).addMeta(
                LABEL.withInitialValue("Combined power of all producers"),
                UNIT_TYPE.withInitialValue(UNITS_POWER_KILOWATT),
                STORE_DATA_POINTS,
                READ_ONLY,
                RULE_STATE),
            new AssetAttribute("totalPowerConsumers", POWER).addMeta(
                LABEL.withInitialValue("Combined power use of all consumers"),
                UNIT_TYPE.withInitialValue(UNITS_POWER_KILOWATT),
                STORE_DATA_POINTS,
                RULE_STATE,
                READ_ONLY)
        );
        energyManagement = assetStorageService.merge(energyManagement);
        energyManagementId = energyManagement.getId();

        // ### De Rotterdam ###
        Asset building1Asset = new Asset("De Rotterdam", BUILDING, energyManagement);
        building1Asset.setAttributes(
                new AssetAttribute(AttributeType.GEO_STREET, Values.create("Wilhelminakade 139")),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3072 AP")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands")),
                new AssetAttribute("powerBalance", POWER).addMeta(
                        LABEL.withInitialValue("Balance of power production and use"),
                        UNIT_TYPE.withInitialValue(UNITS_POWER_KILOWATT),
                        STORE_DATA_POINTS,
                        RULE_STATE,
                        READ_ONLY)
        );
        building1Asset = assetStorageService.merge(building1Asset);

        Asset storage1Asset = createDemoElectricityStorageAsset("Battery De Rotterdam", building1Asset, new GeoJSONPoint(4.488324, 51.906577));
        storage1Asset = assetStorageService.merge(storage1Asset);

        Asset consumption1Asset = createDemoElectricityConsumerAsset("Consumption De Rotterdam", building1Asset, new GeoJSONPoint(4.487519, 51.906544));
        consumption1Asset = assetStorageService.merge(consumption1Asset);

        Asset production1Asset = createDemoElectricityProducerAsset("Solar De Rotterdam", building1Asset, new GeoJSONPoint(4.488592, 51.907047));
        production1Asset.getAttribute("totalPower").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(6.02));
            assetAttribute.addMeta(STORE_DATA_POINTS);
        });
        production1Asset.getAttribute("totalEnergy").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(6.02)));
        production1Asset.getAttribute("installedCapacity").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(16.52)));
        production1Asset.getAttribute("systemEfficiency").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(93)));
        production1Asset.getAttribute("panelOrientation").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(ElectricityProducerOrientationType.EAST_WEST.name())));
        production1Asset = assetStorageService.merge(production1Asset);

        // ### Stadhuis ###

        Asset building2Asset = new Asset("Stadhuis", BUILDING, energyManagement);
        building2Asset.setAttributes(
                new AssetAttribute(AttributeType.GEO_STREET, Values.create("Coolsingel 40")),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3011 AD")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
        );
        building2Asset = assetStorageService.merge(building2Asset);

        Asset storage2Asset = createDemoElectricityStorageAsset("Battery Stadhuis", building2Asset, new GeoJSONPoint(4.47985, 51.92274));
        storage2Asset = assetStorageService.merge(storage2Asset);

        Asset consumption2Asset = createDemoElectricityConsumerAsset("Consumption Stadhuis", building2Asset, new GeoJSONPoint(4.47933, 51.92259));
        consumption2Asset = assetStorageService.merge(consumption2Asset);

        Asset production2Asset = createDemoElectricityProducerAsset("Solar Stadhuis", building2Asset, new GeoJSONPoint(4.47945, 51.92301));
        production2Asset.getAttribute("totalPower").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(5.13));
            assetAttribute.addMeta(STORE_DATA_POINTS);
        });
        production2Asset.getAttribute("totalEnergy").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(5.13)));
        production2Asset.getAttribute("installedCapacity").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(15.70)));
        production2Asset.getAttribute("systemEfficiency").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(79)));
        production2Asset.getAttribute("panelOrientation").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(ElectricityProducerOrientationType.SOUTH.name())));
        production2Asset = assetStorageService.merge(production2Asset);

        // ### Markthal ###

        Asset building3Asset = new Asset("Markthal", BUILDING, energyManagement);
        building3Asset.setAttributes(
                new AssetAttribute(AttributeType.GEO_STREET, Values.create("Dominee Jan Scharpstraat 298")),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3011 GZ")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands")),
                new AssetAttribute("allChargersInUse", BOOLEAN)
                    .addMeta(
                        LABEL.withInitialValue("All chargers in use"),
                        RULE_STATE,
                        READ_ONLY)
        );
        building3Asset = assetStorageService.merge(building3Asset);

        Asset production3Asset = createDemoElectricityProducerAsset("Solar Markthal", building3Asset, new GeoJSONPoint(4.47945, 51.92301));
        production3Asset.getAttribute("totalPower").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(4.28));
            assetAttribute.addMeta(STORE_DATA_POINTS);
        });
        production3Asset.getAttribute("totalEnergy").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(4.28)));
        production3Asset.getAttribute("installedCapacity").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(18.11)));
        production3Asset.getAttribute("systemEfficiency").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(91)));
        production3Asset.getAttribute("panelOrientation").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(ElectricityProducerOrientationType.SOUTH.name())));
        production3Asset = assetStorageService.merge(production3Asset);

        Asset charger1Asset = createDemoElectricityChargerAsset("Charger 1 Markthal", building3Asset, new GeoJSONPoint(4.486143, 51.920058));
        charger1Asset.getAttribute("power").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(0));
            assetAttribute.addMeta(
                    new MetaItem(
                            AGENT_LINK,
                            new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                    ),
                    new MetaItem(
                            SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                    ),
                    new MetaItem(
                            SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                            Values.createArray().addAll(
                                    Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 2),
                                    Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 5),
                                    Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 10),
                                    Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 5),
                                    Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 15),
                                    Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 32),
                                    Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 35),
                                    Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 17),
                                    Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 9),
                                    Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 6),
                                    Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 0)
                            )
                    )
            );
        });
        charger1Asset = assetStorageService.merge(charger1Asset);

        Asset charger2Asset = createDemoElectricityChargerAsset("Charger 2 Markthal", building3Asset, new GeoJSONPoint(4.486188, 51.919957));
        charger2Asset.getAttribute("power").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(0));
            assetAttribute.addMeta(
                    new MetaItem(
                            AGENT_LINK,
                            new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                    ),
                    new MetaItem(
                            SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                    ),
                    new MetaItem(
                            SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                            Values.createArray().addAll(
                                    Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 5),
                                    Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 11),
                                    Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 5),
                                    Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 10),
                                    Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 6),
                                    Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 17),
                                    Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 14),
                                    Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 9),
                                    Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 28),
                                    Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 38),
                                    Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 32),
                                    Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 26),
                                    Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 13),
                                    Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 6),
                                    Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 0)
                            )
                    )
            );
        });
        charger2Asset = assetStorageService.merge(charger2Asset);

        Asset charger3Asset = createDemoElectricityChargerAsset("Charger 3 Markthal", building3Asset, new GeoJSONPoint(4.486232, 51.919856));
        charger1Asset.getAttribute("power").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(0));
            assetAttribute.addMeta(
                    new MetaItem(
                            AGENT_LINK,
                            new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                    ),
                    new MetaItem(
                            SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                    ),
                    new MetaItem(
                            SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                            Values.createArray().addAll(
                                    Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 7),
                                    Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 9),
                                    Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 6),
                                    Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 2),
                                    Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 6),
                                    Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 18),
                                    Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 29),
                                    Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 34),
                                    Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 22),
                                    Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 14),
                                    Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 0)
                            )
                    )
            );
        });
        charger3Asset = assetStorageService.merge(charger3Asset);

        Asset charger4Asset = createDemoElectricityChargerAsset("Charger 4 Markthal", building3Asset, new GeoJSONPoint(4.486286, 51.919733));
        charger4Asset.getAttribute("power").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(0));
            assetAttribute.addMeta(
                    new MetaItem(
                            AGENT_LINK,
                            new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                    ),
                    new MetaItem(
                            SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                    ),
                    new MetaItem(
                            SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                            Values.createArray().addAll(
                                    Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 3),
                                    Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 17),
                                    Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 15),
                                    Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 8),
                                    Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 16),
                                    Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 4),
                                    Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 0),
                                    Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 15),
                                    Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 34),
                                    Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 30),
                                    Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 11),
                                    Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 16),
                                    Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 7),
                                    Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 4)
                            )
                    )
            );
        });
        charger4Asset = assetStorageService.merge(charger4Asset);

        // ### Erasmianum ###

        Asset building4Asset = new Asset("Erasmianum", BUILDING, energyManagement);
        building4Asset.setAttributes(
                new AssetAttribute(AttributeType.GEO_STREET, Values.create("Wytemaweg 25")),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3015 CN")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
        );
        building4Asset = assetStorageService.merge(building4Asset);

        Asset consumption4Asset = createDemoElectricityConsumerAsset("Consumption Erasmianum", building4Asset, new GeoJSONPoint(4.468324, 51.912062));
        consumption4Asset = assetStorageService.merge(consumption4Asset);

        // ### Oostelijk zwembad ###

        Asset building5Asset = new Asset("Oostelijk zwembad", BUILDING, energyManagement);
        building5Asset.setAttributes(
                new AssetAttribute(AttributeType.GEO_STREET, Values.create("Gerdesiaweg 480")),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3061 RA")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
        );
        building5Asset = assetStorageService.merge(building5Asset);

        Asset consumption5Asset = createDemoElectricityConsumerAsset("Consumption Zwembad", building5Asset, new GeoJSONPoint(4.498048, 51.925770));
        consumption5Asset = assetStorageService.merge(consumption5Asset);

        Asset production5Asset = createDemoElectricityProducerAsset("Solar Zwembad", building5Asset, new GeoJSONPoint(4.498281, 51.925507));
        production5Asset.getAttribute("totalPower").ifPresent(assetAttribute -> {
            assetAttribute.setValue(Values.create(5.13));
            assetAttribute.addMeta(STORE_DATA_POINTS);
        });
        production5Asset.getAttribute("totalEnergy").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(7.44)));
        production5Asset.getAttribute("installedCapacity").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(12.79)));
        production5Asset.getAttribute("systemEfficiency").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(86)));
        production5Asset.getAttribute("panelOrientation").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(ElectricityProducerOrientationType.SOUTH.name())));
        production5Asset = assetStorageService.merge(production5Asset);

        // ### Weather ###

        Asset weatherHttpApiAgent = new Asset("Weather Agent", AssetType.AGENT, energyManagement);
        weatherHttpApiAgent.addAttributes(
                initProtocolConfiguration(new AssetAttribute("weatherApiClient"), HttpClientProtocol.PROTOCOL_NAME)
                        .addMeta(
                                new MetaItem(META_PROTOCOL_BASE_URI, Values.create("https://api.openweathermap.org/data/2.5/")),
                                new MetaItem(META_QUERY_PARAMETERS, Values.createObject()
                                        .put("appid", "a6ea6724e5d116ea6d938bee2a8f4689")
                                        .put("lat", 51.918849)
                                        .put("lon", 4.463250)
                                        .put("units", "metric")),
                                new MetaItem(META_HEADERS, Values.createObject()
                                        .put("Accept", "application/json")
                                )
                        )
        );
        weatherHttpApiAgent = assetStorageService.merge(weatherHttpApiAgent);
        weatherHttpApiAgentId = weatherHttpApiAgent.getId();

        Asset weather = new Asset("Weather", WEATHER, energyManagement);
        weather.setId(UniqueIdentifierGenerator.generateId(weather.getName()));
        weather.addAttributes(
                new AssetAttribute("currentWeather", OBJECT)
                        .setMeta(
                                new MetaItem(
                                        AGENT_LINK,
                                        new AttributeRef(weatherHttpApiAgentId, "weatherApiClient").toArrayValue()),
                                new MetaItem(META_ATTRIBUTE_PATH, Values.create("weather")),
                                new MetaItem(META_ATTRIBUTE_POLLING_MILLIS, Values.create(halfHourInMillis)),
                                new MetaItem(LABEL, Values.create("Open Weather Map API weather end point")),
                                new MetaItem(READ_ONLY, Values.create(true)),
                                new MetaItem(ATTRIBUTE_LINK, createWeatherApiAttributeLink(weather.getId(), "main", "temp", "temperature")),
                                new MetaItem(ATTRIBUTE_LINK, createWeatherApiAttributeLink(weather.getId(), "main", "humidity", "humidity")),
                                new MetaItem(ATTRIBUTE_LINK, createWeatherApiAttributeLink(weather.getId(), "wind", "speed", "windSpeed")),
                                new MetaItem(ATTRIBUTE_LINK, createWeatherApiAttributeLink(weather.getId(), "wind", "deg", "windDirection"))
                        ));
                new AssetAttribute(AttributeType.LOCATION, new GeoJSONPoint(4.463250, 51.918849).toValue())
                        .addMeta(SHOW_ON_DASHBOARD);

        weather = assetStorageService.merge(weather);

        // ################################ Realm smartcity - Environment monitor ###################################

        Asset environmentMonitor = new Asset();
        environmentMonitor.setRealm(this.realmCityTenant);
        environmentMonitor.setName("Environment Monitor");
        environmentMonitor.setType(THING);
        environmentMonitor = assetStorageService.merge(environmentMonitor);

        Asset environment1Asset = createDemoEnvironmentAsset("Oudehaven", environmentMonitor, new GeoJSONPoint(4.49313, 51.91885),() -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        environment1Asset = assetStorageService.merge(environment1Asset);

        Asset environment2Asset = createDemoEnvironmentAsset("Kaappark", environmentMonitor, new GeoJSONPoint(4.480434, 51.899287),() -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        environment2Asset = assetStorageService.merge(environment2Asset);

        Asset environment3Asset = createDemoEnvironmentAsset("Museumpark", environmentMonitor, new GeoJSONPoint(4.472457, 51.912047 ),() -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        environment3Asset = assetStorageService.merge(environment3Asset);

        Asset environment4Asset = createDemoEnvironmentAsset("Eendrachtsplein", environmentMonitor, new GeoJSONPoint(4.473599, 51.916292),() -> new MetaItem[]{
                new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
                new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        environment4Asset = assetStorageService.merge(environment4Asset);

        Asset groundwater1Asset = createDemoGroundwaterAsset("Leuvehaven", environmentMonitor, new GeoJSONPoint(4.48413, 51.91431));
        Asset groundwater2Asset = createDemoGroundwaterAsset("Steiger", environmentMonitor, new GeoJSONPoint(4.482887, 51.920082));
        Asset groundwater3Asset = createDemoGroundwaterAsset("Stadhuis", environmentMonitor, new GeoJSONPoint(4.480876, 51.923212));

        Asset[] groundwaterArray = {groundwater1Asset, groundwater2Asset, groundwater3Asset};
        for (int i=0; i<groundwaterArray.length; i++){
            groundwaterArray[i].getAttribute("soilTemperature").ifPresent(assetAttribute -> {
                assetAttribute.addMeta(
                    new MetaItem(
                        AGENT_LINK,
                        new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                    ),
                    new MetaItem(
                        SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                    ),
                    new MetaItem(
                        SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                        Values.createArray().addAll(
                                Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 12.2),
                                Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 12.1),
                                Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 12.0),
                                Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 11.8),
                                Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 11.7),
                                Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 11.7),
                                Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 11.9),
                                Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 12.1),
                                Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 12.8),
                                Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 13.5),
                                Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 13.9),
                                Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 15.2),
                                Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 15.3),
                                Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 15.5),
                                Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 15.5),
                                Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 15.4),
                                Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 15.2),
                                Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 15.2),
                                Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 14.6),
                                Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 14.2),
                                Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 13.8),
                                Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 13.4),
                                Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 12.8),
                                Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 12.3)
                        )
                    )
                );
            });
            groundwaterArray[i].getAttribute("waterLevel").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(3.51)));
        }

        groundwater1Asset = assetStorageService.merge(groundwater1Asset);
        groundwater2Asset = assetStorageService.merge(groundwater2Asset);
        groundwater3Asset = assetStorageService.merge(groundwater3Asset);

        // ################################ Realm smartcity - Mobility and Safety ###################################

        Asset mobilityAndSafety = new Asset();
        mobilityAndSafety.setRealm(this.realmCityTenant);
        mobilityAndSafety.setName("Mobility and Safety");
        mobilityAndSafety.setType(THING);
        mobilityAndSafety = assetStorageService.merge(mobilityAndSafety);

        // ### Parking ###

        Asset parkingGroupAsset = new Asset("Parking group", GROUP, mobilityAndSafety);
        parkingGroupAsset.getAttribute("childAssetType").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create("urn:openremote:asset:parking")));
        parkingGroupAsset.addAttributes(
                new AssetAttribute("totalOccupancy", PERCENTAGE)
                    .addMeta(
                            LABEL.withInitialValue("Percentage of total parking spaces in use"),
                            RULE_STATE,
                            READ_ONLY,
                            STORE_DATA_POINTS));
        parkingGroupAsset = assetStorageService.merge(parkingGroupAsset);

        Asset parking1Asset = createDemoParkingAsset("Markthal", parkingGroupAsset, new GeoJSONPoint(4.48527, 51.91984));
        parking1Asset.getAttribute("occupiedSpaces").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem(
                    AGENT_LINK,
                    new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                ),
                new MetaItem(
                    SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                ),
                new MetaItem(
                    SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                    Values.createArray().addAll(
                            Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 34),
                            Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 37),
                            Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 31),
                            Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 36),
                            Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 32),
                            Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 39),
                            Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 47),
                            Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 53),
                            Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 165),
                            Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 301),
                            Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 417),
                            Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 442),
                            Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 489),
                            Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 467),
                            Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 490),
                            Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 438),
                            Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 457),
                            Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 402),
                            Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 379),
                            Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 336),
                            Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 257),
                            Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 204),
                            Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 112),
                            Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 75)
                    )
                )
            );
        });
        parking1Asset.getAttribute("priceHourly").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(3.75)));
        parking1Asset.getAttribute("priceDaily").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(25.00)));
        parking1Asset = assetStorageService.merge(parking1Asset);

        Asset parking2Asset = createDemoParkingAsset("Lijnbaan", parkingGroupAsset, new GeoJSONPoint(4.47681, 51.91849));
        parking2Asset.getAttribute("occupiedSpaces").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem(
                    AGENT_LINK,
                    new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                ),
                new MetaItem(
                    SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                ),
                new MetaItem(
                    SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                    Values.createArray().addAll(
                            Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 31),
                            Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 24),
                            Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 36),
                            Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 38),
                            Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 46),
                            Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 48),
                            Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 52),
                            Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 89),
                            Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 142),
                            Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 187),
                            Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 246),
                            Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 231),
                            Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 367),
                            Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 345),
                            Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 386),
                            Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 312),
                            Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 363),
                            Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 276),
                            Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 249),
                            Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 256),
                            Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 123),
                            Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 153),
                            Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 83),
                            Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 25)
                    )
                )
            );
        });
        parking2Asset.getAttribute("priceHourly").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(3.50)));
        parking2Asset.getAttribute("priceDaily").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(23.00)));
        parking2Asset = assetStorageService.merge(parking2Asset);

        Asset parking3Asset = createDemoParkingAsset("Erasmusbrug", parkingGroupAsset, new GeoJSONPoint(4.48207, 51.91127));
        parking3Asset.getAttribute("occupiedSpaces").ifPresent(assetAttribute -> {
            assetAttribute.addMeta(
                new MetaItem(
                    AGENT_LINK,
                    new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()
                ),
                new MetaItem(
                    SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(ReplaySimulatorElement.ELEMENT_NAME)
                ),
                new MetaItem(
                    SimulatorProtocol.REPLAY_ATTRIBUTE_LINK_DATA,
                    Values.createArray().addAll(
                            Values.createObject().put("timestamp", midnight.get(SECOND_OF_DAY)).put("value", 25),
                            Values.createObject().put("timestamp", midnight.plusHours(1).get(SECOND_OF_DAY)).put("value", 23),
                            Values.createObject().put("timestamp", midnight.plusHours(2).get(SECOND_OF_DAY)).put("value", 23),
                            Values.createObject().put("timestamp", midnight.plusHours(3).get(SECOND_OF_DAY)).put("value", 21),
                            Values.createObject().put("timestamp", midnight.plusHours(4).get(SECOND_OF_DAY)).put("value", 18),
                            Values.createObject().put("timestamp", midnight.plusHours(5).get(SECOND_OF_DAY)).put("value", 13),
                            Values.createObject().put("timestamp", midnight.plusHours(6).get(SECOND_OF_DAY)).put("value", 29),
                            Values.createObject().put("timestamp", midnight.plusHours(7).get(SECOND_OF_DAY)).put("value", 36),
                            Values.createObject().put("timestamp", midnight.plusHours(8).get(SECOND_OF_DAY)).put("value", 119),
                            Values.createObject().put("timestamp", midnight.plusHours(9).get(SECOND_OF_DAY)).put("value", 257),
                            Values.createObject().put("timestamp", midnight.plusHours(10).get(SECOND_OF_DAY)).put("value", 357),
                            Values.createObject().put("timestamp", midnight.plusHours(11).get(SECOND_OF_DAY)).put("value", 368),
                            Values.createObject().put("timestamp", midnight.plusHours(12).get(SECOND_OF_DAY)).put("value", 362),
                            Values.createObject().put("timestamp", midnight.plusHours(13).get(SECOND_OF_DAY)).put("value", 349),
                            Values.createObject().put("timestamp", midnight.plusHours(14).get(SECOND_OF_DAY)).put("value", 370),
                            Values.createObject().put("timestamp", midnight.plusHours(15).get(SECOND_OF_DAY)).put("value", 367),
                            Values.createObject().put("timestamp", midnight.plusHours(16).get(SECOND_OF_DAY)).put("value", 355),
                            Values.createObject().put("timestamp", midnight.plusHours(17).get(SECOND_OF_DAY)).put("value", 314),
                            Values.createObject().put("timestamp", midnight.plusHours(18).get(SECOND_OF_DAY)).put("value", 254),
                            Values.createObject().put("timestamp", midnight.plusHours(19).get(SECOND_OF_DAY)).put("value", 215),
                            Values.createObject().put("timestamp", midnight.plusHours(20).get(SECOND_OF_DAY)).put("value", 165),
                            Values.createObject().put("timestamp", midnight.plusHours(21).get(SECOND_OF_DAY)).put("value", 149),
                            Values.createObject().put("timestamp", midnight.plusHours(22).get(SECOND_OF_DAY)).put("value", 108),
                            Values.createObject().put("timestamp", midnight.plusHours(23).get(SECOND_OF_DAY)).put("value", 47)
                    )
                )
            );
        });
        parking3Asset.getAttribute("priceHourly").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(3.40)));
        parking3Asset.getAttribute("priceDaily").ifPresent(assetAttribute -> assetAttribute.setValue(Values.create(20.00)));
        parking3Asset = assetStorageService.merge(parking3Asset);

        // ### Crowd control ###

        Asset assetAreaStation = new Asset("Stationsplein", AREA, mobilityAndSafety)
            .setAttributes(
                new AssetAttribute(AttributeType.LOCATION, STATIONSPLEIN_LOCATION.toValue()),
                new AssetAttribute(AttributeType.GEO_POSTAL_CODE, Values.create("3013 AK")),
                new AssetAttribute(AttributeType.GEO_CITY, Values.create("Rotterdam")),
                new AssetAttribute(AttributeType.GEO_COUNTRY, Values.create("Netherlands"))
            );
        assetAreaStation = assetStorageService.merge(assetAreaStation);
        area1Id = assetAreaStation.getId();

        Asset peopleCounter1Asset = createDemoPeopleCounterAsset("People Counter South", assetAreaStation, new GeoJSONPoint(4.470147, 51.923171), () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        peopleCounter1Asset = assetStorageService.merge(peopleCounter1Asset);

        Asset peopleCounter2Asset = createDemoPeopleCounterAsset("People Counter North", assetAreaStation, new GeoJSONPoint(4.469329, 51.923700), () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        peopleCounter2Asset = assetStorageService.merge(peopleCounter2Asset);

        Asset microphone1Asset = createDemoMicrophoneAsset("Microphone South", assetAreaStation, new GeoJSONPoint(4.470362, 51.923201), () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        microphone1Asset = assetStorageService.merge(microphone1Asset);

        Asset microphone2Asset = createDemoMicrophoneAsset("Microphone North", assetAreaStation, new GeoJSONPoint(4.469190, 51.923786), () -> new MetaItem[]{
            new MetaItem(AGENT_LINK, new AttributeRef(smartcitySimulatorAgentId, "replaySimulator").toArrayValue()),
            new MetaItem(SimulatorProtocol.SIMULATOR_ELEMENT, Values.create(NumberSimulatorElement.ELEMENT_NAME))
        });
        microphone2Asset = assetStorageService.merge(microphone2Asset);

        Asset lightStation1Asset = createDemoLightAsset("Station Light NW", assetAreaStation, new GeoJSONPoint(4.468874, 51.923881));
        lightStation1Asset = assetStorageService.merge(lightStation1Asset);

        Asset lightStation2Asset = createDemoLightAsset("Station Light NE", assetAreaStation, new GeoJSONPoint(4.470539, 51.923991));
        lightStation2Asset = assetStorageService.merge(lightStation2Asset);

        Asset lightStation3Asset = createDemoLightAsset("Station Light S", assetAreaStation, new GeoJSONPoint(4.470558, 51.923186));
        lightStation3Asset = assetStorageService.merge(lightStation3Asset);

        // ### Lighting controller ###

        Asset lightingControllerOPAsset = createDemoLightControllerAsset("Lighting Noordereiland", mobilityAndSafety, new GeoJSONPoint(4.496177, 51.915060));
        lightingControllerOPAsset = assetStorageService.merge(lightingControllerOPAsset); 

        Asset lightOP1Asset = createDemoLightAsset("OnsPark1", lightingControllerOPAsset, new GeoJSONPoint(4.49626, 51.91516));
        lightOP1Asset = assetStorageService.merge(lightOP1Asset);

        Asset lightOP2Asset = createDemoLightAsset("OnsPark2", lightingControllerOPAsset, new GeoJSONPoint(4.49705, 51.91549));
        lightOP2Asset = assetStorageService.merge(lightOP2Asset);

        Asset lightOP3Asset = createDemoLightAsset("OnsPark3", lightingControllerOPAsset, new GeoJSONPoint(4.49661, 51.91495));
        lightOP3Asset = assetStorageService.merge(lightOP3Asset);

        Asset lightOP4Asset = createDemoLightAsset("OnsPark4", lightingControllerOPAsset, new GeoJSONPoint(4.49704, 51.91520));
        lightOP4Asset = assetStorageService.merge(lightOP4Asset);

        Asset lightOP5Asset = createDemoLightAsset("OnsPark5", lightingControllerOPAsset, new GeoJSONPoint(4.49758, 51.91440));
        lightOP5Asset = assetStorageService.merge(lightOP5Asset);

        Asset lightOP6Asset = createDemoLightAsset("OnsPark6", lightingControllerOPAsset, new GeoJSONPoint(4.49786, 51.91452));
        lightOP6Asset = assetStorageService.merge(lightOP6Asset);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    protected static Value createWeatherApiAttributeLink(String assetId, String jsonParentName, String jsonName, String parameter) {
        return Values.convertToValue(new AttributeLink(
                new AttributeRef(assetId, parameter),
                null,
                new ValueFilter[]{
                        new JsonPathFilter("$." + jsonParentName + "." + jsonName, true, false),
                }
        ), Container.JSON.writer()).get();
    }
}
