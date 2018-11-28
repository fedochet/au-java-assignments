package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.request.SourcesRequest;

import java.io.IOException;
import java.io.OutputStream;

public class SourcesRequestSerializer implements Serializer<SourcesRequest> {
    @Override
    public void serialize(SourcesRequest value, OutputStream stream) throws IOException {
        SerializationUtils.getDataOutputStream(stream).writeInt(value.getFileId());
    }
}
