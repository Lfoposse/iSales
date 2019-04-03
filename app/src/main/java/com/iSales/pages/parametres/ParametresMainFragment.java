package com.iSales.pages.parametres;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.iSales.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ParametresMainFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = com.iSales.pages.parametres.ParametresMainFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.preference_parametres_main);


    }
    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged: ");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Log.e(TAG, "onSharedPreferenceChanged: ");
    }
}
