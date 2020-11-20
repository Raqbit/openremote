/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.agent.protocol.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.openremote.agent.protocol.AbstractProtocol;
import org.openremote.model.Container;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.protocol.ProtocolUtil;
import org.openremote.model.syslog.SyslogCategory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.openremote.model.syslog.SyslogCategory.PROTOCOL;

/**
 * This is an abstract {@link Protocol} for protocols that require an {@link IoClient}.
 */
public abstract class AbstractIoClientProtocol<T, U extends IoClient<T>, V extends IoAgent<T, U>> extends AbstractProtocol<V> {

    /**
     * Supplies a set of encoders/decoders that convert from/to {@link String} to/from {@link ByteBuf} based on the generic protocol {@link Attribute}s
     */
    public static Supplier<ChannelHandler[]> getGenericStringEncodersAndDecoders(AbstractNettyIoClient<String, ?> client, Agent agent) {

        boolean hexMode = agent.getAttributes().getValue(Agent.MESSAGE_CONVERT_HEX).orElse(false);
        boolean binaryMode = agent.getAttributes().getValue(Agent.MESSAGE_CONVERT_BINARY).orElse(false);
        Charset charset = agent.getAttributes().getValue(Agent.MESSAGE_CHARSET).map(Charset::forName).orElse(CharsetUtil.UTF_8);
        int maxLength = agent.getAttributes().getValue(Agent.MESSAGE_MAX_LENGTH).orElse(Integer.MAX_VALUE);
        String[] delimiters = agent.getAttributes().getValue(Agent.MESSAGE_DELIMITERS).orElse(new String[0]);
        boolean stripDelimiter = agent.getAttributes().getValue(Agent.MESSAGE_STRIP_DELIMITER).orElse(false);

        return () -> {
            List<ChannelHandler> encodersDecoders = new ArrayList<>();

            if (hexMode || binaryMode) {
                encodersDecoders.add(
                    new AbstractNettyIoClient.MessageToByteEncoder<>(
                        String.class,
                        client,
                        (msg, out) -> {
                            byte[] bytes = hexMode ? ProtocolUtil.bytesFromHexString(msg) : ProtocolUtil.bytesFromBinaryString(msg);
                            out.writeBytes(bytes);
                        }
                    ));

                if (delimiters.length > 0) {
                    ByteBuf[] byteDelimiters = Arrays.stream(delimiters)
                        .map(delim -> Unpooled.wrappedBuffer(hexMode ? ProtocolUtil.bytesFromHexString(delim) : ProtocolUtil.bytesFromBinaryString(delim)))
                        .toArray(ByteBuf[]::new);
                    encodersDecoders.add(new DelimiterBasedFrameDecoder(maxLength, stripDelimiter, byteDelimiters));
                } else {
                    encodersDecoders.add(new FixedLengthFrameDecoder(maxLength));
                }

                // Incoming messages will be bytes
                encodersDecoders.add(
                    new AbstractNettyIoClient.ByteToMessageDecoder<>(
                        client,
                        (byteBuf, messages) -> {
                            byte[] bytes = new byte[byteBuf.readableBytes()];
                            byteBuf.readBytes(bytes);
                            String msg = hexMode ? ProtocolUtil.bytesToHexString(bytes) : ProtocolUtil.bytesToBinaryString(bytes);
                            messages.add(msg);
                        }
                    )
                );
            } else {
                encodersDecoders.add(new StringEncoder(charset));
                if (delimiters.length > 0) {
                    ByteBuf[] byteDelimiters = Arrays.stream(delimiters)
                        .map(delim -> Unpooled.wrappedBuffer(delim.getBytes(charset)))
                        .toArray(ByteBuf[]::new);
                    encodersDecoders.add(new DelimiterBasedFrameDecoder(maxLength, stripDelimiter, byteDelimiters));
                } else {
                    encodersDecoders.add(new FixedLengthFrameDecoder(maxLength));
                }
                encodersDecoders.add(new StringDecoder(charset));
                encodersDecoders.add(new AbstractNettyIoClient.MessageToMessageDecoder<>(String.class, client));
            }

            return encodersDecoders.toArray(new ChannelHandler[0]);
        };
    }

    public static final Logger LOG = SyslogCategory.getLogger(PROTOCOL, AbstractIoClientProtocol.class);
    protected ProtocolIoClient<T, U> client;

    protected AbstractIoClientProtocol(V agent) {
        super(agent);
    }

    @Override
    public String getProtocolInstanceUri() {
        return client != null ? client.ioClient.getClientUri() : "";
    }

    @Override
    protected void doStop(Container container) throws Exception {
        if (client != null) {
            LOG.fine("Stopping IO client for protocol: " + this);
            client.disconnect();
        }
        client = null;
    }

    @Override
    protected void doStart(Container container) throws Exception {
        try {
            client = createIoClient(getAgent());
            LOG.fine("Created IO client '" + client.ioClient.getClientUri() + "' for protocol: " + this);
            client.connect();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create IO client for protocol: " + this, e);
            setConnectionStatus(ConnectionStatus.ERROR);
        }
    }

    @Override
    protected void doLinkedAttributeWrite(Attribute<?> attribute, AttributeEvent event, Object processedValue) {

        if (client == null || attribute == null) {
            return;
        }

        T message = createWriteMessage(attribute, event, processedValue);

        if (message == null) {
            LOG.fine("No message produced for attribute event so not sending to IO client '" + client.ioClient.getClientUri() + "': " + event);
            return;
        }

        client.send(message);
    }

    protected ProtocolIoClient<T, U> createIoClient(V agent) throws Exception {
        U client = doCreateIoClient();
        Supplier<ChannelHandler[]> encoderDecoderProvider = getEncoderDecoderProvider();
        client.setEncoderDecoderProvider(encoderDecoderProvider);
        return new ProtocolIoClient<>(client, this::onConnectionStatusChanged, this::onMessageReceived);
    }

    /**
     * Called when the {@link IoClient} {@link ConnectionStatus} changes
     */
    protected void onConnectionStatusChanged(ConnectionStatus connectionStatus) {
        setConnectionStatus(connectionStatus);
    }

    /**
     * Should return an instance of {@link IoClient} for the linked {@link Agent}; the configuration of
     * encoders/decoders is handled by the separate call to {@link #getEncoderDecoderProvider}
     */
    protected abstract U doCreateIoClient() throws Exception;

    protected abstract Supplier<ChannelHandler[]> getEncoderDecoderProvider();

    /**
     * Called when the {@link IoClient} receives a message from the server
     */
    protected abstract void onMessageReceived(T message);

    /**
     * Generate the actual message to send to the {@link IoClient} for this {@link AttributeEvent}
     */
    protected abstract T createWriteMessage(Attribute<?> attribute, AttributeEvent event, Object processedValue);
}
