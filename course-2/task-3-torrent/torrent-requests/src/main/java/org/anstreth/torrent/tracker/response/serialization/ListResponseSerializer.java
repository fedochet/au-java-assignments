package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Serializer;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.ListResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ListResponseSerializer implements Serializer<ListResponse> {
    private final static FileInfoSerializer FILE_INFO_SERIALIZER = new FileInfoSerializer();

    @Override
    public void serialize(ListResponse value, OutputStream stream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(stream);
        dataOutputStream.writeInt(value.getFiles().size());
        for (FileInfo file : value.getFiles()) {
            FILE_INFO_SERIALIZER.serialize(file, stream);
        }
    }
}
