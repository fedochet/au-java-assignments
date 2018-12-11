package org.anstreth.torrent.client.download;

import org.anstreth.torrent.client.network.PeerClient;
import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.storage.FilePart;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.tracker.response.SourceInfo;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Downloader implements Closeable {
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

    @Override
    public void close() {
        downloader.shutdown();
        updater.shutdown();
    }

    private void update() {
        logger.info("Performing update");
        Set<FilePart> notDownloadedParts;

        try {
            notDownloadedParts = localFilesManager.listFiles().stream()
                .flatMap(fileStorageDetails -> fileStorageDetails.getMissingParts().stream())
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

            if (!sources.isEmpty()) {
                downloader.submit(new DownloadJob(part, sources));
            }
        }

        logger.info("Update is finished");
    }

    private class DownloadJob implements Runnable {
        private final FilePart part;
        private final List<SourceInfo> sources;

        private DownloadJob(FilePart part, List<SourceInfo> sources) {
            this.part = part;
            this.sources = sources;
        }

        @Override
        public void run() {
            try {
                downloadPart();
            } finally {
                currentDownloads.remove(part);
            }
        }

        private void downloadPart() {
            Optional<SourceInfo> possibleSource = selectSource(sources);

            if (!possibleSource.isPresent()) {
                logger.info("No sources for " + part);
                return;
            }

            SourceInfo source = possibleSource.get();
            PeerClient client = new PeerClient(source.getAddress(), source.getPort());

            try (OutputStream outputStream = localFilesManager.openForWriting(part);
                 InputStream inputStream = client.getPart(part.getFileId(), part.getNumber())) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                logger.error(String.format("Cannot download part %s from %s!", part, sources), e);

                return;
            }

            try {
                localFilesManager.finishFilePart(part);
            } catch (IOException e) {
                logger.error(String.format("Cannot mark part %s as finished", part), e);
            }
        }

        private Optional<SourceInfo> selectSource(List<SourceInfo> sources) {
            for (SourceInfo source : sources) {
                PeerClient peerClient = new PeerClient(source.getAddress(), source.getPort());
                try {
                    List<Integer> parts = peerClient.getParts(part.getFileId());
                    if (parts.contains(part.getNumber())) {
                        return Optional.of(source);
                    }
                } catch (IOException e) {
                    logger.error("Error connecting to client with source = " + source);
                }
            }

            return Optional.empty();
        }
    }
}
