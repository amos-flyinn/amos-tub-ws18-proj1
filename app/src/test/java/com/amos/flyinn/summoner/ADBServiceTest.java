package com.amos.flyinn.summoner;

import org.junit.Test;

import java.io.IOException;


public class ADBServiceTest {

    private ADBService service = new ADBService();

    /**
     * On handle intent never errors out
     */
    @Test
    public void onHandleIntentNull() {
        service.onHandleIntent(null);
    }

    @Test(expected = IOException.class)
    public void connectADB() throws IOException{
        service.connectNetworkADB();
    }

    @Test(expected = IOException.class)
    public void spawnApp() throws IOException{
        service.spawnApp(null, "Yolo");
    }

}