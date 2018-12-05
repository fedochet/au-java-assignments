package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.network.TrackerServer;
import org.anstreth.torrent.tracker.repository.InMemoryFileSourcesRepository;
import org.anstreth.torrent.tracker.repository.PersistentFileInfoRepository;
import org.anstreth.torrent.tracker.request.serialization.ListRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UpdateRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UploadRequestDeserializer;
import org.anstreth.torrent.tracker.response.serialization.ListResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.SourcesResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.UpdateResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.UploadResponseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;
import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.UPDATE_REQUEST;

public class TrackerMain {
    private static final Logger log = LoggerFactory.getLogger(TrackerMain.class);

    private static final int SERVER_PORT = 8081;
    private static final Path FILE_INFO_STORAGE = Paths.get("tracker.files");

    public static void main(String[] args) throws IOException {
        log.debug("Starting torrent tracker server.");

        if (!Files.exists(FILE_INFO_STORAGE)) {
            Files.createFile(FILE_INFO_STORAGE);
        }

        TrackerServer trackerServer = new TrackerServer(SERVER_PORT);
        TrackerController trackerController = new TrackerControllerImpl(
            new PersistentFileInfoRepository(FILE_INFO_STORAGE),
            new InMemoryFileSourcesRepository()
        );

        trackerServer.registerMessageHandler(
            LIST_REQUEST,
            new ListRequestDeserializer(), trackerController::handle, new ListResponseSerializer()
        );

        trackerServer.registerMessageHandler(
            UPLOAD_REQUEST,
            new UploadRequestDeserializer(), trackerController::handle, new UploadResponseSerializer()
        );

        trackerServer.registerMessageHandler(
            SOURCES_REQUEST,
            new SourcesRequestDeserializer(), trackerController::handle, new SourcesResponseSerializer()
        );

        trackerServer.registerRequestHandler(
            UPDATE_REQUEST,
            new UpdateRequestDeserializer(), trackerController::handle, new UpdateResponseSerializer()
        );

        trackerServer.run();
    }
}
