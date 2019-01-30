package com.amos.flyinn.summoner;

import com.tananaev.adblib.AdbCrypto;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


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
    public void spawnApp() throws Exception{
        service.spawnApp(null, "Yolo");
    }

    @Test
    public void setupADBCrypto() {
        try {
            AdbCrypto k1=service.setupADBCrypto();
            AdbCrypto k2=service.setupADBCrypto();
            assertEquals(k1.getAdbPublicKeyPayload(), k2.getAdbPublicKeyPayload());
        } catch (Exception err) {
        }
    }


}
