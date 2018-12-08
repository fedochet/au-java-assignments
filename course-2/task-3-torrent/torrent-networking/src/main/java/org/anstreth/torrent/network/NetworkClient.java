package org.anstreth.torrent.network;

import java.io.IOException;
import java.io.InputStream;

public interface NetworkClient {
    <T, M> M makeRequest(byte routeMarker, T request, Class<M> responseClass) throws IOException;
    <T> InputStream makeRawRequest(byte routeMarker, T request) throws IOException;
}
