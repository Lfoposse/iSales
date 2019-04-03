package com.iSales.database.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.PaymentTypesEntry;

import java.util.List;

/**
 * Created by netserve on 12/02/2019.
 */

@Dao
public interface PaymentTypesDao {

    @Query("SELECT * FROM payment_types ORDER BY label")
    List<PaymentTypesEntry> getAllPayments();

    @Query("SELECT * FROM payment_types WHERE id = :paymentId")
    PaymentTypesEntry getPaymentById(Long paymentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPayment(PaymentTypesEntry paymentTypesEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllPayments(List<PaymentTypesEntry> paymentTypesEntries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCmdeLine(PaymentTypesEntry paymentTypesEntry);

    @Delete
    void deletePayment(PaymentTypesEntry paymentTypesEntry);

    @Query("DELETE FROM payment_types")
    void deleteAllPayment();
}
