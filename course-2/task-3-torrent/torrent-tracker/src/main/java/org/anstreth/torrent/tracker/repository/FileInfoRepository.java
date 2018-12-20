package org.anstreth.torrent.tracker.repository;

import org.anstreth.torrent.tracker.response.FileInfo;

import java.util.List;

public interface FileInfoRepository {
    /**
     * Saves information about file. Does not check if file with same name exist.
     *
     * @param fileName name of file
     * @param fileSize size of file
     * @return unique id of saved {@link FileInfo}
     */
    int addFile(String fileName, long fileSize);

    /**
     * @return fileInfos sorted by fileId.
     */

    List<FileInfo> getAllFiles();
}
