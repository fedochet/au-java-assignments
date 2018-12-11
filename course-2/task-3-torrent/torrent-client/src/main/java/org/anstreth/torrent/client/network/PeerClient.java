package org.anstreth.torrent.client.network;

import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.response.StatResponse;
import org.anstreth.torrent.network.NetworkClient;
import org.anstreth.torrent.network.NetworkClientImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

import static org.anstreth.torrent.client.request.ClientRequestMarkers.GET_MARKER;
import static org.anstreth.torrent.client.request.ClientRequestMarkers.STAT_MARKER;

public class PeerClient {
    private final NetworkClient networkClient;

    public PeerClient(InetAddress address, int port) {
        networkClient = new NetworkClientImpl(address, port);
    }

    public List<Integer> getParts(int fileId) throws IOException {
        StatResponse response = networkClient.makeRequest(
            STAT_MARKER,
            new StatRequest(fileId),
            StatResponse.class
        );

        return response.getPartsNumbers();
    }

    public InputStream getPart(int fileId, int partNumber) throws IOException {
        return networkClient.makeRawRequest(GET_MARKER, new GetRequest(fileId, partNumber));
    }
}
