package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.OutputStream;
import java.util.List;

public class LoRaMessageCodec extends ByteToMessageCodec<LoRaMessage> {
    private final ObjectMapper mapper;

    public LoRaMessageCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, LoRaMessage msg, ByteBuf out) throws Exception {
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(out);

        if (msg.type != LoRaMessage.Type.CONFIGURE) {
            throw new RadioMessageCodecException("should not send this message");
        }

        mapper.writeValue((OutputStream) byteBufOutputStream, msg);
        out.writeByte('\n');
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBufInputStream byteBufInputStream = new ByteBufInputStream(in);

        JsonNode tree = this.mapper.readTree(byteBufInputStream);

        LoRaMessage.Type type = mapper.treeToValue(tree.get("type"), LoRaMessage.Type.class);

        // No type, do not attempt decode
        if (type == null) {
            return;
        }

        switch (type) {
            case HANDSHAKE:
            case HANDSHAKE_OK:
                out.add(this.mapper.treeToValue(tree, LoRaMessage.class));
                break;
            case RECEIVED_MESSAGE:
                out.add(this.mapper.treeToValue(tree, LoRaReceivedMessage.class));
                break;
            case CONFIGURE:
                throw new RadioMessageCodecException("should not receive this message");
        }
    }

    static class RadioMessageCodecException extends Exception {

        public RadioMessageCodecException(String msg) {
            super(msg);
        }
    }
}
