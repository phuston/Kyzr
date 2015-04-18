package com.phuston.android.kyzr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.Time;

/**
 * Created by phuston on 1/30/15.
 */
public class TorchActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String MIME_TYPE="application/com.phuston.android.kyzr";
    private NfcAdapter mNfcAdapter;
    private TorchFragment torchfrag;

    protected GoogleApiClient mGoogleApiClient;

    private NetworksClient client;

    protected static final String TAG = "kyzr_location_tag";
    protected Location mLastLocation;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);

        torchfrag=(TorchFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);

        if (torchfrag == null) {
            torchfrag=new TorchFragment();

            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, torchfrag)
                    .commit();
        }

        mNfcAdapter=NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mNfcAdapter.setNdefPushMessageCallback(this, this);

        buildGoogleApiClient();

        try {
            client = new NetworksClient();
        } catch (MalformedURLException e) {
            client = null;
        }
    }


    /**
     * Implementation for the CreateNdefMessageCallback interface
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String text = torchfrag.getTorchID();

        NdefMessage msg = new NdefMessage(NdefRecord.createMime(
                "application/com.phuston.android.kyzr", text.getBytes())
                /**
                 * The Android Application Record (AAR) is commented out. When a device
                 * receives a push with an AAR in it, the application specified in the AAR
                 * is guaranteed to run. The AAR overrides the tag dispatch system.
                 * You can add it back in to guarantee that this
                 * activity starts when receiving a beamed message. For now, this code
                 * uses the tag dispatch system.
                 */
                //,NdefRecord.createApplicationRecord("com.example.android.beam")
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
        torchfrag.addTorch(new String(msg.getRecords()[0].getPayload()));

        // TODO Network Shit.
        if(client != null) {
            String receivedId = new String(msg.getRecords()[0].getPayload());
            String phoneId = torchfrag.getTorchID();
            double lat = mLastLocation.getLatitude();
            double lng = mLastLocation.getLongitude();

            try {
                String formatURL = client.formatRequest(phoneId, receivedId, lat, lng);

                AccessThread at = new AccessThread();
                String returnMessage = at.execute(formatURL).get();

                Toast.makeText(this, returnMessage, Toast.LENGTH_LONG).show();

            } catch(Exception e ) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }



    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    //Google Play GPS Client methods
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        torchfrag.updateLocation(mLastLocation);
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

    //Overriding activity lifecycle methods

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
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

    public class AccessThread extends AsyncTask<String, Void, String> {

        public String doInBackground(String... params) {

            if(client != null) {
                String request = params[0];
                try {
                    return client.access(request);
                } catch (IOException e) {
                    return "Could not connect";
                }
            }

            return "Request Failed";
        }
    }
}
