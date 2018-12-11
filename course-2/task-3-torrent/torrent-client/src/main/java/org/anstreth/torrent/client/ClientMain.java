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
    public static final int UPDATE_PERIOD = 10 * 1000; // 10 seconds
    public static final long PART_SIZE = 1024 * 1024; // 1 mb

    public static final Path CURRENT_DIR = Paths.get(System.getProperty("user.dir"));
    public static final Path DOWNLOADS = CURRENT_DIR.resolve("downloads");
    public static final int SERVER_PORT = 8081;

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <port>");
            System.exit(1);
        }

        if (Files.notExists(DOWNLOADS)) {
            Files.createDirectories(DOWNLOADS);
        }

        ClientCli.ClientArgs clientArgs = ClientCli.parseArgs(args);

        Scanner scanner = new Scanner(System.in);

        try (ClientCli client = new ClientCli(clientArgs)) {
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

                        int fileId = client.uploadFile(file);

                        System.out.println(String.format("File with id %d is added", fileId));
                        break;
                    }

                    case "download": {
                        int fileId = scanner.nextInt();

                        client.downloadFile(fileId);
                        break;
                    }

                    case "sources": {
                        int id = scanner.nextInt();
                        List<SourceInfo> sourcesResponse = client.getFileSources(id);
                        sourcesResponse.forEach(System.out::println);
                        break;
                    }

                    case "exit":
                        return;

                    default:
                        System.err.println(String.format("Unexpected command %s", command));
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
