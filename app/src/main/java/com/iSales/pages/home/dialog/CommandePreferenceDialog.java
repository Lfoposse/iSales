package com.iSales.pages.home.dialog;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.iSales.R;

/**
 * Created by netserve on 06/12/2018.
 */

public class CommandePreferenceDialog extends PreferenceFragmentCompat {
    private static final String TAG = com.iSales.pages.home.dialog.CommandePreferenceDialog.class.getSimpleName();

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
