package org.anstreth.torrent.tracker.request.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.request.UpdateRequest;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class UpdateRequestDeserializer implements Deserializer<UpdateRequest> {
    @Override
    public UpdateRequest deserialize(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = SerializationUtils.getDataInputStream(inputStream);
        short port = dataInputStream.readShort();
        List<Integer> fileIds = SerializationUtils.deserializeList(
            stream -> SerializationUtils.getDataInputStream(inputStream).readInt(),
            dataInputStream
        );

        return new UpdateRequest(port, fileIds);
    }
}
