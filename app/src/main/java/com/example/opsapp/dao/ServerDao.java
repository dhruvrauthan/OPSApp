package com.example.opsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.opsapp.model.RegisteredClient;

@Dao
public interface ServerDao {

    @Insert
    void addClientToServerDatabase(RegisteredClient registeredClient);

    @Query("SELECT EXISTS (SELECT 1 FROM RegisteredClient where id=:id)")
    boolean idExists(String id);

    @Query("SELECT EXISTS (SELECT 1 FROM RegisteredClient where publicKey=:publicKey)")
    boolean publicKeyExists(String publicKey);

}
