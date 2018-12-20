package org.anstreth.torrent.network;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public abstract class Connection implements Closeable {
    private final Socket socket;
    protected final DataInputStream dataInputStream;
    protected final DataOutputStream dataOutputStream;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }

    public Connection(InetAddress address, short port) throws IOException {
        this(new Socket(address, port));
    }

    @Override
    public void close() throws IOException {
        try {
            dataOutputStream.flush();
        } finally {
            socket.close();
        }
    }

    protected <T> List<T> readList(Deserializer<T> deserializer) throws IOException {
        int count = dataInputStream.readInt();
        List<T> result = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            result.add(deserializer.read(dataInputStream));
        }

        return result;
    }

    protected <T> void writeList(List<T> list, Serializer<T> deserializer) throws IOException {
        dataOutputStream.writeInt(list.size());
        for (T element : list) {
            deserializer.write(dataOutputStream, element);
        }
    }

    protected interface Deserializer<T> {
        T read(DataInputStream inputStream) throws IOException;
    }

    protected interface Serializer<T> {
        void write(DataOutputStream outputStream, T element) throws IOException;
    }
}
