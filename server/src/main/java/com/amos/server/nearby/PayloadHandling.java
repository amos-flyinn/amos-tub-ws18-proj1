package com.amos.server.nearby;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

interface PayloadHandling {
    void addHandle(int type, HandlePayload handle);
    HandlePayload getHandle(int type);
    void handlePayload(Payload payload);
    void handlePayloadUpdate(PayloadTransferUpdate update);
}
