package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.List;

public interface FileSourcesRepository {
    @NotNull
    List<SourceInfo> getFileSources(int fileId);
    void addFileSource(int fileId, @NotNull InetAddress inetAddress, short port);
}
