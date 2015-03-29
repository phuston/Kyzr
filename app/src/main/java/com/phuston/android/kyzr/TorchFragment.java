package com.phuston.android.kyzr;


import android.app.Fragment;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class TorchFragment extends Fragment {
    private static final String TAG = "SleepOptionsFragment";
    private Button mSleepNowButton;
    private TextView mIDTextview;
    private TextView mRecievedIDs;
    private String android_id;

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

        return v;
    }

    public void addTorch(String ID){
        mRecievedIDs.setText(mRecievedIDs.getText() + "\n" + ID);
    }

    public String getTorchID(){
        return android_id;
    }

    TorchActivity getContract() {
        return((TorchActivity)getActivity());
    }

}
