package com.rainbow_cl.i_sales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.rainbow_cl.i_sales.database.entry.ProduitEntry;

import java.util.List;

/**
 * Created by netserve on 21/09/2018.
 */

@Dao
public interface ProduitDao {
    @Query("SELECT * FROM produit")
    LiveData<List<ProduitEntry>> loadAllProduits();

    @Query("SELECT * FROM produit WHERE id > :lastId ORDER BY id LIMIT :limit")
    List<ProduitEntry> getProduitsLimit(long lastId, int limit);

    @Query("SELECT * FROM produit")
    List<ProduitEntry> getProduits();

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId ORDER BY id LIMIT :limit")
    List<ProduitEntry> getProduitsLimitAZero(long lastId, int limit);

    @Query("SELECT * FROM produit WHERE id > :lastId AND categorie_id = :categorieId GROUP BY id ORDER BY label LIMIT :limit")
    List<ProduitEntry> getProduitsLimitByCategorie(long lastId, long categorieId, int limit);

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId AND categorie_id = :categorieId GROUP BY id ORDER BY label LIMIT :limit")
    List<ProduitEntry> getProduitsLimitByCategorieAZero(long lastId, long categorieId, int limit);

    @Query("UPDATE produit SET file_content=:localPath WHERE id = :id")
    void updateLocalImgPath(long id, String localPath);

    @Insert
    void insertProduit(ProduitEntry produitEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateProduit(ProduitEntry produitEntry);

    @Delete
    void deleteProduit(ProduitEntry produitEntry);

    @Query("SELECT * FROM produit WHERE id = :id")
    LiveData<ProduitEntry> loadProduitById(long id);

    @Query("SELECT * FROM produit WHERE id = :id")
    ProduitEntry getProduitById(long id);

    @Query("DELETE FROM produit")
    void deleteAllProduit();

}
