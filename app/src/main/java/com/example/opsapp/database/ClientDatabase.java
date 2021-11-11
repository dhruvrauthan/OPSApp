package com.example.opsapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.model.Client;

@Database(entities = {Client.class}, version = 1)
public abstract class ClientDatabase extends RoomDatabase {

    public abstract ClientDao clientDao();

}
