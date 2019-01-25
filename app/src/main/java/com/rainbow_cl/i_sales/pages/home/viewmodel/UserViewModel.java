package com.rainbow_cl.i_sales.pages.home.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.database.entry.UserEntry;
import com.rainbow_cl.i_sales.remote.model.User;

import java.util.List;

/**
 * Created by netserve on 08/10/2018.
 */

public class UserViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = UserViewModel.class.getSimpleName();

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
