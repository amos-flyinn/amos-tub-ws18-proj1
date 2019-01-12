package com.amos.flyinn.nearbyservice;

import java.io.PipedInputStream;

public class VideoStreamSingleton {
    private static final VideoStreamSingleton ourInstance = new VideoStreamSingleton();
    public PipedInputStream os;

    public static VideoStreamSingleton getInstance() {
        return ourInstance;
    }

    private VideoStreamSingleton() {
    }
}
