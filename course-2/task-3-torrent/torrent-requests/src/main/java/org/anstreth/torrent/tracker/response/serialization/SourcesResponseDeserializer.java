package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.response.SourcesResponse;

import java.io.IOException;
import java.io.InputStream;

public class SourcesResponseDeserializer implements Deserializer<SourcesResponse> {

    private final SourceInfoDeserializer SOURCE_INFO_DESERIALIZER = new SourceInfoDeserializer();

    @Override
    public SourcesResponse deserialize(InputStream inputStream) throws IOException {
        return new SourcesResponse(
            SerializationUtils.deserializeList(SOURCE_INFO_DESERIALIZER, inputStream)
        );
    }
}
