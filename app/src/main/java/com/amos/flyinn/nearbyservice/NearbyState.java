package com.amos.flyinn.nearbyservice;

/**
 * Define state of intents for Nearby Service.
 */
public enum NearbyState {
    // Instructions to start and stop NearbyService
    START,
    STOP,
    // Default state or state of errors
    UNKNOWN,
    // State of NearbyServer
    STARTED,
    STOPPED,
    // Connection state of nearby
    CONNECTED,
    DISCONNECTED;
}
