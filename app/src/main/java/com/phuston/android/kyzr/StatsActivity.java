package com.phuston.android.kyzr;

import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.ResponseCache;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.w3c.dom.Text;


public class StatsActivity extends ActionBarActivity {
    private String TAG = "StatsActivity";

    private NetworksClient mNetworkClient;
    private String mAndroid_id;

    private Gson mGson;
    private Stats mStats1;
    private Stats mStats2;

    private TextView mUsernameTextview, mPhoneIDTextview, mDistance1Textview, mNumTrans1Textview, mCurrentOwnerTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        mNetworkClient = new NetworksClient();
        mGson = new GsonBuilder().create();
        mAndroid_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        mUsernameTextview = (TextView)findViewById(R.id.username_textview);
        mPhoneIDTextview = (TextView)findViewById(R.id.phoneid_textview);
        mDistance1Textview = (TextView)findViewById(R.id.distance1_textview);
        mNumTrans1Textview = (TextView)findViewById(R.id.numtransactions1_textview);
        mCurrentOwnerTextview = (TextView)findViewById(R.id.currentowner1_textview);

        getStats();

        mUsernameTextview.setText(mStats1.getUSERNAME());
        mPhoneIDTextview.setText(mAndroid_id);
        mDistance1Textview.setText(mStats1.getDISTANCE());
        mNumTrans1Textview.setText(mStats1.getNUMTRANSACTION());
        mCurrentOwnerTextview.setText(mStats1.getTORCH());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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

        Log.e(TAG, response);

        mStats1 = mGson.fromJson(response, Stats.class);

        String response2 = "";
        try {
            String formatted = mNetworkClient.formatStats(mStats1.getTORCH());
            AccessThread at = new AccessThread();

            response2 = at.execute(formatted).get();
        } catch (Exception e) {
            e.toString();
        }

        mStats2 = mGson.fromJson(response2, Stats.class);
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

    @Override
    public void onResume(){
        super.onResume();
        getStats();
    }
}
