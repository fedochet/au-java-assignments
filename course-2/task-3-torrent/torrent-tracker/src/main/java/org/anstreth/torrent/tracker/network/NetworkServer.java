package org.anstreth.torrent.tracker.network;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;

import java.util.function.Function;

public interface NetworkServer {
    <T, M> void registerMessageHandler(byte routeMarker,
                                       Deserializer<T> deserializer,
                                       Function<T, M> handler,
                                       Serializer<M> serializer);

    <T, M> void registerRequestHandler(byte routeMarker,
                                       Deserializer<T> deserializer,
                                       Function<Request<T>, M> handler,
                                       Serializer<M> serializer);

    void run();
}
