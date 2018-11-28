package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.request.serialization.ListRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UploadRequestDeserializer;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.anstreth.torrent.tracker.response.SourcesResponse;
import org.anstreth.torrent.tracker.response.UploadResponse;
import org.anstreth.torrent.tracker.response.serialization.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TrackerServer {
    private static final byte LIST_REQUEST = 1;
    private static final byte UPLOAD_REQUEST = 2;
    private static final byte SOURCES_REQUEST = 3;
    private static final byte UPDATE_REQUEST = 4;

    private static final ListRequestDeserializer LIST_REQUEST_DESERIALIZER = new ListRequestDeserializer();
    private static final UploadRequestDeserializer UPLOAD_REQUEST_DESERIALIZER = new UploadRequestDeserializer();
    private static final SourcesRequestDeserializer SOURCES_REQUEST_DESERIALIZER = new SourcesRequestDeserializer();
    private static final UploadRequestDeserializer UPDATE_REQUEST_DESERIALIZER = new UploadRequestDeserializer();

    private static final ListResponseSerializer LIST_RESPONSE_SERIALIZER = new ListResponseSerializer();
    private static final UploadResponseSerializer UPLOAD_RESPONSE_SERIALIZER = new UploadResponseSerializer();
    private static final SourcesResponseSerializer SOURCES_RESPONSE_SERIALIZER = new SourcesResponseSerializer();
    private static final UploadResponseSerializer UPDATE_RESPONSE_SERIALIZER = new UploadResponseSerializer();

    private final ServerSocket serverSocket;

    public TrackerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void run(TrackerController trackerController) throws IOException {
        while (true) {
            try (Socket accept = serverSocket.accept()) {
                handleRequest(accept, trackerController);
            }
        }
    }

    private void handleRequest(Socket accept, TrackerController trackerController) throws IOException {
        DataInputStream inputStream = new DataInputStream(accept.getInputStream());
        OutputStream outputStream = accept.getOutputStream();

        byte requestType = inputStream.readByte();

        switch (requestType) {
            case LIST_REQUEST: {
                ListResponse response = trackerController.handle(LIST_REQUEST_DESERIALIZER.deserialize(inputStream));
                LIST_RESPONSE_SERIALIZER.serialize(response, outputStream);
                break;
            }

            case UPLOAD_REQUEST: {
                UploadResponse response = trackerController.handle(UPLOAD_REQUEST_DESERIALIZER.deserialize(inputStream));
                UPLOAD_RESPONSE_SERIALIZER.serialize(response, outputStream);
                break;
            }

            case SOURCES_REQUEST: {
                SourcesResponse response = trackerController.handle(SOURCES_REQUEST_DESERIALIZER.deserialize(inputStream));
                SOURCES_RESPONSE_SERIALIZER.serialize(response, outputStream);
                break;
            }

            case UPDATE_REQUEST: {
                UploadResponse response = trackerController.handle(UPDATE_REQUEST_DESERIALIZER.deserialize(inputStream));
                UPDATE_RESPONSE_SERIALIZER.serialize(response, outputStream);
                break;
            }
        }
    }
}
