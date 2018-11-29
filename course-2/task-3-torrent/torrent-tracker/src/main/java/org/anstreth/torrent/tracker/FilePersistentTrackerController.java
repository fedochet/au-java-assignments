package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.network.Request;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FilePersistentTrackerController implements TrackerController {
    private final Map<Integer, FileInfo> files = new HashMap<>();
    private final Map<Integer, List<SourceInfo>> sources = new HashMap<>();

    private final AtomicInteger nextId = new AtomicInteger(0);

    @Override
    public UploadResponse handle(UploadRequest request) {
        int fileId = generateNextId();
        FileInfo fileInfo = new FileInfo(fileId, request.getFileName(), request.getFileSize());
        files.put(fileId, fileInfo);
        return new UploadResponse(fileId);
    }

    @Override
    public ListResponse handle(ListRequest request) {
        List<FileInfo> fileInfos = new ArrayList<>(files.values());
        fileInfos.sort(Comparator.comparing(FileInfo::getId));
        return new ListResponse(fileInfos);
    }

    @Override
    public SourcesResponse handle(SourcesRequest request) {
        List<SourceInfo> fileSources = sources.getOrDefault(request.getFileId(), Collections.emptyList());
        return new SourcesResponse(fileSources);
    }

    @Override
    public UpdateResponse handle(Request<UpdateRequest> request) {
        for (Integer fileId : request.getBody().getFileIds()) {
            List<SourceInfo> infos = sources.computeIfAbsent(fileId, id -> new ArrayList<>());
            infos.add(new SourceInfo(request.getInetAddress(), request.getBody().getPort()));
        }

        return new UpdateResponse(true);
    }

    private int generateNextId() {
        return nextId.getAndDecrement();
    }

}
