package com.example.opsapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.model.Client;
import com.example.opsapp.model.Converters;

@Database(entities = {Client.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class ClientDatabase extends RoomDatabase {

    public abstract ClientDao clientDao();

}
