package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.tracker.request.ListRequest;

import java.io.IOException;
import java.io.InputStream;

public class ListRequestDeserializer implements Deserializer<ListRequest> {
    @Override
    public ListRequest deserialize(InputStream inputStream) throws IOException {
        return new ListRequest();
    }
}
