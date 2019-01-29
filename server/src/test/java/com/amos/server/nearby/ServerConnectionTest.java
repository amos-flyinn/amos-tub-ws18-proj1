package com.amos.server.nearby;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest(ConnectionsClient.class)
public class ServerConnectionTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ConnectionsClient mockClient;

    @Test
    public void getInstance() {
        // check whether we are getting the same object
        ServerConnection connection = ServerConnection.getInstance();
        ServerConnection connection2 = ServerConnection.getInstance();
        assertEquals(connection, connection2);
    }

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() {
        Mockito.when(mockClient.startDiscovery(Mockito.anyString(), Mockito.any(EndpointDiscoveryCallback.class), Mockito.any(DiscoveryOptions.class))).thenAnswer(invocation -> {
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("test", new DiscoveredEndpointInfo("serviceId", "endName"));
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("test2", new DiscoveredEndpointInfo("serviceId2", "endName2"));
            // Lose endpoint again
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("testL", new DiscoveredEndpointInfo("serviceIdL", "endNameL"));
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointLost("testL");
            // duplicated
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("testD", new DiscoveredEndpointInfo("serviceIdD1", "endNameD1"));
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("testD", new DiscoveredEndpointInfo("serviceIdD1", "endNameD1"));
            ((EndpointDiscoveryCallback) invocation.getArguments()[1]).onEndpointFound("testD", new DiscoveredEndpointInfo("serviceIdD1", "endNameD1"));

            return new Task<Object>() {
                @Override
                public boolean isComplete() {
                    return false;
                }

                @Override
                public boolean isSuccessful() {
                    return false;
                }

                @Override
                public boolean isCanceled() {
                    return false;
                }

                @Nullable
                @Override
                public Object getResult() {
                    return null;
                }

                @Nullable
                @Override
                public <X extends Throwable> Object getResult(@NonNull Class<X> aClass) {
                    return null;
                }

                @Nullable
                @Override
                public Exception getException() {
                    return null;
                }

                @NonNull
                @Override
                public Task<Object> addOnSuccessListener(@NonNull OnSuccessListener<? super Object> onSuccessListener) {
                    return this;
                }

                @NonNull
                @Override
                public Task<Object> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super Object> onSuccessListener) {
                    return this;
                }

                @NonNull
                @Override
                public Task<Object> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super Object> onSuccessListener) {
                    return this;
                }

                @NonNull
                @Override
                public Task<Object> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
                    return this;
                }

                @NonNull
                @Override
                public Task<Object> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                    return this;
                }

                @NonNull
                @Override
                public Task<Object> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                    return this;
                }
            };
        });

        Mockito.when(mockClient.requestConnection(Mockito.eq("endName"), Mockito.anyString(), Mockito.any(ConnectionLifecycleCallback.class))).thenAnswer(invocation -> {
            ConnectionInfo info = new ConnectionInfo("", "", false);
            ((ConnectionLifecycleCallback) invocation.getArguments()[2]).onConnectionInitiated("endName", info);
            ConnectionResolution resolution = new ConnectionResolution(new Status(ConnectionsStatusCodes.STATUS_OK));
            ((ConnectionLifecycleCallback) invocation.getArguments()[2]).onConnectionResult("endName", resolution);
            return null;
        });
    }

    @Test
    public void discover() {
        ServerConnection connection = ServerConnection.getInstance();
        Whitebox.setInternalState(connection, "connectionsClient", mockClient);
        assertNotEquals(null, Whitebox.getInternalState(connection, "connectionsClient"));
        ConnectCallback callback = new ConnectCallback() {
            @Override
            public void success(String message) {
            }

            @Override
            public void failure(String message) {
            }
        };
        connection.discover(callback, connection.buildEndpointDiscoveryCallback("", callback));
        List<String> expected = Arrays.asList("endName", "endName2", "endNameD1");
        assertEquals(expected, connection.getClients());
    }

    @Test
    public void connectTo() {
        ServerConnection connection = ServerConnection.getInstance();
        Whitebox.setInternalState(connection, "connectionsClient", mockClient);
        ConnectCallback callback = new ConnectCallback() {
            @Override
            public void success(String message) {
            }

            @Override
            public void failure(String message) {
                fail("Failed to connect");
            }
        };
        connection.discover(callback, connection.buildEndpointDiscoveryCallback("", callback));
        connection.connectTo("me",  callback);
        assertEquals("test", connection.getClientID());
    }

    @Test
    public void abort() {
        ServerConnection connection = ServerConnection.getInstance();
        Whitebox.setInternalState(connection, "connectionsClient", mockClient);
        connection.abort();
        assertNull(connection.getClientID());
        List<String> empty = Collections.emptyList();
        assertEquals(empty, connection.getClients());
    }

    @Test
    public void sendStream() throws IOException {
        ServerConnection connection = ServerConnection.getInstance();
        Whitebox.setInternalState(connection, "connectionsClient", mockClient);
        connection.sendStream();
    }
}