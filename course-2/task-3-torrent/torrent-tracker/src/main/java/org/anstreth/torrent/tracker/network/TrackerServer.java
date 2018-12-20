package org.anstreth.torrent.tracker.network;

import org.anstreth.torrent.network.AbstractServer;
import org.anstreth.torrent.network.Request;
import org.anstreth.torrent.tracker.TrackerController;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.TrackerRequestMarker;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class TrackerServer extends AbstractServer implements Closeable {
    private final static Logger log = LoggerFactory.getLogger(TrackerServer.class);

    private final TrackerController controller;

    public TrackerServer(short port, TrackerController controller) throws IOException {
        super(port);
        this.controller = controller;
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
            try (TrackerClientConnection connection = new TrackerClientConnection(clientSocket)) {
                byte requestType = connection.readRequestType();
                switch (requestType) {
                    case TrackerRequestMarker.LIST_REQUEST: {
                        connection.writeListResponse(controller.handleListRequest());
                        break;
                    }

                    case TrackerRequestMarker.SOURCES_REQUEST: {
                        SourcesRequest request = connection.readSourcesRequest();
                        connection.writeSourcesResponse(controller.handleSourcesRequest(request));
                        break;
                    }

                    case TrackerRequestMarker.UPDATE_REQUEST: {
                        UpdateRequest updateRequest = connection.readUpdateRequest();
                        connection.writeUpdateResponse(controller.handleUpdateRequest(
                            new RequestImpl<>(connection.getAddress(), updateRequest)
                        ));
                        break;
                    }

                    case TrackerRequestMarker.UPLOAD_REQUEST: {
                        UploadRequest request = connection.readUploadRequest();
                        connection.writeUploadResponse(controller.handle(request));
                        break;
                    }

                    default: {
                        log.warn("Unknown request type {}, ignoring that", requestType);
                        break;
                    }
                }
            } catch (IOException e) {
                log.error("Error during handling request from " + clientSocket, e);
            }
        }
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