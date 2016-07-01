package com.aleclownes.slownotstupid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

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

    public API(Activity activity){
        context = activity;
        queue = Volley.newRequestQueue(activity);
        token = getToken();
    }

    public void sendLocation(double latitude,
                             double longitude, Response.Listener<JSONObject> responseListener,
                             Response.ErrorListener errorListener) throws TokenMissingException{
        if (token == null){
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
