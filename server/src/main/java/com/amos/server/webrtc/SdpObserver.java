package com.amos.server.webrtc;

import android.app.Activity;
import android.util.Log;

import com.amos.server.ConnectionSetupServerActivity;
import com.amos.server.webrtc.SetupStates;

import org.webrtc.SessionDescription;

import java.util.Set;


public class SdpObserver implements org.webrtc.SdpObserver {
    public final static int LOCAL_SDP = 0;
    public final static int REMOTE_SDP = 1;

    private String name;
    private ConnectionSetupServerActivity activity;
    private int type;
    public SdpObserver(String nameObserver, Activity activity,int type)
    {
        this.tag = nameObserver;
        this.type = type;
        this.activity = (ConnectionSetupServerActivity) activity;
    }


    private String tag = "SdpObserver";


    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(tag, "onCreateSuccess() called with: sessionDescription = [" + sessionDescription + "]");
        Log.d(tag,sessionDescription.description);
        Log.d(tag,sessionDescription.type.canonicalForm());
        if(this.type == LOCAL_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.LOCAL_DESCRIPTOR_CREATE);
                }
            });
        }

        if(this.type == REMOTE_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.REMOTE_DESCRIPTOR_CREATE);
                }
            });
        }

    }

    @Override
    public void onSetSuccess() {
        Log.d(tag, "onSetSuccess() called");
        if(this.type == LOCAL_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.LOCAL_DESCRIPTOR_SETTED);
                }
            });
        }

        if(this.type == REMOTE_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.REMOTE_DESCRIPTOR_SETTED);
                }
            });
        }

    }

    @Override
    public void onCreateFailure(String s) {
        Log.d(tag, "onCreateFailure() called with: s = [" + s + "]");
        if(this.type == LOCAL_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_CREATING_LOCAL_DESCRIPTOR);
                }
            });
        }

        if(this.type == REMOTE_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_CREATING_REMOTE_DESCRIPTOR);
                }
            });
        }
    }

    @Override
    public void onSetFailure(String s) {
        Log.d(tag, "onSetFailure() called with: s = [" + s + "]");
        if(this.type == LOCAL_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_SETTED_LOCAL_DESCRIPTION);
                }
            });
        }

        if(this.type == REMOTE_SDP)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.setStateText(SetupStates.FAIL_SETTED_REMOTE_DESCRIPTION);
                }
            });
        }
    }

}
