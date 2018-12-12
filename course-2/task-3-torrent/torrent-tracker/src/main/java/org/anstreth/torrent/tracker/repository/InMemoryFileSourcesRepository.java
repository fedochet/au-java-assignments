package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// FIXME: 05.12.18 remove keys from sources after some time
// FIXME: 06.12.18 disallow adding many same resources for the same file
public class InMemoryFileSourcesRepository implements FileSourcesRepository {
    private final Map<Integer, Set<SourceInfo>> sources = new ConcurrentHashMap<>();

    @Override
    public @NotNull List<SourceInfo> getFileSources(int fileId) {
        Set<SourceInfo> sources = this.sources.getOrDefault(fileId, Collections.emptySet());
        synchronized (sources) {
            return new ArrayList<>(sources);
        }
    }

    @Override
    public void addFileSource(int fileId, @NotNull InetAddress inetAddress, short port) {
        sources.computeIfAbsent(fileId, id -> Collections.synchronizedSet(new HashSet<>()))
            .add(new SourceInfo(inetAddress, port));
    }
}
