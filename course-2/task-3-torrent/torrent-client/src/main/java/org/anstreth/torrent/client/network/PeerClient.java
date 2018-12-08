package org.anstreth.torrent.client.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface PeerClient {
    List<Integer> getParts(int fileId) throws IOException;
    InputStream getPart(int fileId, int partNumber) throws IOException;
}
