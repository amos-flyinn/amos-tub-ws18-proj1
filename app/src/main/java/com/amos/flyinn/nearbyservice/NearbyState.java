package com.amos.flyinn.nearbyservice;

/**
 * Define possible states of the Nearby service. These are set by the service itself or by the
 * server.
 */
public enum NearbyState {
    // Default state or state of errors
    UNKNOWN,
    STOPPED,
    ADVERTISING,
    CONNECTING,
    CONNECTED,
}
