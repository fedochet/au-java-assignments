package org.anstreth.torrent.client;

import org.anstreth.torrent.client.download.Downloader;
import org.anstreth.torrent.client.network.PeerServer;
import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.client.storage.LocalFilesManagerImpl;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.anstreth.torrent.client.ClientMain.*;

/**
 * Class to start and coordinate all parts of client application.
 */
public class ClientCli implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(ClientCli.class);

    private final TrackerClient trackerClient;
    private final ClientArgs clientArgs;
    private final LocalFilesManager localFilesManager;
    private final Downloader downloader;
    private final PeerServer server;

    ClientCli(ClientArgs clientArgs) throws IOException {
        this.clientArgs = clientArgs;
        trackerClient = new TrackerClient(clientArgs.trackerAddress, clientArgs.trackerPort);
        localFilesManager = new LocalFilesManagerImpl(PART_SIZE, DOWNLOADS);
        downloader = new Downloader(trackerClient, localFilesManager, UPDATE_PERIOD);
        server = new PeerServer(clientArgs.clientPort, localFilesManager);
    }

    @Override
    public void close() throws IOException {
        try (Downloader d = downloader; PeerServer s = server) {
            logger.info("Shutting down client CLI...");
        }

        logger.info("CLI is shut down");
    }

    List<FileInfo> listFiles() throws IOException {
        return trackerClient.listFiles();
    }

    int uploadFile(Path file) throws IOException {
        int fileId = trackerClient.addFile(file.getFileName().toString(), Files.size(file));
        trackerClient.updateSources(clientArgs.clientPort, Collections.singletonList(fileId));
        localFilesManager.registerFile(fileId, file.toAbsolutePath());

        return fileId;
    }

    void downloadFile(int fileId) throws IOException {
        if (localFilesManager.fileIsPresent(fileId)) {
            System.out.println("File with id " + fileId + " is already present as local file!");
            return;
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

    static ClientArgs parseArgs(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        clientArgs.clientPort = Short.parseShort(args[0]);
        return clientArgs;
    }

    static class ClientArgs {
        short clientPort;
        InetAddress trackerAddress = InetAddress.getLoopbackAddress();
        short trackerPort = SERVER_PORT;
    }
}
