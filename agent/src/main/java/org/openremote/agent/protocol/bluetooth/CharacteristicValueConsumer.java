package org.openremote.agent.protocol.bluetooth;

import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;

import java.util.UUID;
import java.util.function.Consumer;

public class CharacteristicValueConsumer {
    public final String subscriberId;
    public final UUID charUuid;
    public final ValueType targetType;
    public final UUID serviceUuid;
    private final Consumer<Value> consumer;

    public CharacteristicValueConsumer(String subscriberId, ValueType targetType, UUID serviceUuid, UUID charUuid, Consumer<Value> consumer) {
        this.subscriberId = subscriberId;
        this.targetType = targetType;
        this.serviceUuid = serviceUuid;
        this.charUuid = charUuid;
        this.consumer = consumer;
    }

    public void send(Value value) {
        this.consumer.accept(value);
    }
}
