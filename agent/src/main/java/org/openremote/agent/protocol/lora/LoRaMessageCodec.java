package org.openremote.agent.protocol.lora;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.openremote.model.syslog.SyslogCategory;

import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

public class LoRaMessageCodec extends ByteToMessageCodec<LoRaMessage> {
    private final ObjectMapper mapper;

    public static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, LoRaMessageCodec.class);

    public LoRaMessageCodec(ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
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
            case CONFIGURE_OK:
                out.add(this.mapper.treeToValue(tree, LoRaMessage.class));
                break;
            case RECEIVED_MESSAGE:
                out.add(this.mapper.treeToValue(tree, LoRaReceivedMessage.class));
                break;
            case CONFIGURE:
                LOG.warning(String.format("Received send-only message from LoRa driver daemon: %s", type));
                break;
            default:
                LOG.warning(String.format("Received unknown message from LoRa driver daemon: %s", type));
        }
    }

    static class RadioMessageCodecException extends Exception {

        public RadioMessageCodecException(String msg) {
            super(msg);
        }
    }
}
