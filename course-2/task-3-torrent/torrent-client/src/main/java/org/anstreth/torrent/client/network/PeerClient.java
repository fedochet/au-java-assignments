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

    /**
     * Returns inputStream from which file part can be read.
     *
     * User must close this input stream in order to close connection to the peer client.
     *
     * @param fileId id of target file
     * @param partNumber part of target file
     * @return inputStream, directly connected to the peer's connection socket
     * @throws IOException if something goes wrong
     */
    public InputStream getPart(int fileId, int partNumber) throws IOException {
        return new PeerClientConnection(address, port).getPart(fileId, partNumber);
    }
}
