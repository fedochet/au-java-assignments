package org.anstreth.torrent.tracker.requests.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.requests.ListRequest;

import java.io.IOException;
import java.io.OutputStream;

public class ListRequestSerializer implements Serializer<ListRequest> {
    @Override
    public void serialize(ListRequest value, OutputStream stream) throws IOException {}
}
