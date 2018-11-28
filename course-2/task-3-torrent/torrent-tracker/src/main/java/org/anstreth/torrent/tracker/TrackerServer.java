package org.anstreth.torrent.tracker;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.request.serialization.ListRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.SourcesRequestDeserializer;
import org.anstreth.torrent.tracker.request.serialization.UploadRequestDeserializer;
import org.anstreth.torrent.tracker.response.serialization.ListResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.SourcesResponseSerializer;
import org.anstreth.torrent.tracker.response.serialization.UploadResponseSerializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;

public class TrackerServer {
    private final ServerSocket serverSocket;
    private final Map<Byte, RequestHandler> handlers = new HashMap<>();

    public TrackerServer(int port, TrackerController trackerController) throws IOException {
        serverSocket = new ServerSocket(port);

        // This is so ugly I will have to rework that
        registerMessageHandler(
            LIST_REQUEST,
            new ListRequestDeserializer(), trackerController::handle, new ListResponseSerializer()
        );
        registerMessageHandler(
            UPLOAD_REQUEST,
            new UploadRequestDeserializer(), trackerController::handle, new UploadResponseSerializer()
        );
        registerMessageHandler(
            SOURCES_REQUEST,
            new SourcesRequestDeserializer(), trackerController::handle, new SourcesResponseSerializer()
        );
        registerMessageHandler(
            UPDATE_REQUEST,
            new UploadRequestDeserializer(), trackerController::handle, new UploadResponseSerializer()
        );
    }

    public void run() {
        while (true) {
            try (Socket accept = serverSocket.accept()) {
                handleRequest(accept);
            } catch (IOException e) {
                // TODO log exception
            }
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        byte requestType = inputStream.readByte();
        handlers.get(requestType).handle(socket);
    }

    private <T, M> void registerMessageHandler(byte messageMarker,
                                               Deserializer<T> deserializer,
                                               Function<T, M> handler,
                                               Serializer<M> serializer) {
        if (handlers.containsKey(messageMarker)) {
            throw new IllegalArgumentException(
                "Handler for " + messageMarker + " marker is already present!"
            );
        }

        handlers.put(messageMarker, socket -> {
            T request = deserializer.deserialize(socket.getInputStream());

            M response;
            try {
                response = handler.apply(request);
            } catch (RuntimeException e) {
                // TODO log exception
                return;
            }

            serializer.serialize(response, socket.getOutputStream());
        });
    }

    @FunctionalInterface
    private interface RequestHandler {
        void handle(Socket socket) throws IOException;
    }
}
