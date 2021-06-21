package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigureLoRaMessage extends LoRaMessage {
    @JsonProperty("frequency")
    public float frequency;

    @JsonProperty("node_id")
    public int nodeId;

    public ConfigureLoRaMessage(float frequency, int nodeId) {
        super(Type.CONFIGURE);
        this.frequency = frequency;
        this.nodeId = nodeId;
    }
}
