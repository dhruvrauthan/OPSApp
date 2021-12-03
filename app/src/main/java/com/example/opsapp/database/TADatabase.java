package com.example.opsapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.opsapp.dao.TADao;
import com.example.opsapp.model.TrustedApplication;

@Database(entities = {TrustedApplication.class}, version =1)
public abstract class TADatabase extends RoomDatabase {

    public abstract TADao taDao();

}
