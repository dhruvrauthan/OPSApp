package com.example.opsapp.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.security.KeyPair;
import java.security.PrivateKey;

@Entity
public class Client {

    @NonNull
    @PrimaryKey
    public String id;

    @ColumnInfo(name = "balance")
    public int balance;

    @ColumnInfo(name = "privateKey")
    public String privateKey;

    @ColumnInfo(name = "publicKey")
    public String publicKey;

    public Client(String id, int balance, String publicKey, String privateKey) {
        this.id = id;
        this.balance = balance;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}
