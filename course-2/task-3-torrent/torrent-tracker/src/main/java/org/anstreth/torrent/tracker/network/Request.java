package org.anstreth.torrent.tracker.network;

import java.net.InetAddress;

/**
 * Interface to represent parsed network package.
 */
public interface Request<T> {
    T getBody();
    InetAddress getInetAddress();
}
