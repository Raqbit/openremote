package org.openremote.agent.protocol.tradfri;

import org.openremote.agent.protocol.tradfri.device.Device;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.AttributeList;
import org.openremote.model.v2.AttributeDescriptor;
import org.openremote.model.v2.ValueType;

import java.util.Optional;
import java.util.function.Consumer;

public interface TradfriAsset {

    AttributeDescriptor<Integer> DEVICE_ID = new AttributeDescriptor<>("deviceId", false, ValueType.INTEGER);

    default Optional<Integer> getDeviceId() {
        return getAttributes().getValue(DEVICE_ID);
    }

    default void setDeviceId(Integer deviceId) {
        getAttributes().get(DEVICE_ID).orElse(new Attribute<>(DEVICE_ID)).setValue(deviceId);
    }

    String getId();

    AttributeList getAttributes();

    void addEventHandlers(Device device, Consumer<AttributeEvent> attributeEventConsumer);

    void initialiseAttributes(Device device);
}
