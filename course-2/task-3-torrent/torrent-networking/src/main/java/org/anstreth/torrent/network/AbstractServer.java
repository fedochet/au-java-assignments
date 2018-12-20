package org.anstreth.torrent.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract public class AbstractServer implements Closeable {
    private final static Logger log = LoggerFactory.getLogger(AbstractServer.class);

    private final ServerSocket serverSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    protected AbstractServer(short port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        executor.submit(this::runServer);
    }

    protected abstract Runnable getRequestHandler(Socket clientSocket);

    private void runServer() {
        while (!Thread.interrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.submit(getRequestHandler(clientSocket));
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

    @Override
    public void close() throws IOException {
        try {
            serverSocket.close();
        } finally {
            executor.shutdown();
        }
    }
}
