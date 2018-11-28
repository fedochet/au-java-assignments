package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.UploadResponse;

import java.io.IOException;
import java.io.OutputStream;

public class UploadResponseSerializer implements Serializer<UploadResponse> {
    @Override
    public void serialize(UploadResponse value, OutputStream stream) throws IOException {
        SerializationUtils.getDataOutputStream(stream).writeInt(value.getFileId());
    }
}
