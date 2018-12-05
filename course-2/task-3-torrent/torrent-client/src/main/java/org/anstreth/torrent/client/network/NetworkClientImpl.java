package org.anstreth.torrent.client.network;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.ReflectiveDeserializerFabric;
import org.anstreth.torrent.serialization.ReflectiveSerializer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkClientImpl implements NetworkClient {
    private final String address;
    private final int field;
    private final ReflectiveSerializer reflectiveSerializer = new ReflectiveSerializer();

    public NetworkClientImpl(String address, int port) {
        this.address = address;
        this.field = port;
    }

    @Override
    public <T, M> M makeRequest(byte routeMarker, T request, Class<M> responseClass) throws IOException {
        Deserializer<M> deserializer = ReflectiveDeserializerFabric.createForClass(responseClass);

        try (Socket localhost = new Socket(address, field)) {
            DataOutputStream outputStream = new DataOutputStream(localhost.getOutputStream());
            outputStream.writeByte(routeMarker);
            reflectiveSerializer.serialize(request, outputStream);

            return deserializer.deserialize(localhost.getInputStream());
        }
    }

}
