package org.anstreth.torrent.serialization;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReflectiveSerializer implements Serializer<Object> {
    private final Map<Class<?>, FieldSerializer> fieldSerializers = new HashMap<>();

    {
        fieldSerializers.put(int.class, (o, f, stream) -> stream.writeInt(f.getInt(o)));
        fieldSerializers.put(long.class, (o, f, stream) -> stream.writeLong(f.getLong(o)));
        fieldSerializers.put(double.class, (o, f, stream) -> stream.writeDouble(f.getDouble(o)));
        fieldSerializers.put(float.class, (o, f, stream) -> stream.writeFloat(f.getFloat(o)));
        fieldSerializers.put(short.class, (o, f, stream) -> stream.writeShort(f.getShort(o)));
        fieldSerializers.put(boolean.class, (o, f, stream) -> stream.writeBoolean(f.getBoolean(o)));
        fieldSerializers.put(byte.class, (o, f, stream) -> stream.writeByte(f.getByte(o)));
        fieldSerializers.put(char.class, (o, f, stream) -> stream.writeChar(f.getChar(o)));

        fieldSerializers.put(Integer.class, (o, f, stream) -> stream.writeInt((Integer) f.get(o)));
        fieldSerializers.put(Long.class, (o, f, stream) -> stream.writeLong((Long) f.get(o)));
        fieldSerializers.put(Double.class, (o, f, stream) -> stream.writeDouble((Double) f.get(o)));
        fieldSerializers.put(Float.class, (o, f, stream) -> stream.writeFloat((Float) f.get(o)));
        fieldSerializers.put(Short.class, (o, f, stream) -> stream.writeShort((Short) f.get(o)));
        fieldSerializers.put(Boolean.class, (o, f, stream) -> stream.writeBoolean((Boolean) f.get(o)));
        fieldSerializers.put(Byte.class, (o, f, stream) -> stream.writeByte((Byte) f.get(o)));
        fieldSerializers.put(Character.class, (o, f, stream) -> stream.writeChar((Character) f.get(o)));

        fieldSerializers.put(String.class, (o, f, stream) -> stream.writeUTF((String) f.get(o)));

        fieldSerializers.put(List.class, (o, f, stream) -> {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) f.get(o);
            SerializationUtils.serializeList(list, this, stream);
        });
    }

    @Override
    public void serialize(Object value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = SerializationUtils.getDataOutputStream(stream);
        Field[] fields = value.getClass().getDeclaredFields();
        for (Field field : fields) {
            FieldSerializer fieldSerializer = fieldSerializers.get(field.getType());
            if (fieldSerializer != null) {
                fieldSerializer.serializeWithAccess(value, field, dataOutputStream);
            } else {
                serializeFieldRecursively(value, field, dataOutputStream);
            }
        }
    }

    private void serializeFieldRecursively(Object object, Field field, OutputStream stream) throws IOException {
        field.setAccessible(true);
        try {
            serialize(field.get(object), stream);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot serialize field " + field + " of " + object, e);
        }
    }

    @FunctionalInterface
    interface FieldSerializer {
        default void serializeWithAccess(Object object, Field field, DataOutputStream stream) throws IOException {
            field.setAccessible(true);
            try {
                serialize(object, field, stream);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot serialize field " + field + " of " + object, e);
            }
        }

        void serialize(Object object, Field field, DataOutputStream stream) throws IOException, IllegalAccessException;
    }
}
