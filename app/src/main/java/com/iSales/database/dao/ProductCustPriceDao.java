package com.iSales.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.iSales.database.entry.ProductCustPriceEntry;

import java.util.List;

@Dao
public interface ProductCustPriceDao {
    @Query("SELECT * FROM produit_cust_price WHERE fk_product = :productId")
    List<ProductCustPriceEntry> loadAllCmdeLine(long productId);

    @Query("SELECT * FROM produit_cust_price")
    List<ProductCustPriceEntry> getAllProductCustPrice();

    @Query("SELECT * FROM produit_cust_price where fk_product = :idProduit")
    ProductCustPriceEntry getProductCustPriceByPrdt(long idProduit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducCustPrice(ProductCustPriceEntry productCustPriceEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllProducCustPrice(List<ProductCustPriceEntry> productCustPriceEntries);

    @Delete
    void deleteProductCustPrice(ProductCustPriceEntry productCustPriceEntry);

    @Query("DELETE FROM produit_cust_price")
    void deleteAllProductCustPrice();
}
