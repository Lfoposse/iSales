package com.iSales.pages.home.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.PanierEntry;

/**
 * Created by netserve on 24/09/2018.
 */

public class CategorieViewModel extends ViewModel {
    private LiveData<PanierEntry> panierEntry;

    // Note: The constructor should receive the database and the taskId
    public CategorieViewModel(AppDatabase database, long panierId) {
        panierEntry = database.panierDao().loadPanierById(panierId);
    }

    // TODO (7) Create a getter for the task variable
    public LiveData<PanierEntry> getPanierEntry() {
        return panierEntry;
    }
}
