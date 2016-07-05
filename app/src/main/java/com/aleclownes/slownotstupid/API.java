package com.aleclownes.slownotstupid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alownes on 6/30/2016.
 */

public class API {

    private RequestQueue queue;
    private Context context;
    private final String url = BuildConfig.API_URL;
    private String token;
    private final int MY_PERMISSIONS_FINE_LOCATION = 1;
    private final String TAG = "API";

    public API(Context context){
        this.context = context;
        queue = Volley.newRequestQueue(context);
        token = getToken();
    }

    public void sendLocation(Response.Listener<JSONObject> responseListener,
                        Response.ErrorListener errorListener) throws TokenMissingException{
        Log.v(TAG, "sendLocation");
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null){
                try {
                    sendLocation(0, 0,
                            responseListener, errorListener);
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    sendLocation(loc.getLatitude(),
                            loc.getLongitude(), responseListener,
                            errorListener);
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
            }

        }
        else{
            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_FINE_LOCATION);
        }
    }

    public void sendLocation(double latitude,
                             double longitude, Response.Listener<JSONObject> responseListener,
                             Response.ErrorListener errorListener) throws TokenMissingException{
        if (token == null) {
            throw new TokenMissingException();
        }
        final String endpoint = "location/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", token);
            jsonRequest.put("lat", latitude);
            jsonRequest.put("lon", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public void createIt(Response.Listener<JSONObject> responseListener,
                         Response.ErrorListener errorListener) throws TokenMissingException{
        if (token == null){
            throw new TokenMissingException();
        }
        final String endpoint = "create/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public void sendInfect(String code,
                           Response.Listener<JSONObject> responseListener,
                           Response.ErrorListener errorListener) throws TokenMissingException{
        if (token == null){
            throw new TokenMissingException();
        }
        final String endpoint = "infect/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", token);
            jsonRequest.put("id", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public void getScores(Response.Listener<JSONObject> responseListener,
                          Response.ErrorListener errorListener){
        final String endpoint = "scores/";
        String totalUrl = url + endpoint + "?token="+token;
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, totalUrl, null, responseListener, errorListener);
        queue.add(jsObjRequest);
    }

    public String getToken(){
        if (token == null){
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                    Context.MODE_PRIVATE);
            return sharedPref.getString(context.getString(R.string.token_key), null);
        }
        else{
            return token;
        }
    }

    public void saveToken(String token){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.putString(context.getString(R.string.token_key), token);
        edit.commit();
        this.token = token;
    }

    class TokenMissingException extends Exception{
        public TokenMissingException(){
            super();
        }
    }

    public void removeToken(){
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.shared_preferences_file_key),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPref.edit();
        edit.remove(context.getString(R.string.token_key));
        edit.commit();
        this.token = null;
    }

}
