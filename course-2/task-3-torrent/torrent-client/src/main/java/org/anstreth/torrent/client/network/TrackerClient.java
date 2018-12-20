package org.anstreth.torrent.client.network;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

public class TrackerClient {
    private final InetAddress address;
    private final short port;

    public TrackerClient(InetAddress address, short port) {
        this.address = address;
        this.port = port;
    }

    public int addFile(String file, long size) throws IOException {
        try (TrackerClientConnection connection = connect()) {
            return connection.addFile(file, size);
        }
    }

    public List<FileInfo> listFiles() throws IOException {
        try (TrackerClientConnection connection = connect()) {
            return connection.listFiles();
        }
    }

    public List<SourceInfo> getSources(int fileId) throws IOException {
        try (TrackerClientConnection connection = connect()) {
            return connection.getSources(fileId);
        }
    }

    public boolean updateSources(short port, List<Integer> fileIds) throws IOException {
        try (TrackerClientConnection connection = connect()) {
            return connection.updateSources(port, fileIds);
        }
    }

    private TrackerClientConnection connect() throws IOException {
        return new TrackerClientConnection(address, port);
    }
}
