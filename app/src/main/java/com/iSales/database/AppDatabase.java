package com.iSales.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.iSales.database.dao.CategorieDao;
import com.iSales.database.dao.ClientDao;
import com.iSales.database.dao.CommandeDao;
import com.iSales.database.dao.CommandeLineDao;
import com.iSales.database.dao.PanierDao;
import com.iSales.database.dao.PaymentTypesDao;
import com.iSales.database.dao.ProductCustPriceDao;
import com.iSales.database.dao.ProduitDao;
import com.iSales.database.dao.ServerDao;
import com.iSales.database.dao.SignatureDao;
import com.iSales.database.dao.TokenDao;
import com.iSales.database.dao.UserDao;
import com.iSales.database.entry.CategorieEntry;
import com.iSales.database.entry.ClientEntry;
import com.iSales.database.entry.CommandeEntry;
import com.iSales.database.entry.CommandeLineEntry;
import com.iSales.database.entry.PanierEntry;
import com.iSales.database.entry.PaymentTypesEntry;
import com.iSales.database.entry.ProductCustPriceEntry;
import com.iSales.database.entry.ProduitEntry;
import com.iSales.database.entry.ServerEntry;
import com.iSales.database.entry.SignatureEntry;
import com.iSales.database.entry.TokenEntry;
import com.iSales.database.entry.UserEntry;

/**
 * Created by netserve on 21/09/2018.
 */

@Database(entities = {ProduitEntry.class, ClientEntry.class, CategorieEntry.class, PanierEntry.class, TokenEntry.class, UserEntry.class, CommandeEntry.class, CommandeLineEntry.class, SignatureEntry.class, ServerEntry.class, PaymentTypesEntry.class, ProductCustPriceEntry.class},
        version = 13,
        exportSchema = false)
//@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = com.iSales.database.AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "isales_store";
    private static com.iSales.database.AppDatabase sInstance;

    public static com.iSales.database.AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
//                Log.e(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        com.iSales.database.AppDatabase.class, com.iSales.database.AppDatabase.DATABASE_NAME)
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
