package com.amos.server.mediadecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class MediaHeader {
    public int type = 0;
    public int size = 0;
    public static final int length = 8;

    MediaHeader(int type, int size) {
        this.size = size;
        this.type = type;
    }
}

class MediaData {
    public MediaHeader header;
    public ByteBuffer data;

    MediaData(MediaHeader header, ByteBuffer data) {
        this.header = header;
        this.data = data;
    };
}

public class MediaInputStream {

    InputStream stream;

    private final String TAG = "MediaInputStream";

    private final byte[]nalgate = {2, 2, 2,};


    public MediaInputStream(InputStream is) {
        stream = is;
    }

    private MediaHeader readHeader() throws IOException {
        byte[] buffer = new byte[MediaHeader.length];
        int read = 0;
        while (read < MediaHeader.length) {
            int readByte = stream.read();
            if (readByte < 0) continue;
            buffer[read++] = (byte) readByte;
        }
        ByteBuffer bb = ByteBuffer.wrap(buffer);
        bb = bb.order(ByteOrder.BIG_ENDIAN);
        int type = bb.getInt();
        int size = bb.getInt();
        return new MediaHeader(type, size);
    }

    private boolean isNal(byte[] nalgate) {
        return nalgate[2] == 0 && nalgate[1] == 0 && nalgate[0] == 1;
    }

    public ByteBuffer readMedia(ByteBuffer buffer) throws IOException {
        // MediaHeader header = readHeader();
        // Log.d(TAG, String.format("Reading %d %d", header.type, header.size));
        // byte[] buffer = new byte[header.size];
        buffer.position(0);
        boolean nalfound = false;
        while(true) {
            int readByte = stream.read();
            if (readByte < 0) continue;
            nalgate[2] = nalgate[1];
            nalgate[1] = nalgate[0];
            nalgate[0] = (byte)readByte;
            if (isNal(nalgate)) {
                if (nalfound) {
                    break;
                } else {
                    nalfound = true;
                }
            }
            if (nalfound) {
                buffer.put(nalgate[2]);
            }
        }
        return buffer;
    }
}
