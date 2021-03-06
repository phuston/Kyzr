package com.phuston.android.kyzr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
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

    private boolean mKillActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mNetworkClient = new NetworksClient();
        mAndroid_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationDialog();
        } else {
            checkIfIdExists();
        }

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
            String response;

            try {
                response = vt.execute("verify", initialVerify).get();
            } catch(Exception e) {
                response = e.getMessage();
            }

            if(response.equals("True")) {
                Bundle extras = new Bundle();
                extras.putBoolean("Create User", false);
                mKillActivity = true;
                startTorchActivity(extras);
            }  else if(!response.equals("False") && !response.equals("Invalid Search")) {
                connectionError();
            }
        }
    }


    public void connectionError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Cannot Connect to Kyzr");
        builder.setMessage("Cannot find the server. This could be a problem on our end. Make sure you have internet enabled as well.");
        builder.setNegativeButton("Exit Kyzr", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                return;
            }
        });

        builder.create().show();
    }

    public void displayLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Location Not Available");
        builder.setMessage("In order to collect information about each torch transfer, Kyzr must have access to your location.");
        builder.setPositiveButton("Enable Location", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
        if(mKillActivity) {
            finish();
        }
    }

    public void onResume() {
        super.onResume();
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            displayLocationDialog();
        } else {
            checkIfIdExists();
        }
    }

    public void submitInfo() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            String newUsername = mNewUser.getText().toString();
            String formatUser = newUsername.replace("[^A-Za-z0-9_-]", "");

            if(newUsername.length() < 12){
                if (!newUsername.equals("")) {
                    VerifyThread vt = new VerifyThread();
                    String response = "";

                    try {
                        // Make Request In Next Activity
                        String formattedRequest = mNetworkClient.formatVerify(newUsername);
                        response = vt.execute("verify", formattedRequest).get();
                    } catch (Exception e) {
                        response = e.toString();
                    }

                    if (response.equals("False")) {
                        Toast.makeText(this, "Welcome to Kyzr!", Toast.LENGTH_LONG).show();
                        Bundle extras = new Bundle();
                        extras.putString("Username", formatUser);
                        extras.putBoolean("Create User", true);
                        startTorchActivity(extras);
                    } else {
                        mNewUser.setText("");
                        Toast.makeText(this, "Username already exists.", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(this, "Torch name cannot exceed 12 characters.", Toast.LENGTH_LONG).show();
            }

        } else {
            displayLocationDialog();
        }
    }

    public void onClick(View v) {
        submitInfo();
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
