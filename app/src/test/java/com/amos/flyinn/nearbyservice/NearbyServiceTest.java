package com.amos.flyinn.nearbyservice;

import android.content.Intent;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ServiceController;

import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;

import static org.junit.Assert.*;

/**
 * Wrap Nearby service in a way to test its functions individually
 * Source: https://stackoverflow.com/a/33755494
 */
@RunWith(RobolectricTestRunner.class)
public class NearbyServiceTest {
    private NearbyService service;
    private ServiceController<TestService> controller;

    @Test
    public void testBasicIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
    }

    /**
     * Check that passing code with intent works correctly.
     */
    @Test
    public void testCodeIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction("");
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
        assertEquals("1234", service.getNearbyCode());
    }

    /**
     * Check that leaving action to be null will also work correctly.
     */
    @Test
    public void testNullActionIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
        assertEquals("1234", service.getNearbyCode());
    }

    @Test
    public void testStartIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction(NearbyService.ACTION_START);
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
    }

    @Test
    public void testStopIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction(NearbyService.ACTION_STOP);
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
    }

    @Test
    public void testSetNearbyCode() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction("");
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
        assertEquals("1234", service.getNearbyCode());
        service.setNearbyCode("5678");
        assertEquals("5678", service.getNearbyCode());
    }

    @Test
    public void testSetState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction("");
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        controller.startCommand(0, 0);
        service.setServiceState(NearbyState.STOPPED, "Test");
        assertEquals(NearbyState.STOPPED, service.getServiceState());
        service.setServiceState(NearbyState.CONNECTED, "Test");
        assertEquals(NearbyState.CONNECTED, service.getServiceState());
        service.setServiceState(NearbyState.CONNECTING, "Test");
        assertEquals(NearbyState.CONNECTING, service.getServiceState());
    }

    @Test
    public void testNotify() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), TestService.class);
        intent.putExtra("code", "1234");
        intent.setAction("");
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
        service.notify("test");
    }

    @Test
    public void testCreateNearbyIntent() {
        Intent intent = NearbyService.createNearbyIntent(NearbyService.ACTION_START, ApplicationProvider.getApplicationContext());
        assertEquals(NearbyService.ACTION_START, intent.getAction());
        controller = Robolectric.buildService(TestService.class, intent);
        service = controller.create().get();
    }

    @After
    public void tearDown() {
        controller.destroy();
    }

    public static class TestService extends NearbyService {
        @Override
        public void onStart(@Nullable Intent intent, int startId) {
            onHandleIntent(intent);
            stopSelf(startId);
        }
    }
}
