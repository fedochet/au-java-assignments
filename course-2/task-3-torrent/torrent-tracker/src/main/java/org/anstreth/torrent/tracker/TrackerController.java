package org.anstreth.torrent.tracker;

import org.anstreth.torrent.tracker.network.Request;
import org.anstreth.torrent.tracker.request.ListRequest;
import org.anstreth.torrent.tracker.request.SourcesRequest;
import org.anstreth.torrent.tracker.request.UpdateRequest;
import org.anstreth.torrent.tracker.request.UploadRequest;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.anstreth.torrent.tracker.response.SourcesResponse;
import org.anstreth.torrent.tracker.response.UpdateResponse;
import org.anstreth.torrent.tracker.response.UploadResponse;

public interface TrackerController {
    UploadResponse handle(UploadRequest request);
    ListResponse handle(ListRequest request);
    SourcesResponse handle(SourcesRequest request);
    UpdateResponse handle(Request<UpdateRequest> request);
}
