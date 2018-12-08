package org.anstreth.torrent.client.network;

import org.anstreth.torrent.network.NetworkClient;
import org.anstreth.torrent.network.NetworkClientImpl;
import org.anstreth.torrent.tracker.request.*;
import org.anstreth.torrent.tracker.response.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;

public class TrackerClientImpl implements TrackerClient {
    private final NetworkClient netwrokClient;

    public TrackerClientImpl(InetAddress address, short port) {
        netwrokClient = new NetworkClientImpl(address, port);
    }

    @Override
    public int addFile(String file, long size) throws IOException {
        UploadResponse response = netwrokClient.makeRequest(
            UPLOAD_REQUEST,
            new UploadRequest(file, size),
            UploadResponse.class
        );

        return response.getFileId();
    }

    @Override
    public List<FileInfo> listFiles() throws IOException {
        ListResponse response = netwrokClient.makeRequest(
            LIST_REQUEST,
            new ListRequest(),
            ListResponse.class
        );

        return response.getFiles();
    }

    @Override
    public List<SourceInfo> getSources(int fileId) throws IOException {
        SourcesResponse response = netwrokClient.makeRequest(
            SOURCES_REQUEST,
            new SourcesRequest(fileId),
            SourcesResponse.class
        );

        return response.getAddresses();
    }

    @Override
    public boolean updateSources(short port, List<Integer> fileIds) throws IOException {
        UpdateResponse response = netwrokClient.makeRequest(
            UPDATE_REQUEST,
            new UpdateRequest(port, fileIds),
            UpdateResponse.class
        );

        return response.isSuccessful();
    }
}
