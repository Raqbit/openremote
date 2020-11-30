/*
 * Copyright 2019, OpenRemote Inc.
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
package org.openremote.test.protocol.udp

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.DatagramChannel
import io.netty.handler.codec.FixedLengthFrameDecoder
import io.netty.handler.codec.MessageToMessageEncoder
import io.netty.handler.codec.bytes.ByteArrayDecoder
import org.openremote.agent.protocol.udp.UdpClientAgent
import org.openremote.model.asset.agent.AgentLink
import org.openremote.model.asset.agent.Protocol
import org.openremote.agent.protocol.ProtocolExecutorService
import org.openremote.agent.protocol.udp.AbstractUdpServer
import org.openremote.agent.protocol.udp.UdpClientProtocol
import org.openremote.agent.protocol.udp.UdpStringServer
import org.openremote.manager.agent.AgentService
import org.openremote.manager.asset.AssetProcessingService
import org.openremote.manager.asset.AssetStorageService
import org.openremote.model.Constants
import org.openremote.model.asset.Asset
import org.openremote.model.asset.agent.ConnectionStatus
import org.openremote.model.asset.impl.ThingAsset
import org.openremote.model.attribute.*
import org.openremote.model.query.AssetQuery
import org.openremote.model.query.filter.StringPredicate
import org.openremote.model.value.MetaItemType
import org.openremote.model.value.ValueType
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class UdpClientProtocolTest extends Specification implements ManagerContainerTrait {

    def "Check UDP client protocol configuration and linked attribute deployment"() {

        given: "expected conditions"
        def conditions = new PollingConditions(timeout: 10, delay: 0.2)

        and: "the container starts"
        def container = startContainer(defaultConfig(), defaultServices())
        def protocolExecutorService = container.getService(ProtocolExecutorService.class)
        def assetStorageService = container.getService(AssetStorageService.class)
        def assetProcessingService = container.getService(AssetProcessingService.class)
        def agentService = container.getService(AgentService.class)

        expect: "the system settles down"
        conditions.eventually {
            assert noEventProcessedIn(assetProcessingService, 300)
        }

        when: "a simple UDP echo server is started"
        def echoServerPort = findEphemeralPort()
        def clientPort = findEphemeralPort()
        AbstractUdpServer echoServer = new UdpStringServer(protocolExecutorService, new InetSocketAddress("127.0.0.1", echoServerPort), ";", Integer.MAX_VALUE, true)
        def echoSkipCount = 0
        def clientActualPort = null
        def lastCommand = null
        def lastSend = null
        def receivedMessages = []
        echoServer.addMessageConsumer({
            message, channel, sender ->
                clientActualPort = sender.port
                lastCommand = message
                receivedMessages.add(message)

                if (echoSkipCount == 0) {
                    echoServer.sendMessage(message, sender)
                    lastSend = message
                } else {
                    echoSkipCount--
                }
        })
        echoServer.start()

        then: "the UDP echo server should be connected"
        conditions.eventually {
            assert echoServer.connectionStatus == ConnectionStatus.CONNECTED
        }

        when: "an agent with a UDP client protocol configuration is created"
        def agent = new UdpClientAgent("Test agent")
        agent.setRealm(Constants.MASTER_REALM)
            .setHost("127.0.0.1")
            .setPort(echoServerPort)
            .setBindPort(clientPort)
            .setMessageDelimiters([";"] as String[])
            .setMessageStripDelimiter(true)

        and: "the agent is added to the asset service"
        agent = assetStorageService.merge(agent)

        then: "the protocol instance should be created"
        conditions.eventually {
            assert agentService.getProtocolInstance(agent.id) != null
        }

        when: "an asset is created with attributes linked to the protocol configuration"
        def asset = new ThingAsset("Test Asset")
            .setParent(agent)
            .getAttributes().addOrReplace(
            new Attribute<>("echoHello", ValueType.EXECUTION_STATUS)
                .addMeta(
                    new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink(agent.id)
                        .setWriteValue('"Hello {$value};"'))
                ),
            new Attribute<>("echoWorld", ValueType.STRING)
                .addMeta(
                    new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink(agent.id)
                    .setWriteValue("World;"))
                ),
            new Attribute<>("responseHello", ValueType.STRING)
                .addMeta(
                    new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink(agent.id)
                        .setMessageMatchPredicate(
                            new StringPredicate(AssetQuery.Match.BEGIN, true, "Hello"))
                        )
                ),
            new Attribute<>("responseWorld", ValueType.STRING)
                .addMeta(
                    new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink(agent.id)
                        .setMessageMatchPredicate(
                            new StringPredicate(AssetQuery.Match.BEGIN, true, "Hello"))
                    )
                )
        )

        and: "the asset is merged into the asset service"
        asset = assetStorageService.merge(asset)

        then: "the attributes should be linked"
        conditions.eventually {
            assert agentService.getProtocolInstance(agent.id).linkedAttributes.size() == 4
            assert ((UdpClientProtocol)agentService.getProtocolInstance(agent.id)).protocolMessageConsumers.size() == 2
        }

        when: "a linked attribute value is updated"
        def attributeEvent = new AttributeEvent(asset.id,
            "echoHello",
            "there")
        assetProcessingService.sendAttributeEvent(attributeEvent)

        then: "the server should have received the request"
        conditions.eventually {
            assert receivedMessages.indexOf("Hello there") >= 0
        }

        when: "the agent is disabled"
        agent.setDisabled(true)
        agent = assetStorageService.merge(agent)

        then: "the protocol instance should be unlinked"
        conditions.eventually {
            assert agentService.protocolInstanceMap.isEmpty()
        }

        when: "the received messages are cleared"
        receivedMessages.clear()

        then: "after a while no more messages should be received by the server"
        new PollingConditions(timeout: 5, initialDelay: 1).eventually {
            assert receivedMessages.isEmpty()
        }

        when: "the agent is re-enabled"
        agent.setDisabled(false)
        agent = assetStorageService.merge(agent)

        then: "the attributes should be re-linked"
        conditions.eventually {
            assert agentService.getProtocolInstance(agent.id).linkedAttributes.size() == 4
            assert ((UdpClientProtocol)agentService.getProtocolInstance(agent.id)).protocolMessageConsumers.size() == 2
        }

        when: "the echo server is changed to a byte based server"
        echoServer.stop()
        echoServer.removeAllMessageConsumers()
        echoServer = new AbstractUdpServer<byte[]>(protocolExecutorService, new InetSocketAddress(echoServerPort)) {

            @Override
            protected void addDecoders(DatagramChannel channel) {
                addDecoder(channel, new FixedLengthFrameDecoder(3))
                addDecoder(channel, new ByteArrayDecoder())
            }

            @Override
            protected void addEncoders(DatagramChannel channel) {
                addEncoder(channel, new MessageToMessageEncoder<byte[]>() {

                    @Override
                    protected void encode(ChannelHandlerContext channelHandlerContext, byte[] bytes, List<Object> out) throws Exception {
                        out.add(Unpooled.copiedBuffer(bytes))
                    }
                })
            }
        }
        byte[] lastBytes = null
        echoServer.addMessageConsumer({
            message, channel, sender ->
                lastBytes = message
        })
        echoServer.start()

        then: "the server should be connected"
        conditions.eventually {
            assert echoServer.connectionStatus == ConnectionStatus.CONNECTED
        }

        when: "the agent is updated to use HEX mode"
        agent.setMessageDelimiters(null)
        agent.setMessageConvertHex(true)
        agent = assetStorageService.merge(agent)

        then: "the protocol should be relinked"
        conditions.eventually {
            assert agentService.getProtocolInstance(agent.id).linkedAttributes.size() == 4
            assert ((UdpClientProtocol)agentService.getProtocolInstance(agent.id)).protocolMessageConsumers.size() == 2
        }

        when: "the linked attributes are also updated to work with hex server"
        asset.getAttribute("echoHello").ifPresent({it.getMetaValue(MetaItemType.AGENT_LINK)})
            it.meta.replaceAll{it.name.get() == UdpClientProtocol.META_ATTRIBUTE_WRITE_VALUE.urn ? new MetaItem<>(UdpClientProtocol.META_ATTRIBUTE_WRITE_VALUE, '"abcdef"')) : it}}
        asset.getAttribute("echoWorld").ifPresent({it.meta.replaceAll{it.name.get() == UdpClientProtocol.META_ATTRIBUTE_WRITE_VALUE.urn ? new MetaItem<>(UdpClientProtocol.META_ATTRIBUTE_WRITE_VALUE, '"123456"')) : it}}
        asset = assetStorageService.merge(asset)

        then: "the attributes should be relinked"
        conditions.eventually {
            assert udpClientProtocol.protocolMessageConsumers.size() == 1
            assert udpClientProtocol.protocolMessageConsumers.get(new AttributeRef(agent.id, "protocolConfig")).size() == 2
            assert udpClientProtocol.linkedAttributes.get(new AttributeRef(asset.getId(), "echoHello")).getMetaItem(UdpClientProtocol.META_ATTRIBUTE_WRITE_VALUE).flatMap{it.getValueAsString()}.orElse(null) == '"abcdef"'
        }

        and: "the protocol should become CONNECTED"
        conditions.eventually {
            def status = agentService.getAgentConnectionStatus(new AttributeRef(agent.id, "protocolConfig"))
            assert status == ConnectionStatus.CONNECTED
        }

        when: "the hello linked attribute is executed"
        attributeEvent = new AttributeEvent(asset.id,
            "echoHello",
            AttributeExecuteStatus.REQUEST_START.asValue())
        assetProcessingService.sendAttributeEvent(attributeEvent)

        then: "the bytes should be received by the server"
        conditions.eventually {
            assert lastBytes != null
            assert lastBytes.length == 3
            assert (lastBytes[0] & 0xFF) == 171
            assert (lastBytes[1] & 0xFF) == 205
            assert (lastBytes[2] & 0xFF) == 239
        }

        cleanup: "the server should be stopped"
        if (echoServer != null) {
            echoServer.stop()
        }
    }
}
