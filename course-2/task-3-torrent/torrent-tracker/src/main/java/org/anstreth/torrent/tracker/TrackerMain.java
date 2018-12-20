package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.network.TrackerServer;
import org.anstreth.torrent.tracker.repository.InMemoryFileSourcesRepository;
import org.anstreth.torrent.tracker.repository.PersistentFileInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TrackerMain {
    private static final Logger log = LoggerFactory.getLogger(TrackerMain.class);

    private static final short SERVER_PORT = 8081;
    private static final Path FILE_INFO_STORAGE = Paths.get("tracker.files");

    private static final Duration SOURCE_EXPIRATION_TIME = Duration.of(5, ChronoUnit.MINUTES);
    private static final Duration SOURCES_CHECKS_PERIOD = Duration.of(10, ChronoUnit.SECONDS);

    public static void main(String[] args) throws IOException {
        log.debug("Starting torrent tracker server.");

        if (!Files.exists(FILE_INFO_STORAGE)) {
            Files.createFile(FILE_INFO_STORAGE);
        }

        TrackerController trackerController = new TrackerControllerImpl(
            new PersistentFileInfoRepository(FILE_INFO_STORAGE),
            new InMemoryFileSourcesRepository(SOURCE_EXPIRATION_TIME, SOURCES_CHECKS_PERIOD)
        );

        try (TrackerServer ignored = new TrackerServer(SERVER_PORT, trackerController)) {
            System.out.println("Press any key to stop server");
            System.in.read();
            System.exit(0);
        }
    }
}
