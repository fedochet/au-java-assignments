package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.ListResponse;

import java.io.IOException;
import java.io.OutputStream;

public class ListResponseSerializer implements Serializer<ListResponse> {
    private final static FileInfoSerializer FILE_INFO_SERIALIZER = new FileInfoSerializer();

    @Override
    public void serialize(ListResponse value, OutputStream stream) throws IOException {
        SerializationUtils.serializeList(value.getFiles(), FILE_INFO_SERIALIZER, stream);
    }
}
