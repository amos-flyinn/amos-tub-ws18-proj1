package com.amos.flyinn.summoner;

import java.io.InputStream;

public class ConnectionSigleton {
    private static final ConnectionSigleton ourInstance = new ConnectionSigleton();
    public InputStream inputStream;

    public static ConnectionSigleton getInstance() {
        return ourInstance;
    }

    private ConnectionSigleton() {
    }
}
