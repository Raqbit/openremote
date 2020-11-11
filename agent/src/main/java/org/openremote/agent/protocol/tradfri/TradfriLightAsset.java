package org.openremote.agent.protocol.tradfri;

import org.openremote.agent.protocol.ProtocolAssetService;
import org.openremote.agent.protocol.tradfri.device.Light;
import org.openremote.agent.protocol.tradfri.device.event.*;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.asset.AssetType;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.Values;

import java.util.Optional;

import static org.openremote.model.attribute.AttributeValueType.NUMBER;
import static org.openremote.model.attribute.MetaItemType.*;

public class TradfriLightAsset extends TradfriAsset {

    /**
     * Construct the TradfriLightAsset class.
     * @param parentId parent id.
     * @param agentLink the agent link.
     * @param light the light.
     * @param assetService the asset service.
     */
    public TradfriLightAsset(String parentId, MetaItem agentLink, Light light, ProtocolAssetService assetService) {
        super(parentId, light, AssetType.LIGHT, assetService, agentLink);
    }

    /**
     * Method to create the asset attributes
     */
    @Override
    public void createAttributes() {
        Optional<Attribute<?>> lightDimLevelOptional = getAttribute("lightDimLevel");
        if (lightDimLevelOptional.isPresent()) {
            Attribute<?> lightDimLevel = lightDimLevelOptional.get();
            lightDimLevel.setType(NUMBER);
            lightDimLevel.addMeta(
                    new MetaItem<>(RANGE_MIN, 0),
                    new MetaItem<>(RANGE_MAX, 254),
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    agentLink
            );
            lightDimLevel.setDescription("The brightness (0 - 254) of the TRÅDFRI light (Only for dimmable lights)");
            lightDimLevel.setReadOnly(false);
        }

        Optional<Attribute<?>> lightStatusOptional = getAttribute("lightStatus");
        if(lightStatusOptional.isPresent()){
            Attribute<?> lightStatus = lightStatusOptional.get();
            lightStatus.addMeta(
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    agentLink
            );
            lightStatus.setDescription("The state of the TRÅDFRI light (Checked means on, unchecked means off)");
            lightStatus.setReadOnly(false);
        }

        Optional<Attribute<?>> colorGBWOptional = getAttribute("colorGBW");
        if(colorGBWOptional.isPresent()){
            Attribute<?> colorGBW = colorGBWOptional.get();
            colorGBW.addMeta(
                    new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                    new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                    agentLink
            );
            colorGBW.setDescription("The color of the TRÅDFRI light (Only for RGB lights)");
            colorGBW.setReadOnly(false);
        }

        Attribute<?> colorTemperature = new Attribute<>("colorTemperature", NUMBER, 0);
        colorTemperature.addMeta(
                new MetaItem<>(RANGE_MIN, 250),
                new MetaItem<>(RANGE_MAX, 454),
                new MetaItem<>(LABEL, "Color Temperature"),
                new MetaItem<>(DESCRIPTION, Values.create("The color temperature (250 - 454) of the TRÅDFRI light (Only for white spectrum lights)")),
                new MetaItem<>(ACCESS_RESTRICTED_READ, true),
                new MetaItem<>(ACCESS_RESTRICTED_WRITE, true),
                new MetaItem<>(READ_ONLY, false),
                agentLink
        );
        addAttributes(colorTemperature);
    }

    /**
     * Method to create the event handlers
     */
    @Override
    public void createEventHandlers() {
        Asset asset = this;
        EventHandler<LightChangeOnEvent> lightOnOffEventHandler = new EventHandler<LightChangeOnEvent>() {
            @Override
            public void handle(LightChangeOnEvent event) {
                Optional<Attribute<?>> lightStatus = getAttribute("lightStatus");
                Light light = device.toLight();
                if(lightStatus.isPresent() && light.getOn() != null) lightStatus.get().setValue(Values.create(light.getOn()));
                assetService.mergeAsset(asset);
            }
        };

        EventHandler<LightChangeBrightnessEvent> lightBrightnessEventHandler = new EventHandler<LightChangeBrightnessEvent>() {
            @Override
            public void handle(LightChangeBrightnessEvent event) {
                Optional<Attribute<?>> lightDimLevel = getAttribute("lightDimLevel");
                Light light = device.toLight();
                if(lightDimLevel.isPresent() && light.getBrightness() != null) lightDimLevel.get().setValue(Values.create(light.getBrightness()));
                assetService.mergeAsset(asset);
            }
        };

        EventHandler<LightChangeColourEvent> lightColourChangeEventHandler = new EventHandler<LightChangeColourEvent>() {
            @Override
            public void handle(LightChangeColourEvent event) {
                Optional<Attribute<?>> colorGBW = getAttribute("colorGBW");
                Light light = device.toLight();
                if(colorGBW.isPresent() && light.getColourRGB() != null) colorGBW.get().setValue(Values.createObject().put("red", light.getColourRGB().getRed()).put("green", light.getColourRGB().getGreen()).put("blue", light.getColourRGB().getBlue()));
                assetService.mergeAsset(asset);
            }
        };

        EventHandler<LightChangeColourTemperatureEvent> lightColorTemperatureEventHandler = new EventHandler<LightChangeColourTemperatureEvent>() {
            @Override
            public void handle(LightChangeColourTemperatureEvent event) {
                Optional<Attribute<?>> colorTemperature = getAttribute("colorTemperature");
                Light light = device.toLight();
                if(colorTemperature.isPresent() && light.getColourTemperature() != null) colorTemperature.get().setValue(Values.create(light.getColourTemperature()));
                assetService.mergeAsset(asset);
            }
        };
        Light light = device.toLight();
        light.addEventHandler(lightOnOffEventHandler);
        light.addEventHandler(lightBrightnessEventHandler);
        light.addEventHandler(lightColourChangeEventHandler);
        light.addEventHandler(lightColorTemperatureEventHandler);
    }

    /**
     * Method to set the initial values
     */
    @Override
    public void setInitialValues() {
        Light light = device.toLight();

        Optional<Attribute<?>> lightStatus = getAttribute("lightStatus");
        if(lightStatus.isPresent() && light.getOn() != null) lightStatus.get().setValue(Values.create(light.getOn()));

        Optional<Attribute<?>> lightDimLevel = getAttribute("lightDimLevel");
        if(lightDimLevel.isPresent() && light.getBrightness() != null) lightDimLevel.get().setValue(Values.create(light.getBrightness()));

        Optional<Attribute<?>> colorGBW = getAttribute("colorGBW");
        if(colorGBW.isPresent() && light.getColourRGB() != null) colorGBW.get().setValue(Values.createObject().put("red", light.getColourRGB().getRed()).put("green", light.getColourRGB().getGreen()).put("blue", light.getColourRGB().getBlue()));

        Optional<Attribute<?>> colorTemperature = getAttribute("colorTemperature");
        if(colorTemperature.isPresent() && light.getColourTemperature() != null) colorTemperature.get().setValue(Values.create(light.getColourTemperature()));
    }
}
