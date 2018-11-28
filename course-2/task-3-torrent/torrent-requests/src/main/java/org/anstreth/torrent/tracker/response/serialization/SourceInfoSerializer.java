package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SourceInfoSerializer implements Serializer<SourceInfo> {
    @Override
    public void serialize(SourceInfo value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = SerializationUtils.getDataOutputStream(stream);
        dataOutputStream.write(value.getAddress().getAddress());
        dataOutputStream.writeShort(value.getPort());
    }
}
