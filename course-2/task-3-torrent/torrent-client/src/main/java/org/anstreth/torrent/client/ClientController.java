package org.anstreth.torrent.client;

import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.response.StatResponse;

import java.io.IOException;
import java.io.InputStream;

public interface ClientController {
    InputStream handleGetRequest(GetRequest request) throws IOException;
    StatResponse handleStatRequest(StatRequest request) throws IOException;
}
