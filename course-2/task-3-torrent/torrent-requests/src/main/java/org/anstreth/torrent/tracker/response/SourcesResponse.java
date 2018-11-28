package org.anstreth.torrent.tracker.response;

import java.util.ArrayList;
import java.util.List;

public class SourcesResponse {
    private final List<SourceInfo> addresses;

    public SourcesResponse(List<SourceInfo> addresses) {
        this.addresses = new ArrayList<>(addresses);
    }

    public List<SourceInfo> getAddresses() {
        return addresses;
    }
}
