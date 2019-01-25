package com.rainbow_cl.i_sales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.rainbow_cl.i_sales.database.entry.CategorieEntry;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;

import java.util.List;

/**
 * Created by netserve on 01/11/2018.
 */

@Dao
public interface CategorieDao {
    @Query("SELECT * FROM categorie")
    LiveData<List<CategorieEntry>> loadAllCategorie();

    @Query("SELECT cat.id id, cat.fk_parent fk_parent, cat.label label, cat.description description, cat.color color, cat.type type, cat.ref ref, cat.ref_ext ref_ext, cat.visible visible, cat.entity entity, cat.poster_name poster_name, cat.poster_content poster_content, (SELECT COUNT(pdt.id) FROM produit pdt WHERE pdt.categorie_id = cat.id) count_produits FROM categorie cat")
    List<CategorieEntry> getAllCategories();

    @Query("SELECT * FROM (SELECT cat.id id, cat.fk_parent fk_parent, cat.label label, cat.description description, cat.color color, cat.type type, cat.ref ref, cat.ref_ext ref_ext, cat.visible visible, cat.entity entity, cat.poster_name poster_name, cat.poster_content poster_content, (SELECT COUNT(pdt.id) FROM produit pdt WHERE pdt.categorie_id = cat.id) count_produits FROM categorie cat) tab WHERE tab.count_produits > 0")
    List<CategorieEntry> getAllCategoriesAZero();

    @Query("SELECT * FROM categorie")
    List<CategorieEntry> getCategories();

    @Query("DELETE FROM categorie")
    void deleteAllCategorie();

    @Insert
    void insertCategorie(CategorieEntry categorieEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCategorie(CategorieEntry categorieEntry);

    @Delete
    void deleteCategorie(CategorieEntry categorieEntry);

    @Query("SELECT * FROM categorie WHERE id = :id")
    LiveData<CategorieEntry> loadCategorieById(long id);

    @Query("SELECT * FROM categorie WHERE id = :id")
    CategorieEntry getCategorieById(long id);
}
