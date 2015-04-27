package com.phuston.android.kyzr;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

    private EditText mNewUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        mNetworkClient = new NetworksClient();
        mAndroid_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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
                startTorchActivity();
            }
        }
    }

    public void startTorchActivity() {
        try {
            Intent activityStarter = new Intent(this, Class.forName("com.phuston.android.kyzr.TorchActivity"));
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
                String formattedRequest = mNetworkClient.formatAddToDatabase(mAndroid_id, newUsername);
                response = vt.execute("adduser", formattedRequest).get();
            } catch(Exception e) {
                response = e.toString();
            }

            if(response.equals("True")) {
                Toast.makeText(this, "Welcome to Kyzr!", Toast.LENGTH_LONG).show();
                startTorchActivity();
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
