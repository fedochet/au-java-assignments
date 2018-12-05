package org.anstreth.torrent.client;

import org.anstreth.torrent.network.NetworkClient;
import org.anstreth.torrent.network.NetworkClientImpl;
import org.anstreth.torrent.tracker.request.*;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.anstreth.torrent.tracker.response.SourcesResponse;
import org.anstreth.torrent.tracker.response.UpdateResponse;
import org.anstreth.torrent.tracker.response.UploadResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        NetworkClient client = new NetworkClientImpl("localhost", 8081);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String command = scanner.next();

            switch (command) {
                case "list":
                    handleListCommand(client);
                    break;

                case "add": {
                    String fileName = scanner.next();
                    long size = scanner.nextLong();
                    handleAddCommand(client, fileName, size);
                    break;
                }

                case "sources": {
                    int id = scanner.nextInt();
                    handleSourcesCommand(client, id);
                    break;
                }
            }
        }
    }

    private static void handleSourcesCommand(NetworkClient client, int id) throws IOException {
        SourcesResponse sourcesResponse = client.makeRequest(
            TrackerRequestMarker.SOURCES_REQUEST,
            new SourcesRequest(id),
            SourcesResponse.class
        );

        sourcesResponse.getAddresses().forEach(System.out::println);
    }

    private static void handleAddCommand(NetworkClient client, String fileName, long size) throws IOException {
        UploadResponse deserialize = client.makeRequest(
            TrackerRequestMarker.UPLOAD_REQUEST,
            new UploadRequest(fileName, size),
            UploadResponse.class
        );

        System.out.printf("File with id %s is added\n", deserialize.getFileId());

        client.makeRequest(
            TrackerRequestMarker.UPDATE_REQUEST,
            new UpdateRequest((short) 100, Collections.singletonList(deserialize.getFileId())),
            UpdateResponse.class
        );
    }

    private static void handleListCommand(NetworkClient client) throws IOException {
        ListResponse deserialize = client.makeRequest(
            TrackerRequestMarker.LIST_REQUEST,
            new ListRequest(),
            ListResponse.class
        );
        deserialize.getFiles().forEach(System.out::println);
    }
}
