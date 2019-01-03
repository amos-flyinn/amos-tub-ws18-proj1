package com.amos.server.nearby;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ServerConnectionTest {

    @Before public void setUpMockito() {
        MockitoAnnotations.initMocks(this);
    }

    @After public void tearDownMockito() {
        Mockito.validateMockitoUsage();
    }

    @Test
    public void getInstance() {
        // check whether we are getting the same object
        ServerConnection connection = ServerConnection.getInstance();
        ServerConnection connection2 = ServerConnection.getInstance();
        assertEquals(connection, connection2);
    }

    @Test
    public void init() {
        ServerConnection connection = ServerConnection.getInstance();
        connection.init(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void discover() {
    }

    @Test
    public void connectTo() {
    }

    @Test
    public void abort() {
    }

    @Test
    public void sendStream() {
    }
}