package org.anstreth.torrent.client;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;

class ClientArgs {
    private static final long DOWNLOADER_UPDATE_PERIOD = 10 * 1000; // 10 seconds
    private static final long SOURCES_UPDATE_PERIOD = 10 * 1000; // 10 seconds

    private static final long PART_SIZE = 1024 * 1024; // 1 mb

    private static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path DOWNLOADS = CURRENT_DIR.resolve("downloads");
    private static final int TRACKER_DEFAULT_PORT = 8081;

    short clientPort;
    short trackerPort = TRACKER_DEFAULT_PORT;
    InetAddress trackerAddress = InetAddress.getLoopbackAddress();

    long partSize = PART_SIZE;
    Path downloadsDir = DOWNLOADS.toAbsolutePath();

    long downloaderUpdatePeriodMs = DOWNLOADER_UPDATE_PERIOD;
    long sourcesUpdatePeriodMs = SOURCES_UPDATE_PERIOD;

    static ClientArgs parseArgs(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        clientArgs.clientPort = Short.parseShort(args[0]);
        return clientArgs;
    }
}
