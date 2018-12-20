package org.anstreth.torrent.tracker.request;

public class TrackerRequestMarker {
    private TrackerRequestMarker() {}
    public static final byte LIST_REQUEST = 1;
    public static final byte UPLOAD_REQUEST = 2;
    public static final byte SOURCES_REQUEST = 3;
    public static final byte UPDATE_REQUEST = 4;
}
