package com.amos.server;

import android.app.IntentService;
import android.content.Intent;


/**
 *  - Service OnEndpointFound -> Findet einen Advertiser und speichert in eine Liste
 *      ==> Erhält id, können über Endpointinfo den Namen des Advertiser rausfinden
 *  - Service hat Liste von Advertisern ==> Also hat alle User mit ihren Code-Daten
 *      (Struktur, die Namen mit Code zuweist)
 *
 *  - Service erhält Eingabe
 *
 *  - Service checkt Eingabe, ob User in Liste, mit dieser Eingabe übereinstimmt
 *  - Service verbindet sich mit User aus dieser Liste
 *
 */
public class ServerConnAuthService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FOO = "com.amos.server.action.FOO";
    public static final String ACTION_BAZ = "com.amos.server.action.BAZ";

    // TODO: Rename parameters
    public static final String EXTRA_PARAM1 = "com.amos.server.extra.PARAM1";
    public static final String EXTRA_PARAM2 = "com.amos.server.extra.PARAM2";

    public ServerConnAuthService() {
        super("ServerConnAuthService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
