package org.anstreth.torrent.client.network;

import org.anstreth.torrent.network.NetworkClient;
import org.anstreth.torrent.network.NetworkClientImpl;
import org.anstreth.torrent.tracker.request.*;
import org.anstreth.torrent.tracker.response.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;

public class TrackerClient {
    private final NetworkClient networkClient;

    public TrackerClient(InetAddress address, short port) {
        networkClient = new NetworkClientImpl(address, port);
    }

    public int addFile(String file, long size) throws IOException {
        UploadResponse response = networkClient.makeRequest(
            UPLOAD_REQUEST,
            new UploadRequest(file, size),
            UploadResponse.class
        );

        return response.getFileId();
    }

    public List<FileInfo> listFiles() throws IOException {
        ListResponse response = networkClient.makeRequest(
            LIST_REQUEST,
            new ListRequest(),
            ListResponse.class
        );

        return response.getFiles();
    }

    public List<SourceInfo> getSources(int fileId) throws IOException {
        SourcesResponse response = networkClient.makeRequest(
            SOURCES_REQUEST,
            new SourcesRequest(fileId),
            SourcesResponse.class
        );

        return response.getAddresses();
    }

    public boolean updateSources(short port, List<Integer> fileIds) throws IOException {
        UpdateResponse response = networkClient.makeRequest(
            UPDATE_REQUEST,
            new UpdateRequest(port, fileIds),
            UpdateResponse.class
        );

        return response.isSuccessful();
    }
}
