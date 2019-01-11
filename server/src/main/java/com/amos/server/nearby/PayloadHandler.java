package com.amos.server.nearby;

import android.util.SparseArray;

import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

/**
 * Handle incoming payloads and transfers them to an object implementing the handle payload
 * interface
 */
public class PayloadHandler {
    private static final PayloadHandler ourInstance = new PayloadHandler();

    private SparseArray<HandlePayload> handleMap = new SparseArray<>();

    public static PayloadHandler getInstance() {
        return ourInstance;
    }

    private PayloadHandler() {
    }

    public void addHandle(Payload.Type type, HandlePayload handle) {
        handleMap.put(type.hashCode(), handle);
    }

    public void handlePayloadReceived(Payload payload) {
        
        HandlePayload handle = handleMap.get(payload.getType());
        if (handle != null) {
            handle.receive(payload);
        }
        // Payload.Stream stream = payload.asStream();
        // if (stream == null) {
        //     Log.wtf("AAAAAAAAAAAAAAAAAAA", "Payload is null!");
        //     return;
        // }
        // InputStream istream = stream.asInputStream();
    }

    public void handlePayloadUpdate(PayloadTransferUpdate update) {
    }
}
