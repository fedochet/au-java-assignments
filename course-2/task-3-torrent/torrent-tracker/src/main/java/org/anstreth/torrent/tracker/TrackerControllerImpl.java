package org.anstreth.torrent.tracker;

import org.anstreth.torrent.network.Request;
import org.anstreth.torrent.tracker.repository.FileInfoRepository;
import org.anstreth.torrent.tracker.repository.FileSourcesRepository;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrackerControllerImpl implements TrackerController {
    private final FileInfoRepository fileInfoRepository;
    private final FileSourcesRepository sourcesRepository;

    TrackerControllerImpl(@NotNull FileInfoRepository fileInfoRepository,
                          @NotNull FileSourcesRepository sourcesRepository) {
        this.fileInfoRepository = fileInfoRepository;
        this.sourcesRepository = sourcesRepository;
    }

    @NotNull
    @Override
    public UploadResponse handle(@NotNull UploadRequest request) {
        int newFileId = fileInfoRepository.addFile(request.getFileName(), request.getFileSize());
        return new UploadResponse(newFileId);
    }

    @NotNull
    @Override
    public ListResponse handle(@NotNull ListRequest request) {
        return new ListResponse(fileInfoRepository.getAllFiles());
    }

    @NotNull
    @Override
    public SourcesResponse handle(@NotNull SourcesRequest request) {
        List<SourceInfo> fileSources = sourcesRepository.getFileSources(request.getFileId());
        return new SourcesResponse(fileSources);
    }

    @NotNull
    @Override
    public UpdateResponse handle(@NotNull Request<UpdateRequest> request) {
        UpdateRequest requestBody = request.getBody();
        for (Integer fileId : requestBody.getFileIds()) {
            sourcesRepository.addFileSource(fileId, request.getInetAddress(), requestBody.getPort());
        }

        return new UpdateResponse(true);
    }
}
