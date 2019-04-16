package com.iSales.database.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.iSales.database.entry.ClientEntry;

import java.util.List;

/**
 * Created by netserve on 30/10/2018.
 */

@Dao
public interface ClientDao {
    @Query("SELECT * FROM client")
    LiveData<List<ClientEntry>> loadAllClient();

//    @Query("SELECT * FROM client ORDER BY name")
    @Query("SELECT  DISTINCT id, _rowid_ as oid, is_current, note_public, note_private, note, is_synchro, code_client, logo_content, logo, client_id, name, name_alias, firstname, lastname, address, email, phone, region, departement, pays, date_modification, date_creation, town FROM client ORDER BY name")
    List<ClientEntry> getAllClient();

    @Query("SELECT * FROM client WHERE is_synchro = :synchro")
    List<ClientEntry> getAllClientBySynchro(int synchro);

//    @Query("SELECT rowid,  FROM client WHERE id > :lastId ORDER BY name LIMIT :limit")
//    List<ClientEntry> getClientsLimit(long lastId, int limit);

    @Query("SELECT  DISTINCT id, _rowid_ as oid, is_current, note_public, note_private, note, is_synchro, code_client, logo_content, logo, client_id, name, name_alias, firstname, lastname, address, email, phone, region, departement, pays, date_modification, date_creation, town FROM client WHERE id > :lastId ORDER BY name LIMIT :limit")
    List<ClientEntry> getClientsLimit(long lastId, int limit);

    @Query("SELECT * FROM client WHERE is_current = :current")
    ClientEntry getCurrentClient(int current);

    @Query("SELECT DISTINCT id, _rowid_ as oid, is_current, note_public, note_private, note, is_synchro, code_client, logo_content, logo, client_id, name, name_alias, firstname, lastname, address, email, phone, region, departement, pays, date_modification, date_creation, town FROM client WHERE LOWER(address) LIKE '%'||:keyword||'%' OR LOWER(name) LIKE '%'||:keyword||'%' GROUP BY id ORDER BY name LIMIT :limit")
    List<ClientEntry> getClientsLikeLimit(int limit, String keyword);

    @Insert
    Long insertClient(ClientEntry clientEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateClient(ClientEntry clientEntry);

//    Mise a jour du logo d'un client
    @Query("UPDATE client SET logo_content = :logoBytes WHERE id = :clientId")
    void updateLogo_content(String logoBytes, long clientId);

//    Mise a jour du logo d'un client
    @Query("UPDATE client SET logo = :logoPath WHERE id = :clientId")
    void updateLogo(String logoPath, long clientId);

//    Mise a jour du client courant. 1 si client courant, 0 sinon
    @Query("UPDATE client SET is_current = :current WHERE id = :clientId")
    void updateCurrentClient(int current, long clientId);

//    Mise a jour du client courant. 1 si client courant, 0 sinon
    @Query("UPDATE client SET is_synchro = :synchro WHERE id = :clientId")
    void updateSynchroClient(int synchro, long clientId);

//    Mise a jour du client courant. 1 si client courant, 0 sinon
    @Query("UPDATE client SET id = :idThirdpartie WHERE name = :name AND email = :email")
    void updateIdClient(long idThirdpartie, String name, String email);

//    Mise a jour du client courant
    @Query("UPDATE client SET is_current = 0")
    void updateAllCurrentClient();

    @Query("SELECT * FROM client WHERE id = :id")
    LiveData<ClientEntry> loadClientById(long id);

    @Query("SELECT * FROM client WHERE id = :id")
    ClientEntry getClientById(long id);

    @Query("SELECT * FROM client WHERE client_id = :id")
    ClientEntry getClientByClientId(long id);

    @Delete
    void deleteClient(ClientEntry clientEntry);

    @Query("DELETE FROM client")
    void deleteAllClient();

    @Query("DELETE FROM client WHERE id = :id")
    void deleteClientById(long id);

    @Query("DELETE FROM client WHERE client_id = :id")
    void deleteClientByClientId(long id);
}
