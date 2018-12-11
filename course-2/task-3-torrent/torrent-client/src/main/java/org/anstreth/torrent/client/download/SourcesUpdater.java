package org.anstreth.torrent.client.download;

import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.storage.FilePartsDetails;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SourcesUpdater implements AutoCloseable {
    private Logger logger = LoggerFactory.getLogger(SourcesUpdater.class);

    private final ScheduledExecutorService updater = Executors.newScheduledThreadPool(1);
    private final TrackerClient trackerClient;
    private final short clientPort;
    private final long updateRate;

    private LocalFilesManager localFileManager;

    private SourcesUpdater(TrackerClient trackerClient, LocalFilesManager localFileManager, short clientPort, long updateRate) {
        this.trackerClient = trackerClient;
        this.localFileManager = localFileManager;
        this.clientPort = clientPort;
        this.updateRate = updateRate;
    }

    private SourcesUpdater start() {
        updater.scheduleAtFixedRate(this::updateSources, 0, updateRate, TimeUnit.MILLISECONDS);
        return this;
    }

    public void updateSources() {
        try {
            List<FilePartsDetails> filesParts = localFileManager.listFiles();
            List<Integer> availableFilesIds = filesParts.stream()
                .filter(parts -> !parts.getReadyParts().isEmpty())
                .map(FilePartsDetails::getFileId)
                .collect(Collectors.toList());

            if (availableFilesIds.isEmpty()) {
                logger.info("No files available on this client to be source of!");
            }

            boolean success = trackerClient.updateSources(clientPort, availableFilesIds);

            if (success) {
                logger.info("Sources of this client are successfully updated for: {}", availableFilesIds);
            } else {
                logger.warn("Sources of this client wasn't updated, returned status is error!");
            }
        } catch (Exception e) {
            logger.error("Error during trying to updateSources tracker sources", e);
        }
    }

    public static SourcesUpdater startUpdater(TrackerClient trackerClient, LocalFilesManager localFileManager, short port, long updateRate) {
        return new SourcesUpdater(trackerClient, localFileManager, port, updateRate).start();
    }

    @Override
    public void close() {
        updater.shutdown();
    }
}
