package org.anstreth.torrent.client.network;

import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.response.StatResponse;
import org.anstreth.torrent.network.Connection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

class PeerServerConnection extends Connection {
    PeerServerConnection(Socket socket) throws IOException {
        super(socket);
    }

    byte readRequestType() throws IOException {
        return dataInputStream.readByte();
    }

    StatRequest readStatRequest() throws IOException {
        return new StatRequest(dataInputStream.readInt());
    }

    void writeStatResponse(StatResponse response) throws IOException {
        writeList(response.getPartsNumbers(), DataOutputStream::writeInt);
    }

    GetRequest readGetRequest() throws IOException {
        int fileId = dataInputStream.readInt();
        int partNumber = dataInputStream.readInt();
        return new GetRequest(fileId, partNumber);
    }

    OutputStream getOutputStream() {
        return dataOutputStream;
    }
}
