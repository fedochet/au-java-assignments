package org.anstreth.torrent.client;

import org.anstreth.torrent.client.network.NetworkClient;
import org.anstreth.torrent.client.network.NetworkClientImpl;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.TrackerRequestMarker;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.request.serialization.ListRequestSerializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestSerializer;
import org.anstreth.torrent.tracker.request.serialization.UploadRequestSerializer;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.anstreth.torrent.tracker.response.SourcesResponse;
import org.anstreth.torrent.tracker.response.UploadResponse;
import org.anstreth.torrent.tracker.response.serialization.ListResponseDeserializer;
import org.anstreth.torrent.tracker.response.serialization.UploadResponseDeserializer;

import java.io.IOException;
import java.util.Scanner;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        NetworkClient client = new NetworkClientImpl("localhost", 8081);

        client.addSerializer(UploadRequest.class, new UploadRequestSerializer());
        client.addDeserializer(UploadResponse.class, new UploadResponseDeserializer());

        client.addSerializer(SourcesRequest.class, new SourcesRequestSerializer());
        client.addDeserializer(SourcesRequest.class, new SourcesRequestDeserializer());

        client.addSerializer(ListRequest.class, new ListRequestSerializer());
        client.addDeserializer(ListResponse.class, new ListResponseDeserializer());

        Scanner scanner = new Scanner(System.in);
        while (true) {
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
    }

    private static void handleAddCommand(NetworkClient client, String fileName, long size) throws IOException {
        UploadResponse deserialize = client.makeRequest(
            TrackerRequestMarker.UPLOAD_REQUEST,
            new UploadRequest(fileName, size),
            UploadResponse.class
        );
        System.out.printf("File with id %s is added\n", deserialize.getFileId());
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
