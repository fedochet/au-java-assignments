package org.anstreth.torrent.client.network;

import org.anstreth.torrent.client.request.ClientRequestMarkers;
import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.response.StatResponse;
import org.anstreth.torrent.client.storage.FilePart;
import org.anstreth.torrent.client.storage.FilePartsDetails;
import org.anstreth.torrent.client.storage.LocalFilesManager;
import org.anstreth.torrent.network.SingleThreadServer;
import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.ReflectiveDeserializerFabric;
import org.anstreth.torrent.serialization.ReflectiveSerializer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            try (PeerServerConnection connection = new PeerServerConnection(clientSocket)) {
                byte requestType = connection.readRequestType();
                switch (requestType) {
                    case ClientRequestMarkers.STAT_MARKER: {
                        log.info("Handling stats request");
                        StatRequest request = connection.readStatRequest();
                        FilePartsDetails fileDetails = localFilesManager.getFileDetails(request.getFileId());
                        List<Integer> parts = new ArrayList<>(fileDetails.getReadyPartsIndexes());

                        connection.writeStatResponse(new StatResponse(parts));
                        log.info("Stat request is handled");
                        break;
                    }

                    case ClientRequestMarkers.GET_MARKER: {
                        log.info("Handling get request");
                        GetRequest request = connection.readGetRequest();
                        FilePart part = new FilePart(request.getFileId(), request.getPartNumber());
                        try (InputStream filePart = localFilesManager.openForReading(part)) {
                            IOUtils.copy(filePart, connection.getOutputStream());
                        }

                        log.info("Get request is handled");
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("Error during handling request from " + clientSocket, e);
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
