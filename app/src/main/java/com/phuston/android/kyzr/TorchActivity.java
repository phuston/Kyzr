package com.phuston.android.kyzr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by phuston on 1/30/15.
 */
public class TorchActivity extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String MIME_TYPE="application/com.phuston.android.kyzr";
    private NfcAdapter mNfcAdapter;
    private TorchFragment mTorchFrag;

    private NetworksClient mNetworkClient;

    protected static final String TAG = "kyzr_location_tag";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);

        // Connects activity to child fragment
        mTorchFrag=(TorchFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (mTorchFrag == null) {
            mTorchFrag=new TorchFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, mTorchFrag)
                    .commit();
        }


        // Initializations for NFC adapter
        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {

            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        checkNFC();

        mNfcAdapter.setNdefPushMessageCallback(this, this);

        // Initializations for GPS
        mRequestingLocationUpdates = true;
        mLastUpdateTime = "";

        buildGoogleApiClient();

        // Initialization for NetworksClient
        updateValuesFromBundle(savedInstanceState);
        mNetworkClient = new NetworksClient();
    }


    public void checkNFC() {
        if(!mNfcAdapter.isEnabled()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("NFC Disabled");
            builder.setMessage("In order to use Kyzr, you must have NFC enabled. Otherwise, you won't be able to transfer torches! Click okay to enable NFC!");
            builder.setPositiveButton("Enable NFC", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent getNFC;
                    if (Build.VERSION.SDK_INT >= 16) {
                        getNFC = new Intent(android.provider.Settings.ACTION_NFC_SETTINGS);
                    } else {
                        getNFC = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                    }

                    startActivity(getNFC);
                }
            });

            builder.setNegativeButton("Exit Kyzr", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                    return;
                }
            });

            builder.create().show();
        }
    }


    /**
     * Methods for accessing database from server
     */
    public void getCurrTorch() {

        String response = "";
        try {
            String formatted = mNetworkClient.formatGetCurrTorch(mTorchFrag.getTorchID());
            AccessThread at = new AccessThread();

            response = at.execute("currtorch", formatted).get();
        } catch (Exception e) {
            e.toString();
        }
        mTorchFrag.updateCurrTorch(response);
    }

    public void addUser() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            boolean create = extras.getBoolean("Create User");

            if(create) {
                double latitude = mCurrentLocation.getLatitude();
                double longitude = mCurrentLocation.getLongitude();

                String username = extras.getString("Username");
                String lat = String.valueOf(latitude);
                String lng = String.valueOf(longitude);
                String id = mTorchFrag.getTorchID();

                String response = "";

                try {
                    String formatted = mNetworkClient.formatAddToDatabase(id, username, lat, lng);
                    AccessThread at = new AccessThread();

                    response = at.execute("create", formatted).get();
                } catch (Exception e) {
                    e.toString();
                }

                if(!response.equals("True")) {
                    Toast.makeText(this, "Could not create user", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Successful!", Toast.LENGTH_LONG).show();
                    getCurrTorch();
                }

            }
        }
    }


    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = mTorchFrag.getTorchID();

        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
                MIME_TYPE, text.getBytes())
                ,NdefRecord.createApplicationRecord("com.phuston.android.kyzr")
        );
        return msg;
    }


    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        if(mNetworkClient != null) {
            String receivedId = new String(msg.getRecords()[0].getPayload());
            String phoneId = mTorchFrag.getTorchID();
            double lat = mCurrentLocation.getLatitude();
            double lng = mCurrentLocation.getLongitude();

            try {
                String formatURL = mNetworkClient.formatRequest(phoneId, receivedId, lat, lng);

                AccessThread at = new AccessThread();
                String returnMessage = at.execute("add", formatURL).get();

                getIntent().setAction("android.intent.action.MAIN");
                Toast.makeText(this, returnMessage, Toast.LENGTH_LONG).show();

            } catch(Exception e ) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
            }
            // Update the value of mCurrentLocation from the Bundle
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            addUser();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    // TODO: Implement this so transaction can only occur if user has network
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Thread for accessing the server.
     */
    public class AccessThread extends AsyncTask<String, Void, String> {

        public String doInBackground(String... params) {

            if(mNetworkClient != null) {
                String method = params[0];
                String request = params[1];

                URL sendTo = null;

                try {
                    switch (method) {
                        case "add":
                            sendTo = new URL("http://thekyzrproject.com/dbadd");
                            break;
                        case "create":
                            sendTo = new URL("http://thekyzrproject.com/newuser");
                            break;
                        case "stats":
                            sendTo = new URL("http://thekyzrproject.com/stats");
                            break;
                        case "currtorch":
                            sendTo = new URL("http://thekyzrproject.com/currtorch");
                            break;
                    }
                } catch (MalformedURLException e) {
                    return "Error Occurred";
                }

                try {
                    if(sendTo != null) {
                        return mNetworkClient.access(request, sendTo);
                    }
                } catch(Exception e) {
                    return e.toString();
                }
            }

            return "Request Failed";
        }
    }

    //Overriding activity lifecycle methods

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        } else {
            checkNFC();
        }

        getCurrTorch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
