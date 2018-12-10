package org.anstreth.torrent.client.download;

import org.anstreth.torrent.client.network.PeerClientImpl;
import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.storage.FilePart;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.tracker.response.SourceInfo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Downloader {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    private final Set<FilePart> currentDownloads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ExecutorService downloader = Executors.newCachedThreadPool();
    private final ScheduledExecutorService updater = Executors.newScheduledThreadPool(1);

    private final LocalFilesManager localFilesManager;
    private final TrackerClient trackerClient;

    public Downloader(TrackerClient trackerClient, LocalFilesManager localFilesManager, long updatePeriod) {
        this.localFilesManager = localFilesManager;
        this.trackerClient = trackerClient;
        updater.scheduleAtFixedRate(this::update, 0, updatePeriod, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        downloader.shutdown();
        updater.shutdown();
    }

    private void update() {
        logger.info("Performing update");
        Set<FilePart> notDownloadedParts;

        try {
            notDownloadedParts = localFilesManager.listFiles().stream()
                .flatMap(fileStorageDetails -> fileStorageDetails.missingParts().stream())
                .filter(part -> !currentDownloads.contains(part))
                .collect(Collectors.toSet());
        } catch (IOException e) {
            logger.error("Cannot fetch inforation about file parts", e);
            return;
        }

        logger.info("Parts to download: {}", notDownloadedParts.size());

        for (FilePart part : notDownloadedParts) {
            List<SourceInfo> sources;

            try {
                sources = trackerClient.getSources(part.getFileId());
            } catch (IOException e) {
                logger.error("Cannot fetch information about part " + part, e);
                break;
            }

            Optional<SourceInfo> randomElement = getRandomElement(sources);
            currentDownloads.add(part);
            randomElement.ifPresent(sourceInfo ->
                downloader.submit(new DownloadJob(part, sourceInfo))
            );
        }

        logger.info("Update is finished");
    }

    private class DownloadJob implements Runnable {
        private final FilePart part;
        private final SourceInfo sourceInfo;
        private final PeerClientImpl client;

        private DownloadJob(FilePart part, SourceInfo sourceInfo) {
            this.part = part;
            this.sourceInfo = sourceInfo;
            client = new PeerClientImpl(sourceInfo.getAddress(), sourceInfo.getPort());
        }

        @Override
        public void run() {
            try (OutputStream outputStream = localFilesManager.openForWriting(part);
                 InputStream inputStream = client.getPart(part.getFileId(), part.getNumber())) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                logger.error(String.format("Cannot download part %s from %s!", part, sourceInfo), e);

                currentDownloads.remove(part);
                logger.info("Part " + part + " will be available for downloading");

                return;
            }

            try {
                localFilesManager.finishFilePart(part);
            } catch (IOException e) {
                logger.error(String.format("Cannot mark part %s as finished", part), e);
            } finally {
                currentDownloads.remove(part);
            }
        }
    }

    private <T> Optional<T> getRandomElement(List<T> sources) {
        if (sources.isEmpty()) return Optional.empty();

        int index = ThreadLocalRandom.current().nextInt(sources.size()) % sources.size();
        return Optional.of(sources.get(index));
    }
}
