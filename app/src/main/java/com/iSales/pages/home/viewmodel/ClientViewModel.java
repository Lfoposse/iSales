package com.iSales.pages.home.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.pages.home.viewmodel.PanierViewModel;

import java.util.List;

/**
 * Created by netserve on 31/10/2018.
 */

public class ClientViewModel extends AndroidViewModel {
    // Constant for logging
    private static final String TAG = PanierViewModel.class.getSimpleName();

    private LiveData<List<ClientEntry>> clientEntries;

    public ClientViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
//        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        clientEntries = database.clientDao().loadAllClient();
    }

    public LiveData<List<ClientEntry>> getClientLimit(long lastId, int limit) {
        return clientEntries;
    }
}
