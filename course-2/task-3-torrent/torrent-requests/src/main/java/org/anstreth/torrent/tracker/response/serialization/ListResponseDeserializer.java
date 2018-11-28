package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.response.ListResponse;

import java.io.IOException;
import java.io.InputStream;

public class ListResponseDeserializer implements Deserializer<ListResponse> {
    private final static FileInfoDeserializer FILE_INFO_DESERIALIZER = new FileInfoDeserializer();

    @Override
    public ListResponse deserialize(InputStream inputStream) throws IOException {
        return new ListResponse(
                SerializationUtils.deserializeList(FILE_INFO_DESERIALIZER, inputStream)
        );
    }
}
