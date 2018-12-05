package org.anstreth.torrent.tracker;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.network.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

class TrackerServer {
    private final static Logger log = LoggerFactory.getLogger(TrackerServer.class);

    private final ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Byte, RequestHandler> handlers = new HashMap<>();

    TrackerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    void start() {
        log.debug(
            "Server started at address {}, port {}",
            serverSocket.getInetAddress(),
            serverSocket.getLocalPort()
        );

        registerShutdownHook();

        executor.execute(() -> {
            while (!serverSocket.isClosed()) {
                try (Socket accept = serverSocket.accept()) {
                    try {
                        handleRequest(accept);
                    } catch (IOException e) {
                        log.error("Error reading data from " + accept, e);
                    }
                } catch (IOException e) {
                    if (serverSocket.isClosed()) {
                        log.debug("Server socket has been closed");
                    } else {
                        log.error("Error accepting connection, something is wrong with server socket");
                    }
                }
            }
        });
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.debug("Server is shutting down...");
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.error("Error shutting down server socket", e);
            }
            executor.shutdown();
            log.debug("Server is shut down.");
        }, "Server shutdown hook"));
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
            log.debug("Handling request at {} from {}", messageMarker, socket.getInetAddress());

            T request = deserializer.deserialize(socket.getInputStream());

            M response;
            try {
                response = handler.apply(new RequestImpl<>(socket.getInetAddress(), request));
            } catch (RuntimeException e) {
                String errorMessage = String.format(
                    "Runtime error while handling request %s at %d from %s",
                    request,
                    messageMarker,
                    socket.getInetAddress()
                );

                log.error(errorMessage, e);
                return;
            }

            serializer.serialize(response, socket.getOutputStream());

            log.debug(
                "Request at {} from {} is successfully handled!", messageMarker, socket.getInetAddress()
            );
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
