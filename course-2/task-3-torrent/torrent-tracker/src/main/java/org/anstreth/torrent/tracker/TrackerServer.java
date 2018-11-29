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

    void start() throws IOException {
        log.debug(
            "Server started at address {}, port {}",
            serverSocket.getInetAddress(),
            serverSocket.getLocalPort()
        );

        registerShutdownHook();

        executor.execute(() -> {
            try (Socket accept = serverSocket.accept()) {
                try {
                    handleRequest(accept);
                } catch (IOException e) {
                    log.error("Error reading data from " + accept, e);
                }
            } catch (IOException e) {
                log.error("Error accepting connection, something is wrong with server socket. Stopping server");
            }
        });
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.debug("Server is shutting down...");
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
            T request = deserializer.deserialize(socket.getInputStream());

            M response;
            try {
                response = handler.apply(new RequestImpl<>(socket.getInetAddress(), request));
            } catch (RuntimeException e) {
                log.error("Runtime error while handling request " + request, e);
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
