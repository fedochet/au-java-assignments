package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.serialization.Deserializer;
import org.anstreth.torrent.serialization.SerializationUtils;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

public class SourceInfoDeserializer implements Deserializer<SourceInfo> {
    @Override
    public SourceInfo deserialize(InputStream inputStream) throws IOException {
        DataInputStream dataInputStream = SerializationUtils.getDataInputStream(inputStream);

        byte[] ipAddress = new byte[4];
        dataInputStream.readFully(ipAddress);
        short port = dataInputStream.readShort();

        return new SourceInfo(InetAddress.getByAddress(ipAddress), port);
    }
}
