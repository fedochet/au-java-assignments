package org.anstreth.torrent.serialization;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class ReflectiveDeserializerFabric {
    private static final Map<Class<?>, DataDeserializer> fieldDeserializers = new HashMap<>();

    static {
        fieldDeserializers.put(int.class, DataInputStream::readInt);
        fieldDeserializers.put(long.class, DataInputStream::readLong);
        fieldDeserializers.put(double.class, DataInputStream::readDouble);
        fieldDeserializers.put(float.class, DataInputStream::readFloat);
        fieldDeserializers.put(short.class, DataInputStream::readShort);
        fieldDeserializers.put(boolean.class, DataInputStream::readBoolean);
        fieldDeserializers.put(byte.class, DataInputStream::readByte);
        fieldDeserializers.put(char.class, DataInputStream::readChar);

        fieldDeserializers.put(Integer.class, DataInputStream::readInt);
        fieldDeserializers.put(Long.class, DataInputStream::readLong);
        fieldDeserializers.put(Double.class, DataInputStream::readDouble);
        fieldDeserializers.put(Float.class, DataInputStream::readFloat);
        fieldDeserializers.put(Short.class, DataInputStream::readShort);
        fieldDeserializers.put(Boolean.class, DataInputStream::readBoolean);
        fieldDeserializers.put(Byte.class, DataInputStream::readByte);
        fieldDeserializers.put(Character.class, DataInputStream::readChar);

        fieldDeserializers.put(String.class, DataInput::readUTF);
    }


    public static <T> Deserializer<T> createForClass(Class<T> clazz) {
        return stream -> {
            DataInputStream dataOutputStream = SerializationUtils.getDataInputStream(stream);

            if (fieldDeserializers.containsKey(clazz)) {
                return clazz.cast(fieldDeserializers.get(clazz).deserialize(dataOutputStream));
            }

            Field[] fields = clazz.getDeclaredFields();
            Class<?>[] fieldClasses = Arrays.stream(fields).map(Field::getType).toArray(Class[]::new);
            Constructor<T> declaredConstructor;

            try {
                declaredConstructor = clazz.getDeclaredConstructor(fieldClasses);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No constructor from all field on class " + clazz);
            }

            List<Object> arguments = deserializeFields(fields, dataOutputStream);

            try {
                return declaredConstructor.newInstance(arguments.toArray());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException("Cannot create instance of class " + clazz, e);
            }
        };
    }

    private static List<Object> deserializeFields(Field[] fields, DataInputStream dataOutputStream) throws IOException {
        List<Object> arguments = new ArrayList<>();

        for (Field field : fields) {
            DataDeserializer fieldDeserializer = fieldDeserializers.get(field.getType());
            if (fieldDeserializer != null) {
                Object object = fieldDeserializer.deserialize(dataOutputStream);
                arguments.add(object);
            } else if (field.getType().isAssignableFrom(List.class)) {
                arguments.add(deserializeList(field, dataOutputStream));
            } else {
                Deserializer<?> deserializer = createForClass(field.getType());
                arguments.add(deserializer.deserialize(dataOutputStream));
            }
        }

        return arguments;
    }

    private static List<?> deserializeList(Field field, DataInputStream dataOutputStream) throws IOException {
        ParameterizedType genericType = (ParameterizedType) field.getGenericType();
        Type listParameterType = genericType.getActualTypeArguments()[0];
        Class<?> parameterClass = findClass(listParameterType.getTypeName());

        return SerializationUtils.deserializeList(createForClass(parameterClass), dataOutputStream);
    }

    private static Class<?> findClass(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                "No class with name " + typeName + " is found, cannot deserialize!",
                e
            );
        }
    }

    @FunctionalInterface
    private interface DataDeserializer {
        Object deserialize(DataInputStream stream) throws IOException;
    }
}
