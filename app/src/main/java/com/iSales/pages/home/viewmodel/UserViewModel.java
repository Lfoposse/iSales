package com.iSales.pages.home.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.UserEntry;

import java.util.List;

/**
 * Created by netserve on 08/10/2018.
 */

public class UserViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = com.iSales.pages.home.viewmodel.UserViewModel.class.getSimpleName();

    private LiveData<List<UserEntry>> userEntries;

    public UserViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
//        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        userEntries = database.userDao().loadUser();
    }

    public LiveData<List<UserEntry>> getUserEntry() {
        return userEntries;
    }
}
