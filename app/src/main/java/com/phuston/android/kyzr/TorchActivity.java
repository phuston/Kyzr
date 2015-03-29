package com.phuston.android.kyzr;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.nio.charset.Charset;

/**
 * Created by phuston on 1/30/15.
 */
public class TorchActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String MIME_TYPE="application/com.phuston.android.beamer";
    private NfcAdapter mNfcAdapter=null;
    private TorchFragment torchfrag=null;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    protected static final String TAG = "basic-location-sample";

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torch);

        //TODO: connect latitude and longitude textviews

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

        findViewById(R.id.fragmentContainer).post(new Runnable() {
            public void run() {
                handleIntent(getIntent());
            }
        });
    }

    private void handleIntent(Intent i) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(i.getAction())) {
            Parcelable[] rawMsgs=i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msg=(NdefMessage)rawMsgs[0];
            String TorchID=new String(msg.getRecords()[0].getPayload());

            torchfrag.addTorch(TorchID);
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent arg0) {
        NdefRecord uriRecord=
                new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                        MIME_TYPE.getBytes(Charset.forName("US-ASCII")),
                        new byte[0],

                        torchfrag.getTorchID()
                                .getBytes(Charset.forName("US-ASCII")));
        NdefMessage msg=
                new NdefMessage(
                        new NdefRecord[] {
                                uriRecord,
                                NdefRecord.createApplicationRecord("com.phuston.android.beamer") });

        return(msg);
    }

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

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs=intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage msg=(NdefMessage)rawMsgs[0];
        String TorchID=new String(msg.getRecords()[0].getPayload());

        torchfrag.addTorch(TorchID);
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

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
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
}
