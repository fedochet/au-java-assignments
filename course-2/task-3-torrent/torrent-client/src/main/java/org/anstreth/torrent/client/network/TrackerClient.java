package org.anstreth.torrent.client.network;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.SourceInfo;

import java.io.IOException;
import java.util.List;

public interface TrackerClient {
    int addFile(String file, long size) throws IOException;
    List<FileInfo> listFiles() throws IOException;
    List<SourceInfo> getSources(int fileId) throws IOException;
    boolean updateSources(short port, List<Integer> fileIds) throws IOException;
}
