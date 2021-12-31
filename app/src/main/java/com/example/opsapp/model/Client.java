package com.example.opsapp.model;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.ArrayList;

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

    @ColumnInfo(name= "inPaymentLog")
    public ArrayList<PaymentPacket> inPaymentLog;

    public Client(String id, int balance, String publicKey, String privateKey, String certificate) {
        this.id = id;
        this.balance = balance;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.certificate= certificate;
        this.inPaymentLog= new ArrayList<>();
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

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public ArrayList<PaymentPacket> getInPaymentLog() {
        return inPaymentLog;
    }

    public void setInPaymentLog(ArrayList<PaymentPacket> inPaymentLog) {
        this.inPaymentLog = inPaymentLog;
    }
}
