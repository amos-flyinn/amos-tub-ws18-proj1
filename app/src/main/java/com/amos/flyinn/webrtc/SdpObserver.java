package com.amos.flyinn.webrtc;

import android.app.Activity;
import android.util.Log;

import com.amos.flyinn.ConnectionSetupActivity;

import org.webrtc.SessionDescription;

/**
 * <h1>SdpObserver Class</h1>
 *
 * <p>This class helps to track the possible states of the WebRTC Stream protocol. It
 * proceed to handle failure and successfull states.
 * </p>
 */
public class SdpObserver implements org.webrtc.SdpObserver {
    public final static int LOCAL_SDP = 0;
    public final static int REMOTE_SDP = 1;

    private String name;
    private ConnectionSetupActivity activity;
    private int type;
    private String tag = "SdpObserver";


    /**
     * Constructor for creating a new observer that track the WebRTC states protocol
     *
     * @param nameObserver the observer name to recognize the ObserverObject in the logs
     * @param activity     the activity to access the TextView
     * @param type         the type of the Observer. Remote or Local. Please look at the constants at {@link com.amos.flyinn.webrtc.SdpObserver }
     */
    public SdpObserver(String nameObserver, Activity activity, int type) {
        this.tag = nameObserver;
        this.type = type;
        this.activity = (ConnectionSetupActivity) activity;
    }

    /**
     * Callback to inform the successfully creation of the Session descriptor message.
     * It also inform the UI over the UI thread to update the new successfully state
     * on the State TextView description
     * <p>
     * This method is going to be called when the descriptor was created locally or remote
     *
     * @param sessionDescription the session descriptor created by the WebRTC library
     */
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(tag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
        Log.d(tag, sessionDescription.description);
        Log.d(tag, sessionDescription.type.canonicalForm());
        if (this.type == LOCAL_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.LOCAL_DESCRIPTOR_CREATE);
                }
            });
        }

        if (this.type == REMOTE_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.REMOTE_DESCRIPTOR_CREATE);
                }
            });
        }

    }

    /**
     * Callback to inform the successfully setting of the Session descriptor message.
     * It also inform the UI over the UI thread to update the new successfully state
     * on the State TextView description.
     * <p>
     * This method is going to be called when the Session descriptor was setted in a remote or local form
     */
    @Override
    public void onSetSuccess() {
        Log.d(tag, "onSetSuccess() called");
        if (this.type == LOCAL_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.LOCAL_DESCRIPTOR_SETTED);
                }
            });
        }

        if (this.type == REMOTE_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.REMOTE_DESCRIPTOR_SETTED);
                }
            });
        }

    }

    /**
     * Callback to inform the failure at the moment of creating a new Session descriptor message.
     * It also inform the UI over the UI thread to update the to the new failure state and to handle it
     * on the State TextView description.
     * <p>
     * This method is going to be called when the Session descriptor failed to be created in a remote or local form
     */
    @Override
    public void onCreateFailure(String s) {
        Log.d(tag, "onCreateFailure() called with: s = [" + s + "]");
        if (this.type == LOCAL_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_CREATING_LOCAL_DESCRIPTOR);
                }
            });
        }

        if (this.type == REMOTE_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_CREATING_REMOTE_DESCRIPTOR);
                }
            });
        }
    }

    /**
     * Callback to inform the failure at the moment of setting a new Session descriptor message.
     * It also inform the UI over the UI thread to update the to the new failure state and to handle it
     * on the State TextView description.
     * <p>
     * This method is going to be called when the Session descriptor failed to be setted in a remote or local form
     *
     * @param s the message description of the failure
     */

    @Override
    public void onSetFailure(String s) {
        Log.d(tag, "onSetFailure() called with: s = [" + s + "]");
        if (this.type == LOCAL_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_SETTED_LOCAL_DESCRIPTION);
                }
            });
        }

        if (this.type == REMOTE_SDP) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_SETTED_REMOTE_DESCRIPTION);
                }
            });
        }
    }

}
