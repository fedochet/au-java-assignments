package org.anstreth.torrent.client.network;

import org.anstreth.torrent.client.request.ClientRequestMarkers;
import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.storage.FilePart;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.network.SingleThreadServer;
import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.ReflectiveDeserializerFabric;
import org.anstreth.torrent.serialization.ReflectiveSerializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PeerServer implements Closeable {
    private final static Logger log = LoggerFactory.getLogger(SingleThreadServer.class);

    private final ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final LocalFilesManager localFilesManager;

    public PeerServer(int port, LocalFilesManager localFilesManager) throws IOException {
        serverSocket = new ServerSocket(port);
        this.localFilesManager = localFilesManager;
        executor.submit(this::runServer);
    }

    private void runServer() {
        while (!Thread.interrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new PeerRequestHandler(clientSocket));
            } catch (IOException e) {
                if (serverSocket.isClosed()) {
                    log.info("Server socket has been closed");
                    break;
                } else {
                    log.error("Error accepting connection, something is wrong with server socket");
                }
            }
        }
    }

    private class PeerRequestHandler implements Runnable {
        private final Socket clientSocket;

        PeerRequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            ReflectiveSerializer reflectiveSerializer = new ReflectiveSerializer();
            Deserializer<StatRequest> statRequestDeserializer = ReflectiveDeserializerFabric.createForClass(StatRequest.class);
            Deserializer<GetRequest> getRequestDeserializer = ReflectiveDeserializerFabric.createForClass(GetRequest.class);

            try (Socket socket = clientSocket) {
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                byte requestType = inputStream.readByte();
                switch (requestType) {
                    case ClientRequestMarkers.STAT_MARKER: {
                        log.info("Handling stats request");
                        StatRequest request = statRequestDeserializer.deserialize(inputStream);
                        List<Integer> numbers = localFilesManager.getAvailableParts(request.getFileId())
                            .stream()
                            .map(FilePart::getNumber)
                            .collect(Collectors.toList());

                        reflectiveSerializer.serialize(numbers);
                        break;
                    }

                    case ClientRequestMarkers.GET_MARKER: {
                        log.info("Handling get request");
                        GetRequest deserialize = getRequestDeserializer.deserialize(inputStream);
                        FilePart part = new FilePart(deserialize.getFileId(), deserialize.getPartNumber());
                        try (InputStream filePart = localFilesManager.openForReading(part)) {
                            IOUtils.copy(filePart, socket.getOutputStream());
                        }

                        log.info("Get is handled");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            serverSocket.close();
        } finally {
            executor.shutdown();
        }
    }
}
