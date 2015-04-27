package com.phuston.android.kyzr;

import com.getbase.floatingactionbutton.FloatingActionButton;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;



public class TorchFragment extends Fragment {

    private String mAndroid_id;
    private Toolbar mToolbar;

    private FloatingActionButton mFloatActButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.kyzr_title);
        mAndroid_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_torch, parent, false);

        mToolbar = (Toolbar)(v.findViewById(R.id.tool_bar));
        ((ActionBarActivity)getActivity()).setSupportActionBar(mToolbar);

        mFloatActButton = (FloatingActionButton)v.findViewById(R.id.float_act_button);

        mFloatActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), HelpActivity.class);
                startActivity(i);
            }
        });

        return v;
    }

    public void updateLocation(Location lastLocation) {
        //TODO: Implement this
    }

    public void addTorch(String ID) {
        //TODO: Implement adding torch
    }

    public String getTorchID() {
        return mAndroid_id;
    }

}
