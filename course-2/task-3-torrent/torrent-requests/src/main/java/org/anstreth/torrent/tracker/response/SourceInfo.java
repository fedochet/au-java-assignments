package org.anstreth.torrent.tracker.response;

import java.net.InetAddress;
import java.util.Objects;

public class SourceInfo {
    private final InetAddress address;
    private final short port;

    public SourceInfo(InetAddress address, short port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public short getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceInfo that = (SourceInfo) o;
        return port == that.port &&
            Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port);
    }

    @Override
    public String toString() {
        return "SourceInfo{" +
            "address=" + address +
            ", port=" + port +
            '}';
    }
}
