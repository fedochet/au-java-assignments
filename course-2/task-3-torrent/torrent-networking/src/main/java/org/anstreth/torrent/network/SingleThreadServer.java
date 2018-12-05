package org.anstreth.torrent.network;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.ReflectiveDeserializerFabric;
import org.anstreth.torrent.serialization.ReflectiveSerializer;
import org.anstreth.torrent.serialization.Serializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class SingleThreadServer implements NetworkServer {
    private final static Logger log = LoggerFactory.getLogger(SingleThreadServer.class);

    private final ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<Byte, RequestHandler> handlers = new ConcurrentHashMap<>();

    public SingleThreadServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        log.debug(
            "Server started at address {}, port {}",
            serverSocket.getInetAddress(),
            serverSocket.getLocalPort()
        );

        registerShutdownHook();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(getClientHandler(clientSocket));
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    log.debug("Server socket has been closed");
                    break;
                } else {
                    log.error("Error accepting connection, something is wrong with server socket");
                }
            }
        }
    }

    @NotNull
    private Runnable getClientHandler(Socket clientSocket) {
        return () -> {
            try (Socket client = clientSocket) {
                handleRequest(client);
            } catch (IOException e) {
                log.error("Error reading data from " + clientSocket, e);
            }
        };
    }

    private void handleRequest(Socket socket) throws IOException {
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        byte requestType = inputStream.readByte();
        handlers.get(requestType).handle(socket);
    }

    @Override
    public <T, M> void registerMessageHandler(byte routeMarker,
                                              Class<T> requestClass,
                                              Function<T, M> handler) {
        registerRequestHandler(routeMarker, requestClass, handler.compose(Request::getBody));
    }

    @Override
    public <T, M> void registerRequestHandler(byte routeMarker,
                                              Class<T> requestClass,
                                              Function<Request<T>, M> handler) {
        if (handlers.containsKey(routeMarker)) {
            throw new IllegalArgumentException(
                "Handler for " + routeMarker + " marker is already present!"
            );
        }

        Serializer<Object> serializer = new ReflectiveSerializer();
        Deserializer<T> deserializer = ReflectiveDeserializerFabric.createForClass(requestClass);

        handlers.put(routeMarker, socket -> {
            log.debug("Handling request at {} from {}", routeMarker, socket.getInetAddress());

            T request = deserializer.deserialize(socket.getInputStream());

            M response;
            try {
                response = handler.apply(new RequestImpl<>(socket.getInetAddress(), request));
            } catch (RuntimeException e) {
                String errorMessage = String.format(
                    "Runtime error while handling request %s at %d from %s",
                    request,
                    routeMarker,
                    socket.getInetAddress()
                );

                log.error(errorMessage, e);
                return;
            }

            serializer.serialize(response, socket.getOutputStream());

            log.debug(
                "Request at {} from {} is successfully handled!", routeMarker, socket.getInetAddress()
            );
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
