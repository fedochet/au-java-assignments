package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.UpdateResponse;

import java.io.IOException;
import java.io.OutputStream;

public class UpdateResponseSerializer implements Serializer<UpdateResponse> {
    @Override
    public void serialize(UpdateResponse value, OutputStream stream) throws IOException {
        SerializationUtils.getDataOutputStream(stream).writeBoolean(value.isStatus());
    }
}
