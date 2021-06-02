package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.openremote.agent.protocol.io.AbstractNettyIOClient;
import org.openremote.agent.protocol.tcp.AbstractTCPClientProtocol;
import org.openremote.agent.protocol.tcp.TCPIOClient;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.util.Pair;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LoRaProtocol extends AbstractTCPClientProtocol<LoRaProtocol, LoRaAgent, LoRaAgent.LoRaAgentLink, LoRaMessage, TCPIOClient<LoRaMessage>> {

    public static final String PROTOCOL_DISPLAY_NAME = "LoRa";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9230;
    public static final int DEFAULT_NODE_ID = 1;
    private static final int DEFAULT_FREQUENCY = 868;

    private final ObjectMapper mapper = new ObjectMapper();

    private final int nodeId;
    private final int frequency;

    protected final Multimap<Integer, Pair<AttributeRef, Consumer<Map<String, Object>>>> attrLinks = HashMultimap.create();


    public LoRaProtocol(LoRaAgent agent) {
        super(agent);
        this.nodeId = agent.getNodeId().orElse(DEFAULT_NODE_ID);
        this.frequency = agent.getFrequency().orElse(DEFAULT_FREQUENCY);
    }


    @Override
    protected void doLinkAttribute(String assetId, Attribute<?> attribute, LoRaAgent.LoRaAgentLink agentLink) throws RuntimeException {
        attrLinks.put(
                agentLink.fromId,
                new Pair<>(
                        new AttributeRef(assetId, attribute.getName()),
                        radioMessage -> this.updateLinkedAttribute(new AttributeState(assetId, attribute.getName(), radioMessage))
                )
        );
    }

    @Override
    protected void doUnlinkAttribute(String assetId, Attribute<?> attribute, LoRaAgent.LoRaAgentLink agentLink) {
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());
        attrLinks.entries().removeIf(consumer -> consumer.getValue().key.equals(attributeRef));
    }

    @Override
    protected void doLinkedAttributeWrite(Attribute<?> attribute, LoRaAgent.LoRaAgentLink agentLink, AttributeEvent event, Object processedValue) {
        // We don't support this
    }

    @Override
    protected Supplier<ChannelHandler[]> getEncoderDecoderProvider() {
        return () -> new ChannelHandler[]{
                new LineBasedFrameDecoder(Integer.MAX_VALUE),
                new LoRaMessageCodec(this.mapper),
                new LoRaMessageCodec(this.mapper),
                new AbstractNettyIOClient.MessageToMessageDecoder<>(LoRaMessage.class, this.client.ioClient)
        };
    }

    @Override
    protected void onMessageReceived(LoRaMessage message) {
        switch (message.type) {
            case HANDSHAKE:
                // Respond with configuration message
                this.client.ioClient.sendMessage(new ConfigureLoRaMessage(this.frequency, this.nodeId));
                this.setConnectionStatus(ConnectionStatus.WAITING);
                break;
            case CONFIGURE_OK:
                // Handshake complete, we are connected
                this.setConnectionStatus(ConnectionStatus.CONNECTED);
                break;
            case RECEIVED_MESSAGE:
                for (Pair<AttributeRef, Consumer<Map<String, Object>>> consumer : attrLinks.get(((LoRaReceivedMessage) message).fromId)) {
                    consumer.value.accept(((LoRaReceivedMessage) message).data);
                }
                break;
            default:
                LOG.warning("Unhandled decoded LoRa message ");
        }
    }

    @Override
    protected LoRaMessage createWriteMessage(Attribute<?> attribute, LoRaAgent.LoRaAgentLink agentLink, AttributeEvent event, Object processedValue) {
        return null;
    }


    @Override
    public String getProtocolName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    @Override
    protected TCPIOClient<LoRaMessage> doCreateIoClient() {
        String host = agent.getAttributes().getValue(Agent.HOST).orElse(DEFAULT_HOST);
        int port = agent.getAttributes().getValue(Agent.PORT).orElse(DEFAULT_PORT);

        return new TCPIOClient<>(host, port);
    }
}
