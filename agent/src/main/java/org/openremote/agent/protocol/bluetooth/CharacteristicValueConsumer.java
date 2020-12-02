package org.openremote.agent.protocol.bluetooth;

import org.openremote.model.value.Value;

import java.util.UUID;
import java.util.function.Consumer;

public class CharacteristicValueConsumer {
    public final String subscriberId;
    public final UUID serviceUuid;
    public final UUID charUuid;
    private final Consumer<Value> consumer;

    public CharacteristicValueConsumer(String subscriberId, UUID serviceUuid, UUID charUuid, Consumer<Value> consumer) {
        this.subscriberId = subscriberId;
        this.serviceUuid = serviceUuid;
        this.charUuid = charUuid;
        this.consumer = consumer;
    }

    public void send(Value value) {
        this.consumer.accept(value);
    }
}
