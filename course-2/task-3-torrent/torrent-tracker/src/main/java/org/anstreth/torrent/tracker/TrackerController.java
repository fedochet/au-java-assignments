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
import org.jetbrains.annotations.NotNull;

public interface TrackerController {
    @NotNull
    UploadResponse handle(@NotNull UploadRequest request);
    @NotNull
    ListResponse handle(@NotNull ListRequest request);
    @NotNull
    SourcesResponse handle(@NotNull SourcesRequest request);
    @NotNull
    UpdateResponse handle(@NotNull Request<UpdateRequest> request);
}
