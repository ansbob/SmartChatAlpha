package com.example.admin.smartchatalphav1.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.admin.smartchatalphav1.R;

public class Settings extends Fragment {

    final String TAG = "SettingsFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.settings_fragment, null);
        Log.i(TAG, "OnCreateView");


        return v;
    }
}
