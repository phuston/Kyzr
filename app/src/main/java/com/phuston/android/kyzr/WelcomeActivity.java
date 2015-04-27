package com.phuston.android.kyzr;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URL;


public class WelcomeActivity extends Activity implements View.OnClickListener {

    private NetworksClient mNetworkClient;
    private String mAndroid_id;

    private TorchFragment mTorchFrag;

    private EditText mNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mTorchFrag = new TorchFragment();

        mNetworkClient = new NetworksClient();
        mAndroid_id = mTorchFrag.getTorchID();



        checkIfIdExists();

        mNewUser = (EditText) findViewById(R.id.etTorchName);
        Button submit = (Button) findViewById(R.id.bSubmitTorch);

        submit.setOnClickListener(this);
    }

    public void checkIfIdExists() {
        String initialVerify = "";

        try {
            initialVerify = mNetworkClient.formatVerify(mAndroid_id);
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if(!initialVerify.equals("")) {
            VerifyThread vt = new VerifyThread();
            String response = "";

            try {
                response = vt.execute("verify", initialVerify).get();
            } catch(Exception e) {
                response = e.getMessage();
            }

            if(response.equals("True")) {
                Bundle extras = new Bundle();
                extras.putBoolean("Create User", false);
                startTorchActivity(extras);
            }
        }
    }

    public void startTorchActivity(Bundle extras) {
        try {
            Intent activityStarter = new Intent(this, Class.forName("com.phuston.android.kyzr.TorchActivity"));
            if(extras != null) {
                activityStarter.putExtras(extras);
            }
            startActivity(activityStarter);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Could not continue", Toast.LENGTH_LONG).show();
        }
    }

    public void onPause() {
        super.onPause();
        finish();
    }

    public void onClick(View v) {
        String newUsername = mNewUser.getText().toString();
        newUsername.replace("[^A-Za-z0-9_-]", "");

        if(!newUsername.equals("")) {
            VerifyThread vt = new VerifyThread();
            String response = "";

            try {
                // Make Request In Next Activity
                String formattedRequest = mNetworkClient.formatVerify(newUsername);
                response = vt.execute("verify", formattedRequest).get();
            } catch(Exception e) {
                response = e.toString();
            }

            if(response.equals("False")) {
                Toast.makeText(this, "Welcome to Kyzr!", Toast.LENGTH_LONG).show();
                Bundle extras = new Bundle();
                extras.putString("Username", newUsername);
                extras.putBoolean("Create User", true);
                startTorchActivity(extras);
            } else {
                mNewUser.setText("");
                Toast.makeText(this, "User already exists.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class VerifyThread extends AsyncTask<String, Void, String> {

        public String doInBackground(String... params) {
            String method = params[0];
            String request = params[1];

            String response = "";

            String address = "";

            switch (method) {
                case "verify":
                    address = "http://thekyzrproject.com/verify";
                    break;
                case "adduser":
                    address = "http://thekyzrproject.com/newuser";
                    break;
            }

            if(!address.equals("")) {
                try {
                    URL sendTo = new URL(address);
                    response = mNetworkClient.access(request, sendTo);
                } catch (Exception e) {
                    response = e.toString();
                }

                return response;
            }

            return "Error";
        }

    }
}
