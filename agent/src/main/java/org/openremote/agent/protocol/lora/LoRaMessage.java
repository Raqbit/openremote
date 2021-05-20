package org.openremote.agent.protocol.lora;

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
        @JsonProperty("handshake") HANDSHAKE,
        @JsonProperty("configure") CONFIGURE,
        @JsonProperty("handshake_ok") HANDSHAKE_OK,
        @JsonProperty("received_msg") RECEIVED_MESSAGE;
    }
}
