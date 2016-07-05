package com.aleclownes.slownotstupid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_FINE_LOCATION = 1;
    private API api;
    private TextView mTextView;
    private ViewGroup controlsLayout;
    private SignInButton signInButton;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = new API(this);
        mTextView = (TextView) findViewById(R.id.text_view);
        String token = api.getToken();
        controlsLayout = (LinearLayout) findViewById(R.id.controls_layout);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("194344242590-0gal7oam49i66o16gqj3lui34d09q3ot.apps.googleusercontent.com")
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Toast.makeText(MainActivity.this,
                                "Connection Failed: " + connectionResult.getErrorMessage(),
                                Toast.LENGTH_LONG);
                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        changeSignInView(token != null);
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
                integrator.shareText(api.getToken());
            }
        });
        final Button logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.removeToken();
                api.saveToken(null);
                changeSignInView(false);
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
        super.onActivityResult(requestCode, resultCode, intent);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(intent);
            Log.d(TAG, "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                changeSignInView(true);
                GoogleSignInAccount acct = result.getSignInAccount();
                api.saveToken(acct.getIdToken());
                try {
                    api.sendLocation(new ResponseListener(mTextView),
                            new ErrorListener(mTextView));
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
                Intent locationIntent = new Intent(MainActivity.this, LocationService.class);
                startService(locationIntent);
            } else {
                // Signed out, show unauthenticated UI.
                Toast.makeText(this, "Signed out", Toast.LENGTH_LONG);
                changeSignInView(false);
            }
        }
        else{
            //Barcode scan
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null && !TextUtils.isEmpty(scanResult.getContents())) {
                try {
                    api.sendInfect(scanResult.getContents(),
                            new ResponseListener(mTextView), new ErrorListener(mTextView));
                } catch (API.TokenMissingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void changeSignInView(boolean loggedIn){
        if (loggedIn){
            controlsLayout.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        }
        else{
            controlsLayout.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);
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
            api.removeToken();
        }

    }
}
