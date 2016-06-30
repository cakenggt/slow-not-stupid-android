package com.aleclownes.slownotstupid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_FINE_LOCATION = 1;
    private RequestQueue queue;
    private String url ="http://10.0.2.2:3000/";
    private TextView mTextView;
    private EditText mTokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        mTextView = (TextView) findViewById(R.id.text_view);
        mTokenText = (EditText) findViewById(R.id.token_text);
        final Button setTokenButton = (Button) findViewById(R.id.token_button_id);
        setTokenButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                if (ContextCompat.checkSelfPermission(v.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                    Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (loc == null){
                        sendLocation(0, 0);
                    }
                    else{
                        sendLocation(loc.getLatitude(), loc.getLongitude());
                    }

                }
                else{
                    ActivityCompat.requestPermissions((Activity)v.getContext(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_FINE_LOCATION);
                }
            }
        });
        final Button createItButton = (Button) findViewById(R.id.create_button);
        createItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIt();
            }
        });
        final Button scanCodeButton = (Button) findViewById(R.id.scan_code_button);
        scanCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan();
            }
        });
        final Button viewCodeButton = (Button) findViewById(R.id.view_code_button);
        viewCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.shareText(mTextView.getText());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        final TextView mTextView = (TextView) findViewById(R.id.text_view);
        switch (requestCode) {
            case MY_PERMISSIONS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED){
                        LocationManager locationManager = (LocationManager)
                                getSystemService(Context.LOCATION_SERVICE);
                        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        sendLocation(loc.getLatitude(), loc.getLongitude());
                    }

                } else {

                    mTextView.setText("Permission Denied!");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            sendInfect(scanResult.getContents());
        }
    }

    private void sendLocation(double latitude, double longitude){
        final String endpoint = "location/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", mTokenText.getText());
            jsonRequest.put("lat", latitude);
            jsonRequest.put("lon", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response){
                        mTextView.setText("Response: " + response.toString());
                    }

                }, new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText("Error: " + error.toString());
                    }

                });
        queue.add(jsObjRequest);
    }

    private void createIt(){
        final String endpoint = "create/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", mTokenText.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response){
                        mTextView.setText("Response: " + response.toString());
                    }

                }, new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText("Error: " + error.toString());
                    }

                });
        queue.add(jsObjRequest);
    }

    private void sendInfect(String code){
        final String endpoint = "infect/";
        String totalUrl = url + endpoint;
        JSONObject jsonRequest = new JSONObject();
        try {
            jsonRequest.put("token", mTokenText.getText());
            jsonRequest.put("id", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, totalUrl, jsonRequest, new Response.Listener<JSONObject>(){

                    @Override
                    public void onResponse(JSONObject response){
                        mTextView.setText("Response: " + response.toString());
                    }

                }, new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mTextView.setText("Error: " + error.toString());
                    }

                });
        queue.add(jsObjRequest);
    }
}
