package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.SourceInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryFileSourcesRepository implements FileSourcesRepository {
    private final Duration timeLimit;
    private final Map<Integer, Set<TimedSourceInfo>> sourcesByFileId = new ConcurrentHashMap<>();

    public InMemoryFileSourcesRepository(Duration timeLimit, Duration updatePeriod) {
        this.timeLimit = timeLimit;
        new Timer(true /*isDaemon*/)
            .schedule(new OutdatedSourcesRemovalTask(), 0, updatePeriod.toMillis());
    }

    @Override
    public @NotNull List<SourceInfo> getFileSources(int fileId) {
        Set<TimedSourceInfo> sources = this.sourcesByFileId.getOrDefault(fileId, Collections.emptySet());
        synchronized (sources) {
            return sources.stream()
                .map(TimedSourceInfo::getSourceInfo)
                .collect(Collectors.toList());
        }
    }

    @Override
    public void addFileSource(int fileId, @NotNull InetAddress inetAddress, short port) {
        sourcesByFileId
            .computeIfAbsent(fileId, id -> Collections.synchronizedSet(new HashSet<>()))
            .add(timed(new SourceInfo(inetAddress, port)));
    }

    private TimedSourceInfo timed(SourceInfo sourceInfo) {
        return new TimedSourceInfo(sourceInfo, Instant.now().plus(timeLimit));
    }

    private static class TimedSourceInfo {
        private final SourceInfo sourceInfo;
        private final Instant expirationTime;

        private TimedSourceInfo(SourceInfo sourceInfo, Instant expirationTime) {
            this.sourceInfo = sourceInfo;
            this.expirationTime = expirationTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimedSourceInfo that = (TimedSourceInfo) o;
            return Objects.equals(sourceInfo, that.sourceInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceInfo);
        }

        private SourceInfo getSourceInfo() {
            return sourceInfo;
        }

        private boolean isOutdated(Instant currentTime) {
            return expirationTime.isBefore(currentTime);
        }
    }

    private class OutdatedSourcesRemovalTask extends TimerTask {
        @Override
        public void run() {
            Instant now = Instant.now();
            for (Set<TimedSourceInfo> sources : sourcesByFileId.values()) {
                synchronized (sources) {
                    sources.removeIf(info -> info.isOutdated(now));
                }
            }
        }
    }
}
