package com.phuston.android.kyzr;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import java.nio.charset.Charset;

/**
 * Created by phuston on 1/30/15.
 */
public class TorchActivity extends Activity implements NfcAdapter.CreateNdefMessageCallback {
    private static final String MIME_TYPE="application/com.phuston.android.beamer";
    private NfcAdapter mNfcAdapter=null;
    private TorchFragment torchfrag=null;

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
}
