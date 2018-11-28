package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.request.ListRequest;

import java.io.IOException;
import java.io.OutputStream;

public class ListRequestSerializer implements Serializer<ListRequest> {
    @Override
    public void serialize(ListRequest value, OutputStream stream) throws IOException {
    }
}
