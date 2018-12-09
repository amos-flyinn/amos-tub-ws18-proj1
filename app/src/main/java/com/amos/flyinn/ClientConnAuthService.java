package com.amos.flyinn;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

/**
 * Client App advertised in der Umgebung.
 * Akzeptiert jede Verbindung.
 * Wenn Verbindung getrennt wird, soll App geschlossen werden (ggfalls recreate, aber erst später)
 *
 * NOTE: Hier sollen die Elemente vom Server_Nearby benutzt werden
 *
 * Class soll tun: Alle Verbindungssachen mit Nearby
 *  - Übergebener Name = Name des Clients
 *  - Extrahiere den Code
 *  - Client advertised sich (startAdvertising)
 *  - Nehmen jede Verbindung an, die uns erreicht (und vor allem nur Eine)
 *  - Verbindet sich mit Bike
 *
 */
public class ClientConnAuthService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public ClientConnAuthService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
