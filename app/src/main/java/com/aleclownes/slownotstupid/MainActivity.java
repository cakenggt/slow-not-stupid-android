package com.aleclownes.slownotstupid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_FINE_LOCATION = 1;
    private API api;
    private TextView mTextView;
    private EditText mTokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new API(this);
        mTextView = (TextView) findViewById(R.id.text_view);
        mTokenText = (EditText) findViewById(R.id.token_text);
        String token = api.getToken();
        if (token != null){
            mTokenText.setText(token);
        }
        final Button setTokenButton = (Button) findViewById(R.id.token_button_id);
        setTokenButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                api.saveToken(mTokenText.getText().toString());
                try {
                    api.sendLocation(new ResponseListener(mTextView),
                            new ErrorListener(mTextView));
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
                Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                startService(locationIntent);
            }
        });
        final Button createItButton = (Button) findViewById(R.id.create_button);
        createItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    api.createIt(new ResponseListener(mTextView),
                            new ErrorListener(mTextView));
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
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
                integrator.shareText(mTokenText.getText().toString());
            }
        });
        final Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.removeToken();
                mTokenText.setText("");
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
                        try {
                            api.sendLocation(loc.getLatitude(),
                                    loc.getLongitude(), new ResponseListener(mTextView),
                                    new ErrorListener(mTextView));
                        } catch (API.TokenMissingException e) {
                            e.printStackTrace();
                        }
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
            try {
                api.sendInfect(scanResult.getContents(),
                        new ResponseListener(mTextView), new ErrorListener(mTextView));
            } catch (API.TokenMissingException e) {
                e.printStackTrace();
            }
        }
    }

    class ResponseListener implements Response.Listener<JSONObject>{

        private TextView textView;

        public ResponseListener(TextView textView){
            this.textView = textView;
        }

        @Override
        public void onResponse(JSONObject response){
            if (textView != null) {
                textView.setText("Response: " + response.toString());
            }
        }

    }

    class ErrorListener implements Response.ErrorListener{

        private TextView textView;

        public ErrorListener(TextView textView){
            this.textView = textView;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (textView != null) {
                textView.setText("Error: " + error.toString());
            }
        }

    }
}
