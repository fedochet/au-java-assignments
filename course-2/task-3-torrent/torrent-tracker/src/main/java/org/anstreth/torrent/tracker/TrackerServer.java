package org.anstreth.torrent.tracker;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.network.Request;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class TrackerServer {
    private final ServerSocket serverSocket;
    private final Map<Byte, RequestHandler> handlers = new HashMap<>();

    TrackerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    void run() throws IOException {
        while (true) {
            try (Socket accept = serverSocket.accept()) {
                try {
                    handleRequest(accept);
                } catch (IOException e) {
                    // TODO log exception
                }
            }
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        byte requestType = inputStream.readByte();
        handlers.get(requestType).handle(socket);
    }

    <T, M> void registerMessageHandler(byte messageMarker,
                                       Deserializer<T> deserializer,
                                       Function<T, M> handler,
                                       Serializer<M> serializer) {
        registerRequestHandler(
            messageMarker,
            deserializer,
            handler.compose(Request::getBody),
            serializer
        );
    }

    <T, M> void registerRequestHandler(byte messageMarker,
                                       Deserializer<T> deserializer,
                                       Function<Request<T>, M> handler,
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
                response = handler.apply(new RequestImpl<>(socket.getInetAddress(), request));
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

    private static final class RequestImpl<T> implements Request<T> {
        private final InetAddress address;
        private final T body;

        private RequestImpl(InetAddress address, T body) {
            this.address = address;
            this.body = body;
        }

        @Override
        public T getBody() {
            return body;
        }

        @Override
        public InetAddress getInetAddress() {
            return address;
        }
    }
}
