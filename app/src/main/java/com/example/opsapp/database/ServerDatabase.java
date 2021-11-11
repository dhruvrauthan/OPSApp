package com.example.opsapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.opsapp.dao.ServerDao;
import com.example.opsapp.model.RegisteredClient;

@Database(entities = {RegisteredClient.class}, version = 1)
public abstract class ServerDatabase extends RoomDatabase {

    public abstract ServerDao serverDao();

}
