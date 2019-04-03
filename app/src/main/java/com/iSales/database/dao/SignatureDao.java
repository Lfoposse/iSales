package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.iSales.database.entry.SignatureEntry;

import java.util.List;

/**
 * Created by netserve on 06/11/2018.
 */

@Dao
public interface SignatureDao {
    @Query("SELECT * FROM signature_cmde WHERE commande_ref = :cmdeRef")
    LiveData<List<com.iSales.database.entry.SignatureEntry>> loadAllSignatures(long cmdeRef);

    @Query("SELECT * FROM signature_cmde WHERE commande_ref = :cmdeRef")
    List<com.iSales.database.entry.SignatureEntry> getAllSignatureByCmdeRef(long cmdeRef);

    @Query("SELECT * FROM signature_cmde WHERE signature_id = :signatureId")
    com.iSales.database.entry.SignatureEntry getSignatureById(long signatureId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSignature(com.iSales.database.entry.SignatureEntry signatureEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllSignature(List<com.iSales.database.entry.SignatureEntry> signatureEntries);

    @Delete
    void deleteSignature(com.iSales.database.entry.SignatureEntry signatureEntry);

    @Query("DELETE FROM signature_cmde")
    void deleteAllSignature();
}
