package org.openremote.agent.protocol.lora;

import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueConstraint;
import org.openremote.model.value.ValueType;

import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Optional;

@Entity
public class LoRaAgent extends IoAgent<LoRaAgent, LoRaProtocol, LoRaAgent.RadioAgentLink> {

    public static class RadioAgentLink extends AgentLink<RadioAgentLink> {
        @Min(1)
        @Max(245)
        protected int fromId;
    }

    public static final AgentDescriptor<LoRaAgent, LoRaProtocol, RadioAgentLink> DESCRIPTOR = new AgentDescriptor<>(
            LoRaAgent.class, LoRaProtocol.class, RadioAgentLink.class
    );

    public static final AttributeDescriptor<Integer> FREQUENCY = new AttributeDescriptor<>("frequency", ValueType.INTEGER)
            .withUnits("Hz")
            .withConstraints(
                    new ValueConstraint.NotEmpty()
            );
    public static final AttributeDescriptor<Integer> NODE_ID = new AttributeDescriptor<>("nodeId", ValueType.INTEGER)
            .withConstraints(
                    new ValueConstraint.Min(1),
                    new ValueConstraint.Max(254),
                    new ValueConstraint.NotEmpty()
            );

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    protected LoRaAgent() {
    }

    @SuppressWarnings("unused")
    public LoRaAgent(String name) {
        super(name);
    }

    public Optional<Integer> getFrequency() {
        return getAttributes().getValue(FREQUENCY);
    }

    public Optional<Integer> getNodeId() {
        return getAttributes().getValue(NODE_ID);
    }

    @Override
    public LoRaProtocol getProtocolInstance() {
        return new LoRaProtocol(this);
    }
}
