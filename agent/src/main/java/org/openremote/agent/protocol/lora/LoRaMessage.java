package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoRaMessage {

    @JsonProperty("type")
    public Type type;

    public LoRaMessage(Type type) {
        this.type = type;
    }

    @SuppressWarnings("unused")
    public LoRaMessage() {

    }

    public enum Type {
        @JsonProperty("HANDSHAKE") HANDSHAKE,
        @JsonProperty("CONFIGURE") CONFIGURE,
        @JsonProperty("CONFIGURE_OK") CONFIGURE_OK,
        @JsonProperty("RECEIVED_MESSAGE") RECEIVED_MESSAGE,
        @JsonEnumDefaultValue() UNKNOWN;
    }
}
