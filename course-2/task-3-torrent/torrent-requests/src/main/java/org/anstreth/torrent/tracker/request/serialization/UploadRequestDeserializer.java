package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.request.UploadRequest;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadRequestDeserializer implements Deserializer<UploadRequest> {
    @Override
    public UploadRequest deserialize(InputStream inputStream) throws IOException {
        DataInputStream dataOutputStream = SerializationUtils.getDataInputStream(inputStream);
        String name = dataOutputStream.readUTF();
        long size = dataOutputStream.readLong();
        return new UploadRequest(name, size);
    }
}
