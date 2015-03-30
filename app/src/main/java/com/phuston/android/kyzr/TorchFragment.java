package com.phuston.android.kyzr;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class TorchFragment extends Fragment {
    private static final String TAG = "SleepOptionsFragment";
    private Button mSleepNowButton;
    private TextView mIDTextview;
    private TextView mRecievedIDs;
    private String android_id;

    protected TextView mLatitudeText;
    protected TextView mLongitudeText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.beamer_title);
        android_id = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_torch, parent, false);

        mIDTextview  = (TextView)v.findViewById(R.id.IDTextview);
        mIDTextview.setText(android_id);

        mRecievedIDs = (TextView)v.findViewById(R.id.RecievedIDsTextview);

        mLatitudeText = (TextView)v.findViewById(R.id.LatitudeTextview);
        mLongitudeText = (TextView)v.findViewById(R.id.LongitudeTextview);

        return v;
    }

    public void addTorch(String ID){
        mRecievedIDs.setText(mRecievedIDs.getText() + "\n" + ID);
    }

    public String getTorchID(){
        return android_id;
    }

    public void updateLocation(Location lastLocation){
        if (lastLocation != null) {
            mLatitudeText.setText(String.valueOf(lastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(lastLocation.getLongitude()));
        } else {
            Toast.makeText(getActivity(), R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
    }

    TorchActivity getContract() {
        return((TorchActivity)getActivity());
    }

}
