package org.anstreth.torrent.client.network;

import java.io.IOException;

public interface NetworkClient {
    <T, M> M makeRequest(byte routeMarker, T request, Class<M> responseClass) throws IOException;
}
