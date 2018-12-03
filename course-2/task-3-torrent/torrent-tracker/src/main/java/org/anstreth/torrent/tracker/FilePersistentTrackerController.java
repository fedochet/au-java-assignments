package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.network.Request;
import org.anstreth.torrent.tracker.repository.FileInfoRepository;
import org.anstreth.torrent.tracker.repository.FileSourcesRepository;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.*;

import java.util.List;

public class FilePersistentTrackerController implements TrackerController {
    private final FileInfoRepository fileInfoRepository;
    private final FileSourcesRepository sourcesRepository;

    FilePersistentTrackerController(FileInfoRepository fileInfoRepository, FileSourcesRepository sourcesRepository) {
        this.fileInfoRepository = fileInfoRepository;
        this.sourcesRepository = sourcesRepository;
    }

    @Override
    public UploadResponse handle(UploadRequest request) {
        int newFileId = fileInfoRepository.addFile(request.getFileName(), request.getFileSize());
        return new UploadResponse(newFileId);
    }

    @Override
    public ListResponse handle(ListRequest request) {
        return new ListResponse(fileInfoRepository.getAllFiles());
    }

    @Override
    public SourcesResponse handle(SourcesRequest request) {
        List<SourceInfo> fileSources = sourcesRepository.getFileSources(request.getFileId());
        return new SourcesResponse(fileSources);
    }

    @Override
    public UpdateResponse handle(Request<UpdateRequest> request) {
        UpdateRequest requestBody = request.getBody();
        for (Integer fileId : requestBody.getFileIds()) {
            sourcesRepository.addFileSource(fileId, request.getInetAddress(), requestBody.getPort());
        }

        return new UpdateResponse(true);
    }
}
