package org.anstreth.torrent.client;

import org.anstreth.torrent.client.storage.FilePartsDetails;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
    private static Logger logger = LoggerFactory.getLogger(ClientMain.class);

    private static final String LIST_CMD = "list";
    private static final String UPLOAD_CMD = "upload";
    private static final String STATS_CMD = "stats";
    private static final String DOWNLOAD_CMD = "download";
    private static final String SOURCES_CMD = "sources";
    private static final String EXIT_CMD = "exit";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <port>");
            System.exit(1);
        }

        ClientArgs clientArgs = ClientArgs.parseArgs(args);

        if (Files.notExists(clientArgs.downloadsDir)) {
            Files.createDirectories(clientArgs.downloadsDir);
        }

        try (ClientCli client = new ClientCli(clientArgs)) {
            handleUserCommands(client);
        } catch (Exception e) {
            logger.error("Unexpected error happened during command handling", e);
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void handleUserCommands(ClientCli client) throws IOException {
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String command = scanner.next();

                switch (command) {
                    case LIST_CMD: {
                        List<FileInfo> files = client.listFiles();

                        System.out.println("Totally files on tracker: " + files.size());
                        for (FileInfo file : files) {
                            System.out.printf(
                                "\t%s (id: %d, size: %d bytes)%n", file.getName(), file.getId(), file.getSize()
                            );
                        }

                        break;
                    }

                    case UPLOAD_CMD: {
                        String fileLocation = scanner.next();
                        Path file = Paths.get(fileLocation);
                        int fileId = client.uploadFile(file.toAbsolutePath());

                        System.out.printf(
                            "File %s is successfully added to tracker with id %d!%n", file, fileId
                        );

                        break;
                    }

                    case STATS_CMD: {
                        List<FilePartsDetails> filePartsDetails = client.listLocalFiles();
                        System.out.printf("There are %s local files%n", filePartsDetails.size());
                        for (FilePartsDetails filePartsDetail : filePartsDetails) {
                            System.out.printf(
                                "\t%s (id: %d, downloaded parts: %d/%d)%n",
                                filePartsDetail.getFile().getFileName(),
                                filePartsDetail.getFileId(),
                                filePartsDetail.getReadyParts().size(),
                                filePartsDetail.getNumberOfParts()
                            );
                        }
                        break;
                    }

                    case DOWNLOAD_CMD: {
                        int fileId = scanner.nextInt();

                        try {
                            client.downloadFile(fileId);
                            System.out.printf("File with id %d will be dawnloaded eventually...%n", fileId);
                        } catch (IllegalArgumentException e) {
                            System.err.printf("Cannot start downloading %d: %s%n", fileId, e.getMessage());
                        }

                        break;
                    }

                    case SOURCES_CMD: {
                        int fileId = scanner.nextInt();
                        List<SourceInfo> sources = client.getFileSources(fileId);

                        System.out.printf("There are %d sources for file with id %d.%n", sources.size(), fileId);
                        for (SourceInfo source : sources) {
                            System.out.printf("\tAddress: %s, port: %d%n", source.getAddress(), source.getPort());
                        }
                        break;
                    }

                    case EXIT_CMD:
                        System.out.println("Quitting client");
                        return;

                    default:
                        System.err.printf("Unexpected command %s%n", command);
                }
            } catch (ConnectException e) {
                logger.warn("Cannot connect to tracker", e);
                System.err.println("Connection to tracker is refused, maybe it is shut down?");
            }
        }
    }
}
