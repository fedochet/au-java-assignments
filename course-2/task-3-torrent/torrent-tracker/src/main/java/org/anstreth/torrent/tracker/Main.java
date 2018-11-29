package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.request.serialization.ListRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UpdateRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UploadRequestDeserializer;
import org.anstreth.torrent.tracker.response.serialization.ListResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.SourcesResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.UpdateResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.UploadResponseSerializer;

import java.io.IOException;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;
import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.UPDATE_REQUEST;

public class Main {
    private static final int SERVER_PORT = 8081;

    public static void main(String[] args) throws IOException {
        TrackerServer trackerServer = new TrackerServer(SERVER_PORT);
        TrackerController trackerController = new FilePersistentTrackerController();

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
