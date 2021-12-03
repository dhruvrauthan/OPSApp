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

    @ColumnInfo(name = "certificate")
    public String certificate;

    public Client(String id, int balance, String publicKey, String privateKey, String certificate) {
        this.id = id;
        this.balance = balance;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate= certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCertificate(){
        return this.certificate;
    }

    public int getBalance(){
        return this.balance;
    }

    public void setBalance(int balance){
        this.balance= balance;
    }

    public String getId(){
        return this.id;
    }
}
