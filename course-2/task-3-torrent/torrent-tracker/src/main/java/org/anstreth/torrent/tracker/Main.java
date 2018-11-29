package org.anstreth.torrent.tracker;

import java.io.IOException;

public class Main {
    private static final int SERVER_PORT = 8081;

    public static void main(String[] args) throws IOException {
        TrackerServer trackerServer = new TrackerServer(SERVER_PORT, new FilePersistentTrackerController());
        trackerServer.run();
    }
}
