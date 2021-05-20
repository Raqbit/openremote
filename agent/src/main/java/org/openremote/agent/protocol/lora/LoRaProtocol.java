package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import org.openremote.agent.protocol.io.AbstractNettyIoClient;
import org.openremote.agent.protocol.tcp.AbstractTcpClientProtocol;
import org.openremote.agent.protocol.tcp.TcpIoClient;
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

public class LoRaProtocol extends AbstractTcpClientProtocol<LoRaProtocol, LoRaAgent, LoRaAgent.RadioAgentLink, LoRaMessage, TcpIoClient<LoRaMessage>> {

    public static final String PROTOCOL_DISPLAY_NAME = "LoRa";

    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 9230;
    public static final int DEFAULT_NODE_ID = 1;
    private static final int DEFAULT_FREQUENCY = 915;

    private final ObjectMapper mapper = new ObjectMapper();

    private final int nodeId;
    private final int frequency;

    protected final Multimap<Integer, Pair<AttributeRef, Consumer<Map<String, Object>>>> nodeIdLinks = HashMultimap.create();


    public LoRaProtocol(LoRaAgent agent) {
        super(agent);
        this.nodeId = agent.getNodeId().orElse(DEFAULT_NODE_ID);
        this.frequency = agent.getFrequency().orElse(DEFAULT_FREQUENCY);
    }


    @Override
    protected void doLinkAttribute(String assetId, Attribute<?> attribute, LoRaAgent.RadioAgentLink agentLink) throws RuntimeException {
        nodeIdLinks.put(
                agentLink.fromId,
                new Pair<>(
                        new AttributeRef(assetId, attribute.getName()),
                        radioMessage -> this.updateLinkedAttribute(new AttributeState(assetId, attribute.getName(), radioMessage))
                )
        );
    }

    @Override
    protected void doUnlinkAttribute(String assetId, Attribute<?> attribute, LoRaAgent.RadioAgentLink agentLink) {
        AttributeRef attributeRef = new AttributeRef(assetId, attribute.getName());
        nodeIdLinks.entries().removeIf(consumer -> consumer.getValue().key.equals(attributeRef));
    }

    @Override
    protected void doLinkedAttributeWrite(Attribute<?> attribute, LoRaAgent.RadioAgentLink agentLink, AttributeEvent event, Object processedValue) {
        // We don't support this
    }

    @Override
    protected Supplier<ChannelHandler[]> getEncoderDecoderProvider() {
        return () -> new ChannelHandler[]{
                new LineBasedFrameDecoder(Integer.MAX_VALUE),
                new LoRaMessageCodec(this.mapper),
                new LoRaMessageCodec(this.mapper),
                new AbstractNettyIoClient.MessageToMessageDecoder<>(LoRaMessage.class, this.client.ioClient)
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
            case HANDSHAKE_OK:
                // Handshake complete, we are connected
                this.setConnectionStatus(ConnectionStatus.CONNECTED);
                break;
            case RECEIVED_MESSAGE:
                for (Pair<AttributeRef, Consumer<Map<String, Object>>> consumer : nodeIdLinks.get(((LoRaReceivedMessage) message).fromId)) {
                    consumer.value.accept(((LoRaReceivedMessage) message).data);
                }
                break;
        }
    }

    @Override
    protected LoRaMessage createWriteMessage(Attribute<?> attribute, LoRaAgent.RadioAgentLink agentLink, AttributeEvent event, Object processedValue) {
        return null;
    }


    @Override
    public String getProtocolName() {
        return PROTOCOL_DISPLAY_NAME;
    }

    @Override
    protected TcpIoClient<LoRaMessage> doCreateIoClient() {
        String host = agent.getAttributes().getValue(Agent.HOST).orElse(DEFAULT_HOST);
        int port = agent.getAttributes().getValue(Agent.PORT).orElse(DEFAULT_PORT);

        return new TcpIoClient<>(host, port);
    }
}
