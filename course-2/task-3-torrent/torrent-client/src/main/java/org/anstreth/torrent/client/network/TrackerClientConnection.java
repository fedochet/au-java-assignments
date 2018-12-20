package org.anstreth.torrent.client.network;

import org.anstreth.torrent.network.Connection;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static org.anstreth.torrent.tracker.request.TrackerRequestMarker.*;

class TrackerClientConnection extends Connection {
    TrackerClientConnection(InetAddress address, short port) throws IOException {
        super(address, port);
    }

    int addFile(String file, long size) throws IOException {
        dataOutputStream.writeByte(UPLOAD_REQUEST);
        dataOutputStream.writeUTF(file);
        dataOutputStream.writeLong(size);
        dataOutputStream.flush();

        return dataInputStream.readInt();
    }

    List<FileInfo> listFiles() throws IOException {
        dataOutputStream.writeByte(LIST_REQUEST);
        dataOutputStream.flush();

        return readList(inputStream -> {
            int id = inputStream.readInt();
            String fileName = inputStream.readUTF();
            long fileSize = inputStream.readLong();

            return new FileInfo(id, fileName, fileSize);
        });
    }

    List<SourceInfo> getSources(int fileId) throws IOException {
        dataOutputStream.writeByte(SOURCES_REQUEST);
        dataOutputStream.writeInt(fileId);
        dataOutputStream.flush();

        byte[] ipBuffer = new byte[4];

        return readList(inputStream -> {
            inputStream.readFully(ipBuffer);
            short port = inputStream.readShort();

            return new SourceInfo(InetAddress.getByAddress(ipBuffer), port);
        });
    }

    boolean updateSources(short port, List<Integer> fileIds) throws IOException {
        dataOutputStream.writeByte(UPDATE_REQUEST);
        dataOutputStream.writeShort(port);
        writeList(fileIds, DataOutputStream::writeInt);
        dataOutputStream.flush();

        return dataInputStream.readBoolean();
    }
}
