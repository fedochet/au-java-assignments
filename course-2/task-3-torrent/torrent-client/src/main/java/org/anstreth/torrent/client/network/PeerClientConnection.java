package org.anstreth.torrent.client.network;

import org.anstreth.torrent.network.Connection;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.List;

import static org.anstreth.torrent.client.request.ClientRequestMarkers.GET_MARKER;
import static org.anstreth.torrent.client.request.ClientRequestMarkers.STAT_MARKER;

class PeerClientConnection extends Connection {
    PeerClientConnection(InetAddress address, short port) throws IOException {
        super(address, port);
    }

    List<Integer> getParts(int fileId) throws IOException {
        dataOutputStream.writeByte(STAT_MARKER);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();

        return readList(DataInputStream::readInt);
    }

    InputStream getPart(int fileId, int partNumber) throws IOException {
        dataOutputStream.writeByte(GET_MARKER);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.writeInt(partNumber);
        dataOutputStream.flush();

        return dataInputStream;
    }
}
