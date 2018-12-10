package org.anstreth.torrent.network;

import java.io.Closeable;
import java.util.function.Function;

public interface NetworkServer extends Closeable {
    <T, M> void registerMessageHandler(byte routeMarker,
                                       Class<T> requestClass,
                                       Function<T, M> handler);

    <T, M> void registerRequestHandler(byte routeMarker,
                                       Class<T> requestClass,
                                       Function<Request<T>, M> handler);

    void start();
}
