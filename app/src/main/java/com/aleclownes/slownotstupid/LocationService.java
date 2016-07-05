package com.aleclownes.slownotstupid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alownes on 7/5/2016.
 */

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private Timer mTimer;
    private int NOTIFY_INTERVAL = 1000 * 60 * 5;//5 minutes
    private API api;
    private int mId = 5;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Toast.makeText(this, "LocationService Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "LocationService Started", Toast.LENGTH_LONG).show();
        mTimer = new Timer();
        api = new API(getApplicationContext());
        mTimer.scheduleAtFixedRate(new LocationTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class LocationTimerTask extends TimerTask{

        @Override
        public void run() {
            try{
                api.sendLocation(new LocationService.ResponseListener(),
                        new LocationService.ErrorListener());
            } catch (API.TokenMissingException e){
                e.printStackTrace();
            }

        }
    }

    class ResponseListener implements Response.Listener<JSONObject>{

        @Override
        public void onResponse(JSONObject response){
            Toast.makeText(getApplicationContext(),
                    "Success in sending location: "+response.toString(),
                    Toast.LENGTH_LONG).show();
            try{
                if (response.has("nearby") && !TextUtils.isEmpty(response.getString("nearby"))) {
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(android.R.drawable.edit_text)
                                    .setContentTitle("You hear a sound")
                                    .setContentText(response.getString("nearby") + " is nearby");
                    // Creates an explicit intent for an Activity in your app
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the
                    // started Activity.
                    // This ensures that navigating backward from the Activity leads out of
                    // your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    // Adds the back stack for the Intent (but not the Intent itself)
                    stackBuilder.addParentStack(MainActivity.class);
                    // Adds the Intent that starts the Activity to the top of the stack
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent =
                            stackBuilder.getPendingIntent(
                                    0,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    // mId allows you to update the notification later on.
                    mNotificationManager.notify(mId, mBuilder.build());
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

        }

    }

    class ErrorListener implements Response.ErrorListener{

        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(),
                    "Error in location service: "+error.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }
}
