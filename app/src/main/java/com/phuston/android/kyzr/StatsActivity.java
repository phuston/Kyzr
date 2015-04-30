package com.phuston.android.kyzr;

import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class StatsActivity extends ActionBarActivity {

    private NetworksClient mNetworkClient;
    private String mAndroid_id;

    private Gson mGson;
    private Stats mStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mAndroid_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mNetworkClient = new NetworksClient();

        mGson = new GsonBuilder().create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getStats(){
        String response = "";
        try {
            String formatted = mNetworkClient.formatStats(mAndroid_id);
            AccessThread at = new AccessThread();

            response = at.execute(formatted).get();
        } catch (Exception e) {
            e.toString();
        }

        mStats = mGson.fromJson(response, Stats.class);

        updateStats();
    }

    public void updateStats(){
        //TODO: Do shit to textviews here to update view
    }


    public class AccessThread extends AsyncTask<String, Void, String> {

        public String doInBackground(String... params) {

            if(mNetworkClient != null) {
                String request = params[0];

                URL sendTo;

                try {
                    sendTo = new URL("http://thekyzrproject.com/stats");
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
}
