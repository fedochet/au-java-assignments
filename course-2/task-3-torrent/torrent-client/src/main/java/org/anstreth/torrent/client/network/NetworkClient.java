package org.anstreth.torrent.client.network;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;

import java.io.IOException;

public interface NetworkClient {
    <T> void addSerializer(Class<T> clazz, Serializer<T> serializer);
    <T> void addDeserializer(Class<T> clazz, Deserializer<T> serializer);
    <T, M> M makeRequest(byte routeMarker, T request, Class<M> responseClass) throws IOException;
}
