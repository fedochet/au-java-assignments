package org.anstreth.torrent.client;

import org.anstreth.torrent.client.network.TrackerClient;
import org.anstreth.torrent.client.network.TrackerClientImpl;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: <port>");
        }

        ClientArgs clientArgs = parseArgs(args);
        TrackerClient client = new TrackerClientImpl(clientArgs.trackerAddress, clientArgs.trackerPort);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String command = scanner.next();

            switch (command) {
                case "list": {
                    List<FileInfo> deserialize = client.listFiles();

                    deserialize.forEach(System.out::println);
                    break;
                }

                case "add": {
                    String fileName = scanner.next();
                    long size = scanner.nextLong();

                    int fileId = client.addFile(fileName, size);

                    System.out.printf("File with id %s is added\n", fileId);
                    break;
                }

                case "sources": {
                    int id = scanner.nextInt();
                    List<SourceInfo> sourcesResponse = client.getSources(id);

                    sourcesResponse.forEach(System.out::println);
                    break;
                }

                case "exit":
                    return;
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
