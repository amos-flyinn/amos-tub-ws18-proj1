package com.amos.server.nearby;

import com.google.android.gms.nearby.connection.Payload;

public interface HandlePayload {
    void receive(Payload payload);
}
