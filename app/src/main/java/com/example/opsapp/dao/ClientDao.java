package com.example.opsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.opsapp.model.Client;

@Dao
public interface ClientDao {

    @Insert
    void addClient(Client client);

    @Query("SELECT EXISTS (SELECT 1 FROM Client where id=:id)")
    boolean idExists(String id);

    @Query("SELECT EXISTS (SELECT 1 FROM Client where publicKey=:publicKey)")
    boolean publicKeyExists(String publicKey);

}
