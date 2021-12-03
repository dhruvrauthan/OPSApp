package com.example.opsapp.model;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;


@Entity
public class TrustedApplication {

    @NonNull
    @PrimaryKey
    public String clientID;

    @ColumnInfo(name = "privateKey")
    public String privateKey;

    @ColumnInfo(name = "publicKey")
    public String publicKey;

    @ColumnInfo(name = "balance")
    public int balance;

    @ColumnInfo(name = "i")
    public int i;

    @ColumnInfo(name = "j")
    public int j;
    //private String cert;

    public TrustedApplication(String clientID, String privateKey, String publicKey/*, String cert*/){
        this.clientID= clientID;
        this.privateKey= privateKey;
        this.publicKey= publicKey;
        //this.cert= cert;
        balance= 0;
        i=0;
        j=0;
    }

    @SuppressLint("NewApi")
    public PaymentPacket pay(TransactionPacket transactionPacket, Client sender, Client receiver) {
        //1. Abort if T.cert = ⊥ or T.bal < 𝑥;
        //we skip the cert step since the TA registration protocol has not been implemented yet

        //check if sender has enough balance
        int currentBalance= sender.getBalance();
        int amount= transactionPacket.getAmount();
        if(currentBalance<amount){
            return null;
        }

        //2. T.bal←T.bal−𝑥;
        sender.setBalance(currentBalance- amount);

        //3. T.j←T.j+1;

        //4. 𝑃.amount ← 𝑥; 𝑃.sender ← T.cert; 𝑃.receiver ← receiver; 𝑃.index ← T.j;
        try {
            Signature sign = Signature.getInstance("NONEwithRSA");

            //convert privatekey string to privatek key
            byte[] data = Base64.getDecoder().decode((privateKey.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            PrivateKey privateK= fact.generatePrivate(spec);

            sign.initSign(privateK);

            //create [P.amount, P.sender, P.receiver] array
            ArrayList<String> arrayList= new ArrayList<>();
            arrayList.add(String.valueOf(transactionPacket.getAmount()));
            arrayList.add(sender.getCertificate());
            arrayList.add(receiver.getCertificate());

            byte[] dataBytes= arrayList.toString().getBytes();
            dataBytes= java.util.Arrays.copyOf(dataBytes, 256/8);

            //sign arraylist with ta's private key T.sk
            sign.update(dataBytes);

            byte[] signature = sign.sign();

            //create payment packet P
            PaymentPacket P= new PaymentPacket(amount, sender.getCertificate(),
                    receiver.getCertificate(), signature.toString());

            //5. Output 𝑃, where 𝑃.sig←Sign([𝑃.amount,𝑃.sender,𝑃.receiver,𝑃.index],T.sk).
            //send P back to receiver
            return P;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }
}
