package com.iSales.pages.home.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.PanierEntry;

import java.util.List;

/**
 * Created by netserve on 24/09/2018.
 */

public class PanierViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = PanierViewModel.class.getSimpleName();

    private LiveData<List<com.iSales.database.entry.PanierEntry>> panierEntries;

    public PanierViewModel(Application application) {
        super(application);
        com.iSales.database.AppDatabase database = AppDatabase.getInstance(this.getApplication());
//        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        panierEntries = database.panierDao().loadAllPanier();
    }

    public LiveData<List<PanierEntry>> getAllPanierEntries() {
        return panierEntries;
    }
}
