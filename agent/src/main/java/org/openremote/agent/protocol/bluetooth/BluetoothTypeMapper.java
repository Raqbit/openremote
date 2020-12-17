package org.openremote.agent.protocol.bluetooth;

import org.openremote.model.value.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BluetoothTypeMapper {

    /**
     * Charset used to encode/decode strings
     */
    static final Charset STRING_CHARSET = StandardCharsets.UTF_8;
    static final int INTEGER_SIZE = 4;

    /**
     * Stores all mappers between byte arrays and OpenRemote types
     */
    static Map<ValueType, ITypeMapper> typeMappers = new HashMap<>();

    static {
        typeMappers.put(ValueType.STRING, new ITypeMapper() {
            @Override
            public Value bytesToValue(byte[] input) {
                return Values.create(bytesToString(input));
            }

            @Override
            public byte[] valueToBytes(Value value) {
                StringValue val = (StringValue) value;
                return stringToBytes(val.getString());
            }
        });

        // FIXME: We're only supporting 32bit ints for now, while Number value types can hold decimals
        typeMappers.put(ValueType.NUMBER, new ITypeMapper() {
            @Override
            public Value bytesToValue(byte[] input) {
                ByteBuffer buf = ByteBuffer.allocate(INTEGER_SIZE);
                buf.position(buf.capacity() - input.length);
                buf.put(input);
                buf.position(0);
                int value = buf.getInt();
                return Values.create(value);
            }

            @Override
            public byte[] valueToBytes(Value value) {
                NumberValue val = (NumberValue) value;
                return ByteBuffer.allocate(INTEGER_SIZE).putDouble(val.getNumber()).array();
            }
        });

        typeMappers.put(ValueType.BOOLEAN, new ITypeMapper() {
            @Override
            public Value bytesToValue(byte[] input) {
                return Values.create(bytesToString(input).equals("true"));
            }

            @Override
            public byte[] valueToBytes(Value value) {
                BooleanValue val = (BooleanValue) value;

                if (val.getBoolean()) {
                    return stringToBytes("true");
                } else {
                    return stringToBytes("false");
                }
            }
        });

        typeMappers.put(ValueType.ANY, new ITypeMapper() {
            @Override
            public Value bytesToValue(byte[] input) {
                String stringValue = bytesToString(input);
                Optional<Value> value = Values.parse(stringValue);

                if (!value.isPresent()) {
                    throw new ValueException("Could not parse ANY type as JSON");
                }

                return value.get();
            }

            @Override
            public byte[] valueToBytes(Value value) {
                return stringToBytes(value.toJson());
            }
        });
    }

    private static String bytesToString(byte[] bytes) {
        return new String(bytes, STRING_CHARSET);
    }

    private static byte[] stringToBytes(String value) {
        return value.getBytes(STRING_CHARSET);
    }

    public static byte[] getBytesFromValue(Value value) {
        // Get type mapper for type, default to ANY (raw JSON) mapper
        ITypeMapper mapper = typeMappers.getOrDefault(value.getType(), typeMappers.get(ValueType.ANY));
        return mapper.valueToBytes(value);
    }

    public static Value getValueFromBytes(ValueType type, byte[] input) {
        // Get type mapper for type, default to ANY (raw JSON) mapper
        ITypeMapper mapper = typeMappers.getOrDefault(type, typeMappers.get(ValueType.ANY));
        return mapper.bytesToValue(input);
    }

    private interface ITypeMapper {
        Value bytesToValue(byte[] input);

        byte[] valueToBytes(Value value);
    }
}

