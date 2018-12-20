package org.anstreth.torrent.tracker.network;

import org.anstreth.torrent.network.Connection;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

class TrackerClientConnection extends Connection {
    TrackerClientConnection(Socket socket) throws IOException {
        super(socket);
    }

    byte readRequestType() throws IOException {
        return dataInputStream.readByte();
    }

    void writeListResponse(ListResponse handle) throws IOException {
        writeList(handle.getFiles(), this::serializeFileInfo);
        dataOutputStream.flush();
    }

    private void serializeFileInfo(DataOutputStream outputStream, FileInfo element) throws IOException {
        outputStream.writeInt(element.getId());
        outputStream.writeUTF(element.getName());
        outputStream.writeLong(element.getSize());
    }

    SourcesRequest readSourcesRequest() throws IOException {
        return new SourcesRequest(dataInputStream.readInt());
    }

    void writeSourcesResponse(SourcesResponse response) throws IOException {
        writeList(response.getAddresses(), this::serializeSourceInfo);
        dataOutputStream.flush();
    }

    private void serializeSourceInfo(DataOutputStream outputStream, SourceInfo sourceInfo) throws IOException {
        outputStream.write(sourceInfo.getAddress().getAddress());
        outputStream.writeShort(sourceInfo.getPort());
    }

    UpdateRequest readUpdateRequest() throws IOException {
        short port = dataInputStream.readShort();
        List<Integer> fileIds = readList(DataInputStream::readInt);

        return new UpdateRequest(port, fileIds);
    }

    void writeUpdateResponse(UpdateResponse response) throws IOException {
        dataOutputStream.writeBoolean(response.isSuccessful());
        dataOutputStream.flush();
    }

    UploadRequest readUploadRequest() throws IOException {
        String fileName = dataInputStream.readUTF();
        long fileSize = dataInputStream.readLong();

        return new UploadRequest(fileName, fileSize);
    }

    void writeUploadResponse(UploadResponse response) throws IOException {
        dataOutputStream.writeInt(response.getFileId());
        dataOutputStream.flush();
    }
}
