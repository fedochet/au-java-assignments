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
    private final Map<Class<?>, PrimitiveFieldSerializer> primitivesFieldSerializers = new HashMap<>();

    {
        primitivesFieldSerializers.put(int.class, (o, f, stream) -> stream.writeInt(f.getInt(o)));
        primitivesFieldSerializers.put(long.class, (o, f, stream) -> stream.writeLong(f.getLong(o)));
        primitivesFieldSerializers.put(double.class, (o, f, stream) -> stream.writeDouble(f.getDouble(o)));
        primitivesFieldSerializers.put(float.class, (o, f, stream) -> stream.writeFloat(f.getFloat(o)));
        primitivesFieldSerializers.put(short.class, (o, f, stream) -> stream.writeShort(f.getShort(o)));
        primitivesFieldSerializers.put(boolean.class, (o, f, stream) -> stream.writeBoolean(f.getBoolean(o)));
        primitivesFieldSerializers.put(byte.class, (o, f, stream) -> stream.writeByte(f.getByte(o)));
        primitivesFieldSerializers.put(char.class, (o, f, stream) -> stream.writeChar(f.getChar(o)));

        fieldSerializers.put(Integer.class, (o, stream) -> stream.writeInt((Integer) o));
        fieldSerializers.put(Long.class, (o, stream) -> stream.writeLong((Long) o));
        fieldSerializers.put(Double.class, (o, stream) -> stream.writeDouble((Double) o));
        fieldSerializers.put(Float.class, (o, stream) -> stream.writeFloat((Float) o));
        fieldSerializers.put(Short.class, (o, stream) -> stream.writeShort((Short) o));
        fieldSerializers.put(Boolean.class, (o, stream) -> stream.writeBoolean((Boolean) o));
        fieldSerializers.put(Byte.class, (o, stream) -> stream.writeByte((Byte) o));
        fieldSerializers.put(Character.class, (o, stream) -> stream.writeChar((Character) o));

        fieldSerializers.put(String.class, (o, stream) -> stream.writeUTF((String) o));

        fieldSerializers.put(List.class, (o, stream) -> {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) o;
            SerializationUtils.serializeList(list, this, stream);
        });
    }

    @Override
    public void serialize(Object value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = SerializationUtils.getDataOutputStream(stream);

        SerializeWith annotation = value.getClass().getDeclaredAnnotation(SerializeWith.class);
        if (annotation != null) {
            try {
                @SuppressWarnings("unchecked")
                Serializer<Object> serializer = (Serializer<Object>) annotation.value().newInstance();
                serializer.serialize(value, dataOutputStream);
                return;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(
                    "Serializer class " + annotation.value() + " cannot be instantiated"
                );
            }
        }

        if (fieldSerializers.containsKey(value.getClass())) {
            fieldSerializers.get(value.getClass()).serialize(value, dataOutputStream);
            return;
        }

        Field[] fields = value.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (primitivesFieldSerializers.containsKey(field.getType())) {
                primitivesFieldSerializers.get(field.getType()).serializeWithAccess(value, field, dataOutputStream);
            } else if (field.getType().isAssignableFrom(List.class)) {
                fieldSerializers.get(List.class).serializeWithAccess(value, field, dataOutputStream);
            } else if (fieldSerializers.containsKey(field.getType())) {
                fieldSerializers.get(field.getType()).serializeWithAccess(value, field, dataOutputStream);
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
                serialize(field.get(object), stream);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Cannot serialize field " + field + " of " + object, e);
            }
        }

        void serialize(Object object, DataOutputStream stream) throws IOException;
    }


    @FunctionalInterface
    interface PrimitiveFieldSerializer {
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
