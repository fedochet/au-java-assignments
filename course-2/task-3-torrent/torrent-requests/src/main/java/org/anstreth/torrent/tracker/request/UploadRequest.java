package org.anstreth.torrent.tracker.request;

public class UploadRequest {
    private final String fileName;
    private final long fileSize;

    public UploadRequest(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}
