package org.openremote.agent.protocol.tradfri;

import org.openremote.agent.protocol.tradfri.device.Device;
import org.openremote.agent.protocol.tradfri.device.Plug;
import org.openremote.agent.protocol.tradfri.device.event.EventHandler;
import org.openremote.agent.protocol.tradfri.device.event.PlugChangeOnEvent;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.AttributeEvent;

import java.util.function.Consumer;

public class TradfriPlugAsset extends org.openremote.model.asset.impl.Plug implements TradfriAsset {

    public TradfriPlugAsset(String name) {
        this(name, DESCRIPTOR);
    }

    protected <T extends TradfriPlugAsset> TradfriPlugAsset(String name, AssetDescriptor<T> descriptor) {
        super(name, descriptor);
    }

    @Override
    public void addEventHandlers(Device device, Consumer<AttributeEvent> attributeEventConsumer) {
        Plug plug = device.toPlug();
        if (plug == null) {
            return;
        }

        EventHandler<PlugChangeOnEvent> plugOnOffEventHandler = new EventHandler<PlugChangeOnEvent>() {
            @Override
            public void handle(PlugChangeOnEvent event) {
                attributeEventConsumer.accept(new AttributeEvent(getId(), ON_OFF.getName(), plug.getOn()));
            }
        };
        device.addEventHandler(plugOnOffEventHandler);
    }

    @Override
    public void initialiseAttributes(Device device) {
        Plug plug = device.toPlug();
        if (plug == null) {
            return;
        }

        getAttributes().get(ON_OFF).ifPresent(attribute -> {
            attribute.setValue(plug.getOn());
        });
    }
}
