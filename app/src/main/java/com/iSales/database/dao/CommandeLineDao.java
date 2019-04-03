package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.CommandeLineEntry;

import java.util.List;

/**
 * Created by netserve on 03/11/2018.
 */

@Dao
public interface CommandeLineDao {
    @Query("SELECT * FROM commande_line WHERE commande_ref = :cmdeRef")
    LiveData<List<CommandeLineEntry>> loadAllCmdeLine(long cmdeRef);

    @Query("SELECT * FROM commande_line WHERE commande_ref = :cmdeRef")
    List<CommandeLineEntry> getAllCmdeLineByCmdeRef(long cmdeRef);

    @Query("SELECT * FROM commande_line WHERE id = :cmdeLineId")
    CommandeLineEntry getCmdeLineById(long cmdeLineId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCmdeLine(CommandeLineEntry commandeLineEntry);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAllCmdeLine(List<CommandeLineEntry> commandeLineEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCmdeLine(CommandeLineEntry commandeLineEntry);

    @Delete
    void deleteCmdeLine(CommandeLineEntry commandeLineEntry);

    @Query("DELETE FROM commande_line")
    void deleteAllCmdeLine();
}
