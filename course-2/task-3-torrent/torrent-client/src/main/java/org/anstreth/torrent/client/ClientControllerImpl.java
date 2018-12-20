package org.anstreth.torrent.client;

import org.anstreth.torrent.client.request.GetRequest;
import org.anstreth.torrent.client.request.StatRequest;
import org.anstreth.torrent.client.response.StatResponse;
import org.anstreth.torrent.client.storage.FilePart;
import org.anstreth.torrent.client.storage.FilePartsDetails;
import org.anstreth.torrent.client.storage.LocalFilesManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

class ClientControllerImpl implements ClientController {
    private final LocalFilesManager localFilesManager;

    ClientControllerImpl(LocalFilesManager localFilesManager) {
        this.localFilesManager = localFilesManager;
    }

    @Override
    public InputStream handleGetRequest(GetRequest request) throws IOException {
        FilePart part = new FilePart(request.getFileId(), request.getPartNumber());
        return localFilesManager.openForReading(part);
    }

    @Override
    public StatResponse handleStatRequest(StatRequest request) throws IOException {
        FilePartsDetails fileDetails = localFilesManager.getFileDetails(request.getFileId());
        List<Integer> parts = new ArrayList<>(fileDetails.getReadyPartsIndexes());

        return new StatResponse(parts);
    }
}
