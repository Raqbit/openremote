package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LoRaReceivedMessage extends LoRaMessage {
    @JsonProperty("from")
    public int fromId;

    @JsonProperty("data")
    public Map<String, Object> data;

    public LoRaReceivedMessage() {
        super(Type.CONFIGURE);
    }
}
