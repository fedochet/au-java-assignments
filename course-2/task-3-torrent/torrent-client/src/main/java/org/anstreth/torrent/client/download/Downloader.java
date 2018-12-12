package org.anstreth.torrent.client.download;

import org.anstreth.torrent.client.network.PeerClient;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Downloader implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Downloader.class);
    private static final int MAX_DOWNLOADS_AT_TIME = 4;

    private final Set<FilePart> currentDownloads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ExecutorService partsDownloader = Executors.newFixedThreadPool(MAX_DOWNLOADS_AT_TIME);
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
        partsDownloader.shutdown();
        updater.shutdown();
    }

    private void update() {
        logger.info("Performing updateSources");

        try {
            Set<FilePart> notDownloadedParts = localFilesManager.listFiles().stream()
                .flatMap(fileStorageDetails -> fileStorageDetails.getMissingParts().stream())
                .filter(part -> !currentDownloads.contains(part))
                .limit(MAX_DOWNLOADS_AT_TIME - currentDownloads.size())
                .collect(Collectors.toSet());

            Map<Integer, List<SourceInfo>> sources = fetchFilesSources(notDownloadedParts);

            logger.info("Parts to download: {}", notDownloadedParts.size());

            notDownloadedParts.stream()
                .filter(part -> !sources.get(part.getFileId()).isEmpty())
                .forEach(part -> submitJob(part, sources));

            logger.info("Update is finished");
        } catch (IOException e) {
            logger.error("Cannot fetch information about file parts", e);
        }
    }

    private Map<Integer, List<SourceInfo>> fetchFilesSources(Set<FilePart> parts) throws IOException {
        List<Integer> fileIds = parts.stream()
            .map(FilePart::getFileId)
            .distinct()
            .collect(Collectors.toList());

        Map<Integer, List<SourceInfo>> sources = new HashMap<>();
        for (Integer fileId : fileIds) {
            sources.put(fileId, trackerClient.getSources(fileId));
        }
        return sources;
    }

    private void submitJob(FilePart part, Map<Integer, List<SourceInfo>> fileSources) {
        List<SourceInfo> sources = fileSources.get(part.getFileId());
        if (!sources.isEmpty()) {
            partsDownloader.submit(new DownloadJob(part, sources));
        }
    }

    private class DownloadJob implements Runnable {
        private final FilePart part;
        private final List<SourceInfo> sources;

        private DownloadJob(FilePart part, List<SourceInfo> sources) {
            this.part = part;
            this.sources = sources;
            currentDownloads.add(part);
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

            logger.info("Downloading {} from {}", part, source);

            try (OutputStream outputStream = localFilesManager.openForWriting(part);
                 InputStream inputStream = client.getPart(part.getFileId(), part.getNumber())) {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                logger.error(String.format("Cannot download part %s from %s!", part, sources), e);
                return;
            }

            try {
                localFilesManager.finishFilePart(part);
                logger.info("Downloading part {} from {} is done", part, source);
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
                    logger.warn("Error connecting to client with source = " + source, e);
                }
            }

            return Optional.empty();
        }
    }
}
