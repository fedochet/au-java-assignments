package org.anstreth.torrent.tracker.requests.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.requests.ListRequest;
import org.anstreth.torrent.tracker.requests.SourcesRequest;

import java.io.IOException;
import java.io.InputStream;

public class SourcesRequestDeserializer implements Deserializer<SourcesRequest> {
    @Override
    public SourcesRequest deserialize(InputStream inputStream) throws IOException {
        return new SourcesRequest(SerializationUtils.getDataInputStream(inputStream).readInt());
    }
}
