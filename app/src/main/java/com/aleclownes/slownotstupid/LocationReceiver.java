package com.aleclownes.slownotstupid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by alownes on 7/5/2016.
 */

public class LocationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent locationIntent = new Intent(context,LocationService.class);
        context.startService(locationIntent);
        Log.i("LocationReceiver", "started");
    }
}
