package com.amos.server;

import android.content.Context;
import android.content.Intent;
import android.os.Process;


import com.amos.server.ErrorScreen.ErrorActivity;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * this activity will handle crashes and redirect to the error activity which will log the error.
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    Context context;
    public static Boolean crashHappened = false;

    public ExceptionHandler(Context context) {
        this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable exception) {
        this.crashHappened = true;
        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        //start the error activity
        Intent intent = new Intent(context, ErrorActivity.class);
        intent.putExtra("error_message", stackTrace.toString());
        context.startActivity(intent);

        //kill the current activity
        Process.killProcess(Process.myPid());
        System.exit(0);
    }
}
