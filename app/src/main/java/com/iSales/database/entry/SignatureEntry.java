package com.iSales.database.entry;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by netserve on 06/11/2018.
 */

@Entity(tableName = "signature_cmde",
        foreignKeys = {
                @ForeignKey(entity = CommandeEntry.class, parentColumns = "commande_id", childColumns = "commande_ref", onDelete = ForeignKey.CASCADE)})
public class SignatureEntry {
    @PrimaryKey(autoGenerate = true)
    private Long signature_id;
    private Long commande_ref;
    private String name;
    private String content;
    private String type_signature;

    public SignatureEntry() {
    }

    @Ignore
    public SignatureEntry(Long commande_ref, String name, String content, String type_signature) {
        this.commande_ref = commande_ref;
        this.name = name;
        this.content = content;
        this.type_signature = type_signature;
    }

    public Long getSignature_id() {
        return signature_id;
    }

    public void setSignature_id(Long signature_id) {
        this.signature_id = signature_id;
    }

    public Long getCommande_ref() {
        return commande_ref;
    }

    public void setCommande_ref(Long commande_ref) {
        this.commande_ref = commande_ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType_signature() {
        return type_signature;
    }

    public void setType_signature(String type_signature) {
        this.type_signature = type_signature;
    }
}
