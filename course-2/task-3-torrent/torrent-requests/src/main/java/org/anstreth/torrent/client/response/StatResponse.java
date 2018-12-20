package org.anstreth.torrent.client.response;

import java.util.ArrayList;
import java.util.List;

public class StatResponse {
    private final List<Integer> partsNumbers;

    public StatResponse(List<Integer> partsNumbers) {
        this.partsNumbers = new ArrayList<>(partsNumbers);
    }

    public List<Integer> getPartsNumbers() {
        return partsNumbers;
    }
}
