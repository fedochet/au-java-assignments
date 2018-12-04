package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFileSourcesRepository implements FileSourcesRepository {
    private final Map<Integer, List<SourceInfo>> sources = new ConcurrentHashMap<>();

    @Override
    public @NotNull List<SourceInfo> getFileSources(int fileId) {
        List<SourceInfo> list = sources.getOrDefault(fileId, Collections.emptyList());
        synchronized (list) {
            return new ArrayList<>(list);
        }
    }

    @Override
    public void addFileSource(int fileId, @NotNull InetAddress inetAddress, short port) {
        sources.computeIfAbsent(fileId, id -> Collections.synchronizedList(new ArrayList<>()))
            .add(new SourceInfo(inetAddress, port));
    }
}
