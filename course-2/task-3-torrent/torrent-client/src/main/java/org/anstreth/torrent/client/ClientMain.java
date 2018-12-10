package org.anstreth.torrent.client;

import org.anstreth.torrent.client.download.Downloader;
import org.anstreth.torrent.client.network.PeerServer;
import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.network.TrackerClientImpl;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.client.storage.LocalFilesManagerImpl;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class ClientMain {

    private static final long PART_SIZE = 1024 * 1024; // 1 mb

    private static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path DOWNLOADS = CURRENT_DIR.resolve("downloads");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <port>");
            System.exit(1);
        }

        if (Files.notExists(DOWNLOADS)) {
            Files.createDirectories(DOWNLOADS);
        }

        ClientArgs clientArgs = parseArgs(args);

        TrackerClient client = new TrackerClientImpl(clientArgs.trackerAddress, clientArgs.trackerPort);
        LocalFilesManager localFilesManager = new LocalFilesManagerImpl(PART_SIZE, DOWNLOADS);

        Scanner scanner = new Scanner(System.in);

        try (Downloader downloader = new Downloader(client, localFilesManager, 10 * 1000);
             PeerServer server = new PeerServer(clientArgs.clientPort, localFilesManager)) {

            while (scanner.hasNext()) {
                String command = scanner.next();

                switch (command) {
                    case "list": {
                        List<FileInfo> deserialize = client.listFiles();

                        deserialize.forEach(System.out::println);
                        break;
                    }

                    case "upload": {
                        String fileLocation = scanner.next();
                        Path file = Paths.get(fileLocation);

                        int fileId = client.addFile(file.getFileName().toString(), Files.size(file));
                        client.updateSources(clientArgs.clientPort, Collections.singletonList(fileId));
                        localFilesManager.registerFile(fileId, file.toAbsolutePath());

                        System.out.println(String.format("File with id %d is added", fileId));
                        break;
                    }

                    case "download": {
                        int fileId = scanner.nextInt();
                        List<FileInfo> files = client.listFiles();
                        FileInfo fileInfo = files.stream().filter(file -> file.getId() == fileId).findFirst().orElseThrow(() ->
                            new IllegalArgumentException("File with id " + fileId + " does not exist!")
                        );

                        localFilesManager.allocateFile(fileInfo.getId(), fileInfo.getName(), fileInfo.getSize());
                        break;
                    }

                    case "sources": {
                        int id = scanner.nextInt();
                        List<SourceInfo> sourcesResponse = client.getSources(id);

                        sourcesResponse.forEach(System.out::println);
                        break;
                    }

                    case "stats": {
                        localFilesManager.listFiles().forEach(System.out::println);
                        break;
                    }

                    case "exit":
                        return;

                    default:
                        System.err.println(String.format("Unexpected command %s", command));
                }
            }
        }
    }

    private static ClientArgs parseArgs(String[] args) {
        ClientArgs clientArgs = new ClientArgs();
        clientArgs.clientPort = Short.parseShort(args[0]);
        return clientArgs;
    }

    private static class ClientArgs {
        short clientPort;
        InetAddress trackerAddress = InetAddress.getLoopbackAddress();
        short trackerPort = 8081;
    }

}
