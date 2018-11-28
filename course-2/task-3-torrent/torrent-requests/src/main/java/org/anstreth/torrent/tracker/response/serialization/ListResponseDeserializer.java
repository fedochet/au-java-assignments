package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.ListResponse;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ListResponseDeserializer implements Deserializer<ListResponse> {
    private final static FileInfoDeserializer FILE_INFO_DESERIALIZER = new FileInfoDeserializer();

    @Override
    public ListResponse deserialize(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int size = dataInputStream.readInt();
        List<FileInfo> files = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            files.add(FILE_INFO_DESERIALIZER.deserialize(inputStream));
        }
        return new ListResponse(files);
    }
}
