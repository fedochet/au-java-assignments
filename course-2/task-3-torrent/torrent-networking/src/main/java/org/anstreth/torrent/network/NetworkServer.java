package org.anstreth.torrent.network;

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
