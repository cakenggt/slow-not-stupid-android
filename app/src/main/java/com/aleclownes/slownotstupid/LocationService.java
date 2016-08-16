package com.aleclownes.slownotstupid;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alownes on 7/5/2016.
 */

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private int NOTIFY_INTERVAL = 1000 * 60 * 5;//5 minutes
    private API api;
    private int mId = 5;
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onCreate(){
        api = new API(getApplicationContext());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("194344242590-0gal7oam49i66o16gqj3lui34d09q3ot.apps.googleusercontent.com")
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent locationIntent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pi = PendingIntent.getService(getApplicationContext(), 0, locationIntent, 0);
        AlarmManager am = (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                0,
                NOTIFY_INTERVAL, pi);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.v(TAG, "onStartCommand");
        try{
            OptionalPendingResult<GoogleSignInResult> pendingResult =
                    Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (pendingResult.isDone()) {
                // There's immediate result available.
                Log.v(TAG, "immediate result available from silent sign in");
                GoogleSignInAccount acct = pendingResult.get().getSignInAccount();
                api.saveCredentials(acct);
                api.sendLocation(new LocationService.ResponseListener(),
                        new LocationService.ErrorListener());
            } else {
                // There's no immediate result ready
                Log.v(TAG, "pending result from silent sign in");
                pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(@NonNull GoogleSignInResult result) {
                        if (result.isSuccess()){
                            Log.v(TAG, "successful silent sign in");
                            GoogleSignInAccount acct = result.getSignInAccount();
                            api.saveCredentials(acct);
                            try{
                                api.sendLocation(new LocationService.ResponseListener(),
                                        new LocationService.ErrorListener());
                            } catch (API.TokenMissingException e){
                                e.printStackTrace();
                            }
                        }
                        else{
                            Log.v(TAG, "silent sign in unsuccessful");
                        }


                    }
                });
            }
        } catch (API.TokenMissingException e){
            e.printStackTrace();
        }
        return START_STICKY;
    }

    class ResponseListener implements Response.Listener<JSONObject>{

        @Override
        public void onResponse(JSONObject response){
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
