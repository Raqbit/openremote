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
package org.openremote.agent.protocol.velbus.device;

import org.openremote.agent.protocol.velbus.VelbusPacket;
import org.openremote.model.attribute.AttributeType;
import org.openremote.model.util.EnumUtil;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;
import org.openremote.model.value.Values;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.openremote.agent.protocol.velbus.VelbusPacket.OutboundCommand.*;

public class RelayProcessor extends OutputChannelProcessor {

    enum ChannelState implements DevicePropertyValue<ChannelState> {
        OFF(0x00),
        ON(0x01),
        INTERMITTENT(0x03);

        private int code;

        ChannelState(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        @Override
        public Value toValue(ValueType valueType) {
            switch (valueType) {
                case BOOLEAN:
                    switch (this) {
                        case ON:
                            return Values.create(true);
                        case OFF:
                            return Values.create(false);
                        case INTERMITTENT:
                            return null;
                    }
                default:
                    return EnumUtil.enumToValue(this, valueType);
            }
        }

        @Override
        public ChannelState getPropertyValue() {
            return this;
        }

        public static ChannelState fromCode(int code) {
            for (ChannelState type : ChannelState.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }

            return OFF;
        }

        public static Optional<ChannelState> fromValue(Value value) {
            if (value == null) {
                return Optional.empty();
            }

            switch (value.getType()) {
                case BOOLEAN:
                    return fromBoolean(Values.getBoolean(value).orElse(null));
                default:
                    return EnumUtil.enumFromValue(ChannelState.class, value);
            }
        }

        public static Optional<ChannelState> fromBoolean(Boolean value) {
            if (value == null) {
                return Optional.of(INTERMITTENT);
            } else if (value) {
                return Optional.of(ON);
            } else {
                return Optional.of(OFF);
            }
        }
    }

    protected final static List<PropertyDescriptor> SUPPORTED_PROPERTIES = Arrays.asList(
        new PropertyDescriptor("ch1State", "CH1", "CH1", AttributeType.STRING),
        new PropertyDescriptor("ch2State", "CH2", "CH2", AttributeType.STRING),
        new PropertyDescriptor("ch3State", "CH3", "CH3", AttributeType.STRING),
        new PropertyDescriptor("ch4State", "CH4", "CH4", AttributeType.STRING),
        new PropertyDescriptor("ch5State", "CH5", "CH5", AttributeType.STRING),
        new PropertyDescriptor("ch1Setting", "CH1 Setting", "CH1_SETTING", AttributeType.STRING),
        new PropertyDescriptor("ch2Setting", "CH2 Setting", "CH2_SETTING", AttributeType.STRING),
        new PropertyDescriptor("ch3Setting", "CH3 Setting", "CH3_SETTING", AttributeType.STRING),
        new PropertyDescriptor("ch4Setting", "CH4 Setting", "CH4_SETTING", AttributeType.STRING),
        new PropertyDescriptor("ch5Setting", "CH5 Setting", "CH5_SETTING", AttributeType.STRING),
        new PropertyDescriptor("ch1Locked", "CH1 Locked", "CH1_LOCKED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch2Locked", "CH2 Locked", "CH2_LOCKED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch3Locked", "CH3 Locked", "CH3_LOCKED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch4Locked", "CH4 Locked", "CH4_LOCKED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch5Locked", "CH5 Locked", "CH5_LOCKED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch1Inhibited", "CH1 Inhibited", "CH1_INHIBITED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch2Inhibited", "CH2 Inhibited", "CH2_INHIBITED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch3Inhibited", "CH3 Inhibited", "CH3_INHIBITED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch4Inhibited", "CH4 Inhibited", "CH4_INHIBITED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch5Inhibited", "CH5 Inhibited", "CH5_INHIBITED", AttributeType.BOOLEAN),
        new PropertyDescriptor("ch1LedState", "CH1 LED State", "CH1_LED", AttributeType.STRING),
        new PropertyDescriptor("ch2LedState", "CH2 LED State", "CH2_LED", AttributeType.STRING),
        new PropertyDescriptor("ch3LedState", "CH3 LED State", "CH3_LED", AttributeType.STRING),
        new PropertyDescriptor("ch4LedState", "CH4 LED State", "CH4_LED", AttributeType.STRING),
        new PropertyDescriptor("ch5LedState", "CH5 LED State", "CH5_LED", AttributeType.STRING),
        new PropertyDescriptor("ch1OnSeconds", "CH1 On (s)", "CH1_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch2OnSeconds", "CH2 On (s)", "CH2_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch3OnSeconds", "CH3 On (s)", "CH3_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch4OnSeconds", "CH4 On (s)", "CH4_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch5OnSeconds", "CH5 On (s)", "CH5_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch1IntermittentSeconds", "CH1 Intermittent (s)", "CH1_INTERMITTENT", AttributeType.NUMBER),
        new PropertyDescriptor("ch2IntermittentSeconds", "CH2 Intermittent (s)", "CH2_INTERMITTENT", AttributeType.NUMBER),
        new PropertyDescriptor("ch3IntermittentSeconds", "CH3 Intermittent (s)", "CH3_INTERMITTENT", AttributeType.NUMBER),
        new PropertyDescriptor("ch4IntermittentSeconds", "CH4 Intermittent (s)", "CH4_INTERMITTENT", AttributeType.NUMBER),
        new PropertyDescriptor("ch5IntermittentSeconds", "CH5 Intermittent (s)", "CH5_INTERMITTENT", AttributeType.NUMBER),
        new PropertyDescriptor("ch1LockSeconds", "CH1 Lock (s)", "CH1_LOCK", AttributeType.NUMBER),
        new PropertyDescriptor("ch2LockSeconds", "CH2 Lock (s)", "CH2_LOCK", AttributeType.NUMBER),
        new PropertyDescriptor("ch3LockSeconds", "CH3 Lock (s)", "CH3_LOCK", AttributeType.NUMBER),
        new PropertyDescriptor("ch4LockSeconds", "CH4 Lock (s)", "CH4_LOCK", AttributeType.NUMBER),
        new PropertyDescriptor("ch5LockSeconds", "CH5 Lock (s)", "CH5_LOCK", AttributeType.NUMBER),
        new PropertyDescriptor("ch1ForceOnSeconds", "CH1 Force On (s)", "CH1_FORCE_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch2ForceOnSeconds", "CH2 Force On (s)", "CH2_FORCE_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch3ForceOnSeconds", "CH3 Force On (s)", "CH3_FORCE_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch4ForceOnSeconds", "CH4 Force On (s)", "CH4_FORCE_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch5ForceOnSeconds", "CH5 Force On (s)", "CH5_FORCE_ON", AttributeType.NUMBER),
        new PropertyDescriptor("ch1InhibitSeconds", "CH1 Inhibit (s)", "CH1_INHIBIT", AttributeType.NUMBER),
        new PropertyDescriptor("ch2InhibitSeconds", "CH2 Inhibit (s)", "CH2_INHIBIT", AttributeType.NUMBER),
        new PropertyDescriptor("ch3InhibitSeconds", "CH3 Inhibit (s)", "CH3_INHIBIT", AttributeType.NUMBER),
        new PropertyDescriptor("ch4InhibitSeconds", "CH4 Inhibit (s)", "CH4_INHIBIT", AttributeType.NUMBER),
        new PropertyDescriptor("ch5InhibitSeconds", "CH5 Inhibit (s)", "CH5_INHIBIT", AttributeType.NUMBER)
    );

    @Override
    public List<VelbusPacket> getStatusRequestPackets(VelbusDevice device) {
        return Collections.singletonList(
            new VelbusPacket(device.getBaseAddress(), VelbusPacket.OutboundCommand.MODULE_STATUS.getCode(), (byte)0x1F)
        );
    }

    @Override
    public List<PropertyDescriptor> getPropertyDescriptors(VelbusDeviceType deviceType) {
        return SUPPORTED_PROPERTIES;
    }

    @Override
    public List<VelbusPacket> getPropertyWritePackets(VelbusDevice device, String property, Value value) {
        return getChannelNumberAndPropertySuffix(device, CHANNEL_REGEX, property)
            .map(
                channelNumberAndPropertySuffix -> {
                    int channelNumber = channelNumberAndPropertySuffix.key;
                    VelbusPacket.OutboundCommand command = null;
                    Integer[] params = new Integer[1];

                    switch (channelNumberAndPropertySuffix.value) {
                        case "":
                            command = ChannelState.fromValue(value)
                                .map(state -> {
                                    switch (state) {
                                        case OFF:
                                            return RELAY_OFF;
                                        case ON:
                                            return RELAY_ON;
                                        case INTERMITTENT:
                                            params[0] = 0xFFFFFF;
                                            return RELAY_BLINK_TIMER;
                                    }
                                    return null;
                                })
                                .orElse(null);
                            break;
                        case "LOCKED":
                            command = Values.getBoolean(value)
                                .map(locked -> {
                                    params[0] = 0xFFFFFF;
                                    return locked ? LOCK : LOCK_CANCEL;
                                })
                                .orElse(null);
                            break;
                        case "INHIBITED":
                            command = Values.getBoolean(value)
                                .map(inhibited -> {
                                    params[0] = 0xFFFFFF;
                                    return inhibited ? INHIBIT : INHIBIT_CANCEL;
                                })
                                .orElse(null);
                            break;
                        case "_ON":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[0] = duration;
                                    return duration == 0 ? RELAY_OFF : RELAY_ON_TIMER;
                                })
                                .orElse(null);
                             break;
                        case "_INTERMITTENT":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[0] = duration;
                                    return duration == 0 ? RELAY_OFF : RELAY_BLINK_TIMER;
                                })
                                .orElse(null);
                            break;
                        case "_LOCK":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[0] = duration;
                                    return duration == 0 ? LOCK_CANCEL : LOCK;
                                })
                                .orElse(null);
                            break;
                        case "_FORCE_ON":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[0] = duration;
                                    return duration == 0 ? FORCE_ON_CANCEL : FORCE_ON;
                                })
                                .orElse(null);
                            break;
                        case "_INHIBIT":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[0] = duration;
                                    return duration == 0 ? INHIBIT_CANCEL : INHIBIT;
                                })
                                .orElse(null);
                            break;
                    }

                    if (command != null) {
                        return getPackets(device, channelNumber, command, params[0] == null ? 0 : params[0]);
                    }

                    return null;
                }
            )
            .orElse(null);
    }

    @Override
    public boolean processReceivedPacket(VelbusDevice device, VelbusPacket packet) {

        VelbusPacket.InboundCommand packetCommand = VelbusPacket.InboundCommand.fromCode(packet.getCommand());

        switch (packetCommand) {
            case RELAY_STATUS:
                // Extract channel info
                int channelNumber = (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1;

                ChannelSetting setting = device.getDeviceType() == VelbusDeviceType.VMB1RY ? ChannelSetting.NORMAL : ChannelSetting.fromCode(packet.getByte(2) & 0xFF);
                ChannelState state = (packet.getByte(3) & 0xFF) == 0x11 ? ChannelState.INTERMITTENT : ChannelState.fromCode(packet.getByte(3) & 0xFF);
                LedState ledStatus = device.getDeviceType() == VelbusDeviceType.VMB1RY ? null : LedState.fromCode(packet.getByte(4) & 0xFF);

                device.setProperty("CH" + channelNumber, state);
                device.setProperty("CH" + channelNumber + "_SETTING", setting);
                device.setProperty("CH" + channelNumber + "_LOCKED", setting == ChannelSetting.LOCKED ? BooleanDevicePropertyValue.TRUE : BooleanDevicePropertyValue.FALSE);
                device.setProperty("CH" + channelNumber + "_INHIBITED", setting == ChannelSetting.INHIBITED ? BooleanDevicePropertyValue.TRUE : BooleanDevicePropertyValue.FALSE);
                device.setProperty("CH" + channelNumber + "_LED", ledStatus);
                return true;
        }

        return false;
    }

    protected static List<VelbusPacket> getPackets(VelbusDevice velbusDevice, int channelNumber, VelbusPacket.OutboundCommand command, int durationSeconds) {
        byte[] packetBytes = null;

        switch (command) {
            case FORCE_ON:
            case RELAY_ON_TIMER:
            case RELAY_BLINK_TIMER:
            case LOCK:
            case INHIBIT:
                durationSeconds = durationSeconds == -1 ? 0xFFFFFF : durationSeconds;
                durationSeconds = Math.min(0xFFFFFF, Math.max(0, durationSeconds));
                packetBytes = new byte[4];
                packetBytes[1] = (byte)(durationSeconds >> 16);
                packetBytes[2] = (byte)(durationSeconds >> 8);
                packetBytes[3] = (byte)(durationSeconds);
                break;
            case RELAY_ON:
            case RELAY_OFF:
            case INHIBIT_CANCEL:
            case FORCE_ON_CANCEL:
            case LOCK_CANCEL:
                packetBytes = new byte[1];
                break;
        }

        if (packetBytes != null) {
            packetBytes[0] = (byte)Math.pow(2, channelNumber-1);

            return Collections.singletonList(
                new VelbusPacket(velbusDevice.getBaseAddress(), command.getCode(), VelbusPacket.PacketPriority.HIGH, packetBytes)
            );
        }

        return null;
    }
}
