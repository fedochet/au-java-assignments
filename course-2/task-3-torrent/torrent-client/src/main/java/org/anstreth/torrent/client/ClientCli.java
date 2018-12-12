package org.anstreth.torrent.client;

import org.anstreth.torrent.client.download.Downloader;
import org.anstreth.torrent.client.download.SourcesUpdater;
import org.anstreth.torrent.client.network.PeerServer;
import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.storage.FilePartsDetails;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.client.storage.LocalFilesManagerImpl;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Class to start and coordinate all parts of client application.
 */
public class ClientCli implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ClientCli.class);

    private final TrackerClient trackerClient;
    private final LocalFilesManager localFilesManager;
    private final Downloader downloader;
    private final SourcesUpdater updater;
    private final PeerServer server;

    ClientCli(ClientArgs args) throws IOException {
        trackerClient = new TrackerClient(args.trackerAddress, args.trackerPort);
        localFilesManager = new LocalFilesManagerImpl(args.partSize, args.downloadsDir);
        downloader = new Downloader(trackerClient, localFilesManager, args.downloaderUpdatePeriodMs);
        server = new PeerServer(args.clientPort, localFilesManager);
        updater = new SourcesUpdater(trackerClient, localFilesManager, args.clientPort, args.sourcesUpdatePeriodMs);
    }

    @Override
    public void close() throws IOException {
        updater.close();
        server.close();
        downloader.close();

        logger.info("CLI is shut down");
    }

    List<FileInfo> listFiles() throws IOException {
        return trackerClient.listFiles();
    }

    int uploadFile(Path file) throws IOException {
        int fileId = trackerClient.addFile(file.getFileName().toString(), Files.size(file));
        updater.updateSources();
        localFilesManager.registerFile(fileId, file.toAbsolutePath());

        return fileId;
    }

    void downloadFile(int fileId) throws IOException {
        if (localFilesManager.fileIsPresent(fileId)) {
            throw new IllegalArgumentException("File with id " + fileId + " is already present as local file!");
        }

        List<FileInfo> files = trackerClient.listFiles();
        FileInfo fileInfo = files.stream().filter(file -> file.getId() == fileId).findFirst().orElseThrow(() ->
            new IllegalArgumentException("File with id " + fileId + " does not exist!")
        );

        localFilesManager.allocateFile(fileInfo.getId(), fileInfo.getName(), fileInfo.getSize());
    }

    List<SourceInfo> getFileSources(int id) throws IOException {
        return trackerClient.getSources(id);
    }

    List<FilePartsDetails> listLocalFiles() throws IOException {
        return localFilesManager.listFiles();
    }
}
