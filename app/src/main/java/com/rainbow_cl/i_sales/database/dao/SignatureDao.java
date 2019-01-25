package com.rainbow_cl.i_sales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.rainbow_cl.i_sales.database.entry.CommandeLineEntry;
import com.rainbow_cl.i_sales.database.entry.SignatureEntry;

import java.util.List;

/**
 * Created by netserve on 06/11/2018.
 */

@Dao
public interface SignatureDao {
    @Query("SELECT * FROM signature_cmde WHERE commande_ref = :cmdeRef")
    LiveData<List<SignatureEntry>> loadAllSignatures(long cmdeRef);

    @Query("SELECT * FROM signature_cmde WHERE commande_ref = :cmdeRef")
    List<SignatureEntry> getAllSignatureByCmdeRef(long cmdeRef);

    @Query("SELECT * FROM signature_cmde WHERE signature_id = :signatureId")
    SignatureEntry getSignatureById(long signatureId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSignature(SignatureEntry signatureEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllSignature(List<SignatureEntry> signatureEntries);

    @Delete
    void deleteSignature(SignatureEntry signatureEntry);

    @Query("DELETE FROM signature_cmde")
    void deleteAllSignature();
}
