package com.example.opsapp.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RegisteredClient {

    @NonNull
    @PrimaryKey
    public String id;

    @ColumnInfo(name = "balance")
    public int balance;

    @ColumnInfo(name = "publicKey")
    public String publicKey;

    public RegisteredClient(String id, int balance, String publicKey) {
        this.id = id;
        this.balance = balance;
        this.publicKey = publicKey;
    }

}
