package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.ProduitEntry;

import java.util.List;

/**
 * Created by netserve on 21/09/2018.
 */

@Dao
public interface ProduitDao {
    @Query("SELECT * FROM produit")
    LiveData<List<com.iSales.database.entry.ProduitEntry>> loadAllProduits();

    @Query("SELECT * FROM produit WHERE id > :lastId ORDER BY id LIMIT :limit")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimit(long lastId, int limit);

    @Query("SELECT * FROM produit WHERE id > :lastId AND (LOWER(label) LIKE '%'||:keyword||'%' OR LOWER(ref) LIKE '%'||:keyword||'%') ORDER BY id LIMIT :limit")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByStr(long lastId, int limit, String keyword);

    @Query("SELECT * FROM produit ORDER BY label")
    List<com.iSales.database.entry.ProduitEntry> getAllProduits();

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId ORDER BY id LIMIT :limit")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitAZero(long lastId, int limit);

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId AND (LOWER(label) LIKE '%'||:keyword||'%' OR LOWER(ref) LIKE '%'||:keyword||'%') ORDER BY id LIMIT :limit")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByStrAZero(long lastId, int limit, String keyword);

//    @Query("SELECT * FROM produit WHERE id > :lastId AND categorie_id = :categorieId ORDER BY label LIMIT :limit")
    @Query("SELECT * FROM produit WHERE id > :lastId AND categorie_id = :categorieId GROUP BY id ORDER BY label")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByCategorie(long lastId, long categorieId);

    @Query("SELECT * FROM produit WHERE id > :lastId AND categorie_id = :categorieId AND (LOWER(label) LIKE '%'||:keyword||'%' OR LOWER(ref) LIKE '%'||:keyword||'%') GROUP BY id ORDER BY label")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByCategorieStr(long lastId, long categorieId, String keyword);

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId AND categorie_id = :categorieId GROUP BY id ORDER BY label")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByCategorieAZero(long lastId, long categorieId);

    @Query("SELECT * FROM produit WHERE stock_reel > 0 AND id > :lastId AND categorie_id = :categorieId AND (LOWER(label) LIKE '%'||:keyword||'%' OR LOWER(ref) LIKE '%'||:keyword||'%') GROUP BY id ORDER BY label")
    List<com.iSales.database.entry.ProduitEntry> getProduitsLimitByCategorieStrAZero(long lastId, long categorieId, String keyword);

    @Query("UPDATE produit SET file_content=:localPath WHERE id = :id")
    void updateLocalImgPath(long id, String localPath);

    @Insert
    void insertProduit(com.iSales.database.entry.ProduitEntry produitEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateProduit(com.iSales.database.entry.ProduitEntry produitEntry);

    @Delete
    void deleteProduit(com.iSales.database.entry.ProduitEntry produitEntry);

    @Query("SELECT * FROM produit WHERE id = :id")
    LiveData<com.iSales.database.entry.ProduitEntry> loadProduitById(long id);

    @Query("SELECT * FROM produit WHERE id = :id")
    ProduitEntry getProduitById(long id);

    @Query("DELETE FROM produit")
    void deleteAllProduit();

}
