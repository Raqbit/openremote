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
import org.openremote.model.util.Pair;
import org.openremote.model.value.Value;
import org.openremote.model.value.ValueType;
import org.openremote.model.value.Values;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.openremote.agent.protocol.velbus.VelbusPacket.InboundCommand.DIMMER_STATUS;
import static org.openremote.agent.protocol.velbus.VelbusPacket.OutboundCommand.*;
import static org.openremote.model.util.TextUtil.toLowerCamelCase;
import static org.openremote.model.util.TextUtil.toProperCase;
import static org.openremote.model.util.TextUtil.toUpperCamelCase;

public class AnalogOutputProcessor extends OutputChannelProcessor {

    public enum ChannelState implements DevicePropertyValue<ChannelState> {
        OFF,
        ON,
        LAST,
        HALT;

        @Override
        public Value toValue(ValueType valueType) {
            switch (valueType) {
                case BOOLEAN:
                    switch (this) {
                        case ON:
                            return Values.create(true);
                        case OFF:
                            return Values.create(false);
                        case LAST:
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

        public static Optional<ChannelState> fromValue(Value value) {
            if (value == null) {
                return Optional.of(LAST);
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
                return Optional.of(LAST);
            } else if (value) {
                return Optional.of(ON);
            } else {
                return Optional.of(OFF);
            }
        }
    }

    protected final static String DIMMER_PREFIX = "CH";
    protected final static String IO_PREFIX = "OUTPUT";
    protected static final Pattern IO_CHANNEL_REGEX = Pattern.compile("^OUTPUT(\\d+)(.*$|$)");

    protected final static List<Pair<String, AttributeType>> CHANNEL_PROPERTIES = Arrays.asList(
        // RW - ChannelState
        new Pair<>("", AttributeType.STRING),
        // R - ChannelSetting
        new Pair<>("_SETTING", AttributeType.STRING),
        // R - Read LED status
        new Pair<>("_LED", AttributeType.STRING),
        // RW - True/False
        new Pair<>("_LOCKED", AttributeType.BOOLEAN),
        // RW - True/False
        new Pair<>("_INHIBITED", AttributeType.BOOLEAN),
        // W - Dim level and speed (LEVEL:SPEED)
        new Pair<>("_LEVEL_AND_SPEED", AttributeType.STRING),
        // RW - Dim level 0-100% (-1 to stop current dimming)
        new Pair<>("_LEVEL", AttributeType.NUMBER),
        // W - On for specified time in seconds (0 to cancel)
        new Pair<>("_ON", AttributeType.NUMBER),
        // W - Forced on for specified time in seconds (0 to cancel)
        new Pair<>("_FORCE_ON", AttributeType.NUMBER),
        // W - Lock (force off) for specified time in seconds (0 to unlock)
        new Pair<>("_LOCK", AttributeType.NUMBER),
        // W - Inhibit for specified time in seconds (0 to un-inhibit)
        new Pair<>("_INHIBIT", AttributeType.NUMBER)
    );

    @Override
    public List<VelbusPacket> getStatusRequestPackets(VelbusDevice device) {
        byte channelRequest = device.getDeviceType() == VelbusDeviceType.VMB4DC ? (byte)0x0F : (byte)0x01;
        return Collections.singletonList(
            new VelbusPacket(device.getBaseAddress(), VelbusPacket.OutboundCommand.MODULE_STATUS.getCode(), channelRequest)
        );
    }

    @Override
    public List<PropertyDescriptor> getPropertyDescriptors(VelbusDeviceType deviceType) {
        List<Pair<String, AttributeType>> propertySuffixes = new ArrayList<>(CHANNEL_PROPERTIES);
        final String chPrefix = deviceType == VelbusDeviceType.VMB4AN ? "Output " : "CH";

        if (deviceType == VelbusDeviceType.VMB4AN) {
            propertySuffixes.add(new Pair<>("_VALUE", AttributeType.NUMBER));
            propertySuffixes.add(new Pair<>("_VALUE_AND_SPEED", AttributeType.STRING));
        }

        int channelCount = deviceType == VelbusDeviceType.VMB4AN ? 4 : ChannelProcessor.getMaxChannelNumber(deviceType);

        return IntStream.rangeClosed(1, channelCount)
            .mapToObj(Integer::toString)
            .flatMap(chNumber ->
                propertySuffixes.stream().map(propSuffix ->
                    new PropertyDescriptor(
                        toLowerCamelCase(chPrefix).trim() + chNumber + toUpperCamelCase(propSuffix.key),
                        (chPrefix + chNumber + " " + toProperCase(propSuffix.key, true)).trim(),
                        getChannelPrefix(deviceType) + chNumber + propSuffix.key,
                        propSuffix.value
                    )
                )
            )
            .collect(Collectors.toList());
    }

    protected String getChannelPrefix(VelbusDeviceType deviceType) {
        return deviceType == VelbusDeviceType.VMB4AN ? IO_PREFIX : DIMMER_PREFIX;
    }

    @Override
    public List<VelbusPacket> getPropertyWritePackets(VelbusDevice device, String property, Value value) {
        return getChannelNumberAndPropertySuffix(device, device.getDeviceType() == VelbusDeviceType.VMB4AN ? IO_CHANNEL_REGEX : CHANNEL_REGEX, property)
            .map(
                channelNumberAndPropertySuffix -> {
                    int channelNumber = channelNumberAndPropertySuffix.key;
                    VelbusPacket.OutboundCommand command = null;
                    // Level, Speed, Duration
                    Integer[] params = new Integer[3];

                    switch (channelNumberAndPropertySuffix.value) {
                        case "":
                            command = ChannelState.fromValue(value)
                                .map(state -> {
                                    switch (state) {
                                        case OFF:
                                            params[0] = 0;
                                            return SET_LEVEL;
                                        case ON:
                                            params[0] = 100;
                                            return SET_LEVEL;
                                        case LAST:
                                            return SET_LEVEL_LAST;
                                        case HALT:
                                            return SET_LEVEL_HALT;
                                    }
                                    return null;
                                })
                                .orElse(null);
                            break;
                        case "LOCKED":
                            command = Values.getBoolean(value)
                                .map(locked -> {
                                    params[2] = 0xFFFFFF;
                                    return locked ? LOCK : LOCK_CANCEL;
                                })
                                .orElse(null);
                            break;
                        case "INHIBITED":
                            command = Values.getBoolean(value)
                                .map(inhibited -> {
                                    params[2] = 0xFFFFFF;
                                    return inhibited ? INHIBIT : INHIBIT_CANCEL;
                                })
                                .orElse(null);
                            break;
                        case "_VALUE_AND_SPEED":
                        case "_LEVEL_AND_SPEED":
                            command = getLevelAndSpeedFromValue(value)
                                .map(levelAndSpeed -> {
                                    params[0] = levelAndSpeed.key;
                                    params[1] = levelAndSpeed.value;
                                    return levelAndSpeed.key < 0 ? SET_LEVEL_HALT : channelNumberAndPropertySuffix.value.equals("_VALUE_AND_SPEED") ? SET_VALUE : SET_LEVEL;
                                })
                                .orElse(null);
                            break;
                        case "_VALUE":
                        case "_LEVEL":
                            command = Values.getIntegerCoerced(value)
                                .map(level -> {
                                    params[0] = level;
                                    return level < 0 ? SET_LEVEL_HALT : channelNumberAndPropertySuffix.value.equals("_VALUE") ? SET_VALUE : SET_LEVEL;
                                })
                                .orElse(null);
                            break;
                        case "_LOCK":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[2] = duration;
                                    return duration == 0 ? LOCK_CANCEL : LOCK;
                                })
                                .orElse(null);
                            break;
                        case "_ON":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[2] = duration;
                                    if (duration == 0) {
                                        params[0] = 0;
                                        return SET_LEVEL;
                                    }

                                    return LEVEL_ON_TIMER;
                                })
                                .orElse(null);
                            break;
                        case "_FORCE_ON":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[2] = duration;
                                    return duration == 0 ? FORCE_ON_CANCEL : FORCE_ON;
                                })
                                .orElse(null);
                            break;
                        case "_INHIBIT":
                            command = Values.getIntegerCoerced(value)
                                .map(duration -> {
                                    params[2] = duration;
                                    return duration == 0 ? INHIBIT_CANCEL : INHIBIT;
                                })
                                .orElse(null);
                    }

                    if (command != null) {
                        return getPackets(
                            device,
                            channelNumber,
                            command,
                            params[0] == null ? 0 : params[0],
                            params[1] == null ? 0 : params[1],
                            params[2] == null ? 0 : params[2]
                        );
                    }

                    return null;
                }
            )
            .orElse(null);
    }

    @Override
    public boolean processReceivedPacket(VelbusDevice device, VelbusPacket packet) {
        VelbusPacket.InboundCommand packetCommand = VelbusPacket.InboundCommand.fromCode(packet.getCommand());
        VelbusDeviceType deviceType = device.getDeviceType();

        switch (packetCommand) {
            case DIMMER_STATUS:
            case OUT_LEVEL_STATUS:

                // Extract channel info
                // DIMMER_STATUS command is only used on 1 channel dimmer modules
                int channelNumber = 1;
                IntDevicePropertyValue level;
                String levelPropertyName = deviceType == VelbusDeviceType.VMB4AN ? "_VALUE" : "_LEVEL";

                if (packetCommand != DIMMER_STATUS) {
                    if (device.getDeviceType() == VelbusDeviceType.VMB4AN) {
                        channelNumber = (packet.getByte(1) & 0xFF) - 11;
                        level = new IntDevicePropertyValue((packet.getByte(3) << 8) + packet.getByte(4));
                    } else {
                        channelNumber = (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1;
                        level = new IntDevicePropertyValue(packet.getByte(3) & 0xFF);
                    }
                } else {
                    level = new IntDevicePropertyValue(packet.getByte(3) & 0xFF);
                }

                ChannelSetting setting = packetCommand == DIMMER_STATUS ? ChannelSetting.NORMAL : ChannelSetting.fromCode(packet.getByte(2) & 0x03);
                ChannelState state = level.getPropertyValue() > 0 ? ChannelState.ON : ChannelState.OFF;
                LedState ledState = packetCommand == DIMMER_STATUS ? LedState.fromCode(packet.getByte(3) & 0xFF) : LedState.fromCode(packet.getByte(4) & 0xFF);
                BooleanDevicePropertyValue locked = setting == ChannelSetting.LOCKED ? BooleanDevicePropertyValue.TRUE : BooleanDevicePropertyValue.FALSE;
                BooleanDevicePropertyValue inhibited = setting == ChannelSetting.INHIBITED ? BooleanDevicePropertyValue.TRUE : BooleanDevicePropertyValue.FALSE;

                // Push to device cache
                device.setProperty(getChannelPrefix(deviceType) + channelNumber, state);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + "_SETTING", setting);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + levelPropertyName, level);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + "_LED", ledState);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + "_LOCKED", locked);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + "_INHIBITED", inhibited);
                return true;
            case DIMMER_LEVEL_STATUS:
                channelNumber = (int) (Math.log((packet.getByte(1) & 0xFF)) / Math.log(2)) + 1;
                level = new IntDevicePropertyValue(packet.getByte(2) & 0xFF);
                device.setProperty(getChannelPrefix(deviceType) + channelNumber + "_LEVEL", level);
                return true;
            case PUSH_BUTTON_STATUS:
                if (device.getDeviceType() != VelbusDeviceType.VMB4AN) {
                    // Update each of the dimmer channels
                    int onByte = packet.getByte(1) & 0xFF;
                    int offByte = packet.getByte(2) & 0xFF;

                    for (int i = 1; i <= ChannelProcessor.getMaxChannelNumber(device.getDeviceType()); i++) {
                        if ((onByte & 0x01) == 1) {
                            device.setProperty(getChannelPrefix(deviceType) + i, ChannelState.ON);
                        } else if ((offByte & 0x01) == 1) {
                            device.setProperty(getChannelPrefix(deviceType) + i, ChannelState.OFF);
                        }

                        onByte = onByte >>> 1;
                        offByte = offByte >>> 1;
                    }
                    return true;
                }
                break;
        }

        return false;
    }

    protected List<VelbusPacket> getPackets(VelbusDevice velbusDevice, int channelNumber, VelbusPacket.OutboundCommand command, int level, int speedSeconds, int durationSeconds) {
        byte[] packetBytes = null;

        switch (command) {
            case SET_VALUE:
            case SET_LEVEL:
            case SET_LEVEL_LAST:
                speedSeconds = Math.min(0xFFFF, Math.max(0, speedSeconds));
                if (command == SET_VALUE) {
                    packetBytes = new byte[5];
                    packetBytes[1] = (byte)(level >> 8);
                    packetBytes[2] = (byte)level;
                    packetBytes[3] = (byte)(speedSeconds >> 8);
                    packetBytes[4] = (byte)speedSeconds;
                } else {
                    level = Math.min(100, Math.max(0, level));
                    packetBytes = new byte[4];
                    packetBytes[1] = (byte)level;
                    packetBytes[2] = (byte)(speedSeconds >> 8);
                    packetBytes[3] = (byte)speedSeconds;
                }
                break;
            case FORCE_ON:
            case LEVEL_ON_TIMER:
            case LOCK:
            case INHIBIT:
                durationSeconds = durationSeconds == -1 ? 0xFFFFFF : durationSeconds;
                durationSeconds = Math.min(0xFFFFFF, Math.max(0, durationSeconds));
                packetBytes = new byte[4];
                packetBytes[1] = (byte)(durationSeconds >> 16);
                packetBytes[2] = (byte)(durationSeconds >> 8);
                packetBytes[3] = (byte)(durationSeconds);
                break;
            case SET_LEVEL_HALT:
                // for some reason the module doesn't reliable update the LED status when a halt command is sent
                String ledProp = getChannelPrefix(velbusDevice.getDeviceType()) + channelNumber + "_LED";
                LedState ledState = (LedState)velbusDevice.getPropertyValue(ledProp);
                if (ledState == LedState.FAST) {
                    velbusDevice.setProperty(ledProp, LedState.ON);
                }
            case INHIBIT_CANCEL:
            case FORCE_ON_CANCEL:
            case LOCK_CANCEL:
                packetBytes = new byte[1];
                break;
        }

        if (packetBytes != null) {
            if (velbusDevice.getDeviceType() == VelbusDeviceType.VMB4AN) {
                packetBytes[0] = (byte)(channelNumber + 11);
            } else {
                packetBytes[0] = (byte) Math.pow(2, channelNumber - 1);
            }

            // For LEVEL_ON_TIMER we must also send a SET_LEVEL to make sure the dimmer is on
            if (command == LEVEL_ON_TIMER) {
                return Arrays.asList(
                    new VelbusPacket(velbusDevice.getBaseAddress(), SET_LEVEL.getCode(), VelbusPacket.PacketPriority.HIGH, packetBytes[0], (byte)100, (byte)0, (byte)0),
                    new VelbusPacket(velbusDevice.getBaseAddress(), command.getCode(), VelbusPacket.PacketPriority.HIGH, packetBytes)
                );
            }

            return Collections.singletonList(
                new VelbusPacket(velbusDevice.getBaseAddress(), command.getCode(), VelbusPacket.PacketPriority.HIGH, packetBytes)
            );
        }

        return null;
    }

    protected Optional<Pair<Integer, Integer>> getLevelAndSpeedFromValue(Value value) {
        if (value == null) {
            return Optional.empty();
        }

        switch (value.getType()) {

            case OBJECT:
                return Values.getObject(value)
                    .map(obj -> {
                        Optional<Integer> level = Values.getIntegerCoerced(obj.get("level").orElse(null));
                        Optional<Integer> speed = Values.getIntegerCoerced(obj.get("speed").orElse(null));
                        return level.isPresent() && speed.isPresent() ? new Pair<>(level.get(), speed.get()) : null;
                    });
            case ARRAY:
                return Values.getArray(value)
                    .map(arr -> {
                        Optional<Integer> level = Values.getIntegerCoerced(arr.get(0).orElse(null));
                        Optional<Integer> speed = Values.getIntegerCoerced(arr.get(1).orElse(null));
                        return level.isPresent() && speed.isPresent() ? new Pair<>(level.get(), speed.get()) : null;
                    });
            case STRING:
                return Values.getString(value)
                    .map(levelSpeedStr -> {
                       String[] levelSpeedArr = levelSpeedStr.split("(?<=\\d):(?=\\d)");
                       if (levelSpeedArr.length == 2) {
                           int level = Integer.parseInt(levelSpeedArr[0]);
                           int speed = Integer.parseInt(levelSpeedArr[1]);
                           return new Pair<>(level, speed);
                       }
                       return null;
                    });
        }

        return Optional.empty();
    }
}
