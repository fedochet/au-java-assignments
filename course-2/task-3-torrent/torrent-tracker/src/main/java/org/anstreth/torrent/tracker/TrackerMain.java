package org.anstreth.torrent.tracker;

import org.anstreth.torrent.network.NetworkServer;
import org.anstreth.torrent.network.SingleThreadServer;
import org.anstreth.torrent.tracker.repository.InMemoryFileSourcesRepository;
import org.anstreth.torrent.tracker.repository.PersistentFileInfoRepository;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;

public class TrackerMain {
    private static final Logger log = LoggerFactory.getLogger(TrackerMain.class);

    private static final int SERVER_PORT = 8081;
    private static final Path FILE_INFO_STORAGE = Paths.get("tracker.files");

    public static void main(String[] args) throws IOException {
        log.debug("Starting torrent tracker server.");

        if (!Files.exists(FILE_INFO_STORAGE)) {
            Files.createFile(FILE_INFO_STORAGE);
        }

        NetworkServer trackerServer = new SingleThreadServer(SERVER_PORT);
        TrackerController trackerController = new TrackerControllerImpl(
            new PersistentFileInfoRepository(FILE_INFO_STORAGE),
            new InMemoryFileSourcesRepository()
        );

        trackerServer.registerMessageHandler(LIST_REQUEST, ListRequest.class, trackerController::handle);
        trackerServer.registerMessageHandler(UPLOAD_REQUEST, UploadRequest.class, trackerController::handle);
        trackerServer.registerMessageHandler(SOURCES_REQUEST, SourcesRequest.class, trackerController::handle);
        trackerServer.registerRequestHandler(UPDATE_REQUEST, UpdateRequest.class, trackerController::handle);

        trackerServer.run();
    }
}
