package com.rainbow_cl.i_sales.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.rainbow_cl.i_sales.database.dao.CategorieDao;
import com.rainbow_cl.i_sales.database.dao.ClientDao;
import com.rainbow_cl.i_sales.database.dao.CommandeDao;
import com.rainbow_cl.i_sales.database.dao.CommandeLineDao;
import com.rainbow_cl.i_sales.database.dao.PanierDao;
import com.rainbow_cl.i_sales.database.dao.PaymentTypesDao;
import com.rainbow_cl.i_sales.database.dao.ProductCustPriceDao;
import com.rainbow_cl.i_sales.database.dao.ProduitDao;
import com.rainbow_cl.i_sales.database.dao.ServerDao;
import com.rainbow_cl.i_sales.database.dao.SignatureDao;
import com.rainbow_cl.i_sales.database.dao.TokenDao;
import com.rainbow_cl.i_sales.database.dao.UserDao;
import com.rainbow_cl.i_sales.database.entry.CategorieEntry;
import com.rainbow_cl.i_sales.database.entry.ClientEntry;
import com.rainbow_cl.i_sales.database.entry.CommandeEntry;
import com.rainbow_cl.i_sales.database.entry.CommandeLineEntry;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.database.entry.PaymentTypesEntry;
import com.rainbow_cl.i_sales.database.entry.ProductCustPriceEntry;
import com.rainbow_cl.i_sales.database.entry.ProduitEntry;
import com.rainbow_cl.i_sales.database.entry.ServerEntry;
import com.rainbow_cl.i_sales.database.entry.SignatureEntry;
import com.rainbow_cl.i_sales.database.entry.TokenEntry;
import com.rainbow_cl.i_sales.database.entry.UserEntry;
import com.rainbow_cl.i_sales.remote.model.PaymentTypes;

/**
 * Created by netserve on 21/09/2018.
 */

@Database(entities = {ProduitEntry.class, ClientEntry.class, CategorieEntry.class, PanierEntry.class, TokenEntry.class, UserEntry.class, CommandeEntry.class, CommandeLineEntry.class, SignatureEntry.class, ServerEntry.class, PaymentTypesEntry.class, ProductCustPriceEntry.class},
        version = 10,
        exportSchema = false)
//@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "isales_store";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
//                Log.e(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .allowMainThreadQueries() // autorise Room a effectuer les requetes dans le main UI thread
                        .fallbackToDestructiveMigration() // regnere les table apres une incrementation de version
                        .build();
            }
        }
//        Log.e(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    //    client DAO
    public abstract ClientDao clientDao();

    //    produit DAO
    public abstract ProduitDao produitDao();

    //    produit DAO
    public abstract CategorieDao categorieDao();

    //    panier DAO
    public abstract PanierDao panierDao();

    //    token DAO
    public abstract TokenDao tokenDao();

    //    user DAO
    public abstract UserDao userDao();

    //    comande DAO
    public abstract CommandeDao commandeDao();

    //    comande line DAO
    public abstract CommandeLineDao commandeLineDao();

    //    signature DAO
    public abstract SignatureDao signatureDao();

    //    signature DAO
    public abstract ServerDao serverDao();

    //    PaymentTypes DAO
    public abstract PaymentTypesDao paymentTypesDao();

    //    ProductCustomerPrice DAO
    public abstract ProductCustPriceDao productCustPriceDao();

}
