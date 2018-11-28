package org.anstreth.torrent.tracker.response;

import java.net.InetAddress;

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
}
