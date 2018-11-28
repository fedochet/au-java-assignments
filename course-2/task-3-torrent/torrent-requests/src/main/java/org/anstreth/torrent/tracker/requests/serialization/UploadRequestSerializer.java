package org.anstreth.torrent.tracker.requests.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.requests.UploadRequest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UploadRequestSerializer implements Serializer<UploadRequest> {
    @Override
    public void serialize(UploadRequest value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = SerializationUtils.getDataOutputStream(stream);
        dataOutputStream.writeUTF(value.getName());
        dataOutputStream.writeLong(value.getSize());
    }
}
