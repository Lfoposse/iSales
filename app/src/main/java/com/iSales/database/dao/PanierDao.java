package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.PanierEntry;

import java.util.List;

/**
 * Created by netserve on 24/09/2018.
 */

@Dao
public interface PanierDao {
    @Query("SELECT * FROM panier ORDER BY panier_id")
    LiveData<List<PanierEntry>> loadAllPanier();

    @Query("SELECT * FROM panier ORDER BY panier_id")
    List<PanierEntry> getAllPanier();

    @Query("DELETE FROM panier")
    void deleteAllPanier();

    @Insert
    void insertPanier(PanierEntry panierEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updatePanier(PanierEntry panierEntry);

    @Query("UPDATE panier SET quantity=:quantite WHERE id = :id")
    void updateQuantite(long id, int quantite);

    @Delete
    void deletePanier(PanierEntry panierEntry);

    @Query("SELECT * FROM panier WHERE id = :id")
    LiveData<PanierEntry> loadPanierById(long id);

    @Query("SELECT * FROM panier WHERE id = :id")
    PanierEntry getPanierById(long id);
}
