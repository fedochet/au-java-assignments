package org.anstreth.torrent.tracker.network;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;

import java.util.function.Function;

public interface NetworkServer {
    <T, M> void registerMessageHandler(byte routeMarker,
                                       Class<T> requestClass,
                                       Function<T, M> handler);

    <T, M> void registerRequestHandler(byte routeMarker,
                                       Class<T> requestClass,
                                       Function<Request<T>, M> handler);

    void run();
}
