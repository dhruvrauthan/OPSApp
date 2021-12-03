package com.example.opsapp.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

import com.example.opsapp.model.TrustedApplication;

@Dao
public interface TADao {

    @Insert
    void addTA(TrustedApplication TA);

    @Update
    void updateTA(TrustedApplication TA);

}
