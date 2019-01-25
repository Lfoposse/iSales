package com.rainbow_cl.i_sales.pages.home.dialog;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.rainbow_cl.i_sales.R;

import java.util.Locale;

/**
 * Created by netserve on 06/12/2018.
 */

public class CommandePreferenceDialog extends PreferenceFragmentCompat {
    private static final String TAG = CommandePreferenceDialog.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.commande_preference);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged: ");
    }

}
