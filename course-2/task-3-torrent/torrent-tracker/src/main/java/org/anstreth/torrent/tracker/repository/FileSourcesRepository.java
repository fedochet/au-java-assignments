package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.SourceInfo;

import java.net.InetAddress;
import java.util.List;

public interface FileSourcesRepository {
    List<SourceInfo> getFileSources(int fileId);
    void addFileSource(int fileId, InetAddress inetAddress, short port);
}
