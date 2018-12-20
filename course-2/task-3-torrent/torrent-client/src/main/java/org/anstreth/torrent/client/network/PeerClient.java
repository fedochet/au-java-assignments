package org.anstreth.torrent.client.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

public class PeerClient {
    private final InetAddress address;
    private final short port;

    public PeerClient(InetAddress address, short port) {
        this.address = address;
        this.port = port;
    }

    public List<Integer> getParts(int fileId) throws IOException {
        try (PeerClientConnection connection = new PeerClientConnection(address, port)) {
            return connection.getParts(fileId);
        }
    }

    public InputStream getPart(int fileId, int partNumber) throws IOException {
        return new PeerClientConnection(address, port).getPart(fileId, partNumber);
    }
}
