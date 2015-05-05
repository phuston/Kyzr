package com.phuston.android.kyzr;

import com.getbase.floatingactionbutton.FloatingActionButton;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;


public class TorchFragment extends Fragment {

    private String mAndroid_id;
    protected Toolbar mToolbar;

    protected FloatingActionButton mFloatActButton;
    protected TextView mCurrTorchTextview;

    protected ImageView mTorchImage;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.kyzr_title);
        mAndroid_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_torch, parent, false);

        mCurrTorchTextview = (TextView)v.findViewById(R.id.currtorch_textview);

        mTorchImage = (ImageView)v.findViewById(R.id.torchimageview);

        mTorchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TorchActivity)getActivity()).getCurrTorch();
            }
        });

        mToolbar = (Toolbar)(v.findViewById(R.id.tool_bar));
        ((ActionBarActivity)getActivity()).setSupportActionBar(mToolbar);

        mFloatActButton = (FloatingActionButton)v.findViewById(R.id.float_act_button);

        mFloatActButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), StatsActivity.class);
                startActivity(i);
            }
        });

        return v;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_help, menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_settings:
                try {
                    Intent activityStarter = new Intent(getActivity().getApplicationContext(), Class.forName("com.phuston.android.kyzr.HelpActivity"));
                    startActivity(activityStarter);
                } catch(ClassNotFoundException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Could not find activity to start.", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }

    }

    public void updateCurrTorch(String currTorchID){
        mCurrTorchTextview.setText(currTorchID);
    }

    public String getTorchID() {
        return mAndroid_id;
    }
}
