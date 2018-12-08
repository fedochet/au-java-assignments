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
        TrackerClient client = new TrackerClientImpl(InetAddress.getByName("localhost"), (short) 8081);

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

}
