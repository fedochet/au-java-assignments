package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.SourcesResponse;

import java.io.IOException;
import java.io.OutputStream;

public class SourcesResponseSerializer implements Serializer<SourcesResponse> {
    private static final SourceInfoSerializer SOURCE_INFO_SERIALIZER = new SourceInfoSerializer();

    @Override
    public void serialize(SourcesResponse value, OutputStream stream) throws IOException {
        SerializationUtils.serializeList(value.getAddresses(), SOURCE_INFO_SERIALIZER, stream);
    }
}
