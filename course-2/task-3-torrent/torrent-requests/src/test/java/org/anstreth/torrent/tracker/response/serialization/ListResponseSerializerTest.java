package org.anstreth.torrent.tracker.response.serialization;

import org.anstreth.torrent.tracker.response.FileInfo;
import org.anstreth.torrent.tracker.response.ListResponse;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ListResponseSerializerTest {

    private final ListResponseDeserializer listResponseDeserializer = new ListResponseDeserializer();
    private ListResponseSerializer listResponseSerializer = new ListResponseSerializer();

    @Test
    public void deserialized_list_is_equal_to_original() throws IOException {
        ListResponse listResponse = new ListResponse(Arrays.asList(
                new FileInfo(1, "1", 10),
                new FileInfo(2, "2", 20))
        );

        byte[] serialize = listResponseSerializer.serialize(listResponse);
        ListResponse deserializedResponse = listResponseDeserializer.deserialize(serialize);

        assertEquals(listResponse.getFiles(), deserializedResponse.getFiles());
    }
}