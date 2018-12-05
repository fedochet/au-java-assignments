package org.anstreth.torrent.client.network;

import com.sun.istack.internal.NotNull;
import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NetworkClientImpl implements NetworkClient {
    private final String address;
    private final int field;

    private final Map<Class<?>, Serializer<?>> serializers = new HashMap<>();
    private final Map<Class<?>, Deserializer<?>> deserializers = new HashMap<>();

    public NetworkClientImpl(String address, int port) {
        this.address = address;
        this.field = port;
    }

    @Override
    public <T> void addSerializer(Class<T> clazz, Serializer<T> serializer) {
        if (serializers.containsKey(clazz)) {
            throw new IllegalArgumentException("Serializer for class " + clazz + " is already present!");
        }
        serializers.put(clazz, serializer);
    }

    @Override
    public <T> void addDeserializer(Class<T> clazz, Deserializer<T> deserializer) {
        if (deserializers.containsKey(clazz)) {
            throw new IllegalArgumentException("Deserializer for class " + clazz + " is already present!");
        }
        deserializers.put(clazz, deserializer);
    }

    @Override
    public <T, M> M makeRequest(byte routeMarker, T request, Class<M> responseClass) throws IOException {
        Serializer<T> requestSerializer = getDeserializerForClass(request);
        Deserializer<M> deserializer = getDeserializerForClass(responseClass);

        try (Socket localhost = new Socket(address, field)) {
            DataOutputStream outputStream = new DataOutputStream(localhost.getOutputStream());
            outputStream.writeByte(routeMarker);
            requestSerializer.serialize(request, outputStream);

            return deserializer.deserialize(localhost.getInputStream());
        }
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <M> Deserializer<M> getDeserializerForClass(Class<M> responseClass) {
        return Objects.requireNonNull((Deserializer<M>) deserializers.get(responseClass), () ->
            "No serializer for " + responseClass + " is present"
        );
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private <T> Serializer<T> getDeserializerForClass(T request) {
        return Objects.requireNonNull((Serializer<T>) serializers.get(request.getClass()), () ->
            "No deserializer for " + request.getClass() + " is present"
        );
    }
}
