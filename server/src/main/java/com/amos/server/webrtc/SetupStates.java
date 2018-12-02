package com.amos.flyinn.webrtc;

public class SetupStates {
    public final static int
            ERROR_CONNECTING_SERVER = -7,
            FAIL_SENDING_SESSION_DESCRIPTOR = -6,
            FAIL_CREATING_REMOTE_DESCRIPTOR = -5,
            FAIL_CREATING_LOCAL_DESCRIPTOR = -4,
            FAIL_SETTED_LOCAL_DESCRIPTION = -3,
            FAIL_SETTED_REMOTE_DESCRIPTION = -2,
            PERMISSIONS_FOR_SCREENCAST_DENIED = -1,
            ASKING_PERMISSIONS = 0,
            SETUP_SCREEN_CONNECTION = 1,
            LOCAL_DESCRIPTOR_CREATE = 2,
            REMOTE_DESCRIPTOR_CREATE = 3,
            LOCAL_DESCRIPTOR_SETTED = 4,
            REMOTE_DESCRIPTOR_SETTED = 5;


}
