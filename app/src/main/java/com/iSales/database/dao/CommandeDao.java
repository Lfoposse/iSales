package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.CommandeEntry;

import java.util.List;

/**
 * Created by netserve on 03/11/2018.
 */

@Dao
public interface CommandeDao {
    @Query("SELECT * FROM commande")
    LiveData<List<CommandeEntry>> loadAllCmde();

    @Query("SELECT * FROM commande ORDER BY date_commande ASC")
    List<CommandeEntry> getAllCmde();

    @Query("SELECT * FROM commande WHERE socid = :clientId ORDER BY date_commande ASC")
    List<CommandeEntry> getAllCmdeByClient(long clientId);

    @Query("SELECT * FROM commande WHERE date_commande >= :dateDebut AND date_commande <= :dateFin ORDER BY date_commande ASC")
    List<CommandeEntry> getAllCmdeOnPeriod(long dateDebut, long dateFin);

    @Query("SELECT * FROM commande WHERE date_commande >= :dateDebut AND date_commande <= :dateFin AND socid = :clientId ORDER BY date_commande ASC")
    List<CommandeEntry> getAllCmdeOnPeriodByClient(long dateDebut, long dateFin, long clientId);

    @Query("SELECT * FROM commande WHERE is_synchro = 0")
    List<CommandeEntry> getAllCmdeNotSynchro();

    @Query("SELECT * FROM commande WHERE socid = :clientId AND statut = :statut")
    List<CommandeEntry> getCmdeByClientOnStaut(long clientId, int statut);

    @Query("SELECT * FROM commande WHERE commande_id > :lastId GROUP BY commande_id LIMIT :limit")
    List<CommandeEntry> getCmdesLimit(long lastId, int limit);

    @Query("SELECT * FROM commande GROUP BY commande_id LIMIT :limit")
    List<CommandeEntry> getCmdesFirstLimit(int limit);

    @Query("SELECT * FROM commande WHERE id = :cmdeId")
    CommandeEntry getCmdesById(long cmdeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCmde(CommandeEntry commandeEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateCmde(CommandeEntry commandeEntry);

    @Query("UPDATE commande SET statut=:statut WHERE id = :cmdeId")
    void updateStatutCmde(long cmdeId, String statut);

    @Delete
    void deleteCmde(CommandeEntry commandeEntry);

    @Query("DELETE FROM commande")
    void deleteAllCmde();
}
