package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.request.UploadRequest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UploadRequestSerializer implements Serializer<UploadRequest> {
    @Override
    public void serialize(UploadRequest value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = SerializationUtils.getDataOutputStream(stream);
        dataOutputStream.writeUTF(value.getFileName());
        dataOutputStream.writeLong(value.getFileSize());
    }
}
