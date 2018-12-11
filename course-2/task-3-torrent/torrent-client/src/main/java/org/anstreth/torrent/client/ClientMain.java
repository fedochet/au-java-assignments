package org.anstreth.torrent.client;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <port>");
            System.exit(1);
        }

        ClientArgs clientArgs = ClientArgs.parseArgs(args);

        if (Files.notExists(clientArgs.downloadsDir)) {
            Files.createDirectories(clientArgs.downloadsDir);
        }

        Scanner scanner = new Scanner(System.in);

        try (ClientCli client = new ClientCli(clientArgs)) {
            while (scanner.hasNext()) {
                String command = scanner.next();

                switch (command) {
                    case "list": {
                        List<FileInfo> files = client.listFiles();

                        System.out.println("Totally files on tracker: " + files.size());
                        for (FileInfo file : files) {
                            System.out.printf(
                                "\t%s (id: %d, size: %d bytes)%n", file.getName(), file.getId(), file.getSize()
                            );
                        }

                        break;
                    }

                    case "upload": {
                        String fileLocation = scanner.next();
                        Path file = Paths.get(fileLocation);
                        int fileId = client.uploadFile(file.toAbsolutePath());

                        System.out.printf(
                            "File %s is successfully added to tracker with id %d!%n", file, fileId
                        );

                        break;
                    }

                    case "download": {
                        int fileId = scanner.nextInt();

                        try {
                            client.downloadFile(fileId);
                            System.out.printf("File with id %d will be dawnloaded eventually...%n", fileId);
                        } catch (IllegalArgumentException e) {
                            System.err.printf("Cannot start downloading %d: %s%n", fileId, e.getMessage());
                        }

                        break;
                    }

                    case "sources": {
                        int fileId = scanner.nextInt();
                        List<SourceInfo> sources = client.getFileSources(fileId);

                        System.out.printf("There are %d sources for file with id %d.%n", sources.size(), fileId);
                        for (SourceInfo source : sources) {
                            System.out.printf("\tAddress: %s, port: %d%n", source.getAddress(), source.getPort());
                        }
                        break;
                    }

                    case "exit":
                        System.out.println("Quitting client");
                        return;

                    default:
                        System.err.printf("Unexpected command %s%n", command);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
