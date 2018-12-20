package org.anstreth.torrent.client.network;

import org.anstreth.torrent.client.ClientController;
import org.anstreth.torrent.client.request.ClientRequestMarkers;
import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.network.AbstractServer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class PeerServer extends AbstractServer {
    private final static Logger log = LoggerFactory.getLogger(PeerServer.class);

    private final ClientController controller;

    public PeerServer(short clientPort, ClientController clientController) throws IOException {
        super(clientPort);
        this.controller = clientController;
    }

    @Override
    protected Runnable getRequestHandler(Socket clientSocket) {
        return new PeerRequestHandler(clientSocket);
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
                        StatRequest request = connection.readStatRequest();
                        connection.writeStatResponse(controller.handleStatRequest(request));

                        break;
                    }

                    case ClientRequestMarkers.GET_MARKER: {
                        GetRequest request = connection.readGetRequest();
                        try (InputStream filePart = controller.handleGetRequest(request)) {
                            IOUtils.copy(filePart, connection.getOutputStream());
                        }

                        break;
                    }
                }
            } catch (IOException e) {
                log.error("Error during handling request from " + clientSocket, e);
            }
        }
    }
}
