package com.example.opsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.opsapp.model.Client;
import com.example.opsapp.model.RegisteredClient;

@Dao
public interface ClientDao {

    @Insert
    void addClient(Client client);

    @Update
    void updateClient(Client client);

    @Query("SELECT * from Client where id= :id")
    Client getClient(String id);

}
