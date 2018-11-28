package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.response.UploadResponse;

import java.io.IOException;
import java.io.InputStream;

public class UploadResponseDeserializer implements Deserializer<UploadResponse> {
    @Override
    public UploadResponse deserialize(InputStream inputStream) throws IOException {
        return new UploadResponse(SerializationUtils.getDataInputStream(inputStream).readInt());
    }
}
