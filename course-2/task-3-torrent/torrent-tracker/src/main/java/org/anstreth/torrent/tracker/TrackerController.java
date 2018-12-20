package org.anstreth.torrent.tracker;

import org.anstreth.torrent.network.Request;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.anstreth.torrent.tracker.response.SourcesResponse;
import org.anstreth.torrent.tracker.response.UpdateResponse;
import org.anstreth.torrent.tracker.response.UploadResponse;

public interface TrackerController {
    UploadResponse handle(UploadRequest request);
    ListResponse handleListRequest();
    SourcesResponse handleSourcesRequest(SourcesRequest request);
    UpdateResponse handleUpdateRequest(Request<UpdateRequest> request);
}
