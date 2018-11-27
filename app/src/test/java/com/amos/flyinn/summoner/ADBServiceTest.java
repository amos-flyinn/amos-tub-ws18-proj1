package com.amos.flyinn.summoner;

import org.junit.Test;

import static org.junit.Assert.*;

public class ADBServiceTest {

    private ADBService service = new ADBService();

    @Test
    public void onHandleIntentNull() {
        service.onHandleIntent(null);
    }
}