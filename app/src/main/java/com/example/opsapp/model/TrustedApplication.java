package com.example.opsapp.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Room;

import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.database.ClientDatabase;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
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

    public TrustedApplication(String clientID, String privateKey, String publicKey/*, String cert*/, int balance){
        this.clientID= clientID;
        this.privateKey= privateKey;
        this.publicKey= publicKey;
        //this.cert= cert;
        this.balance= balance;
        i=0;
        j=0;
    }

    @SuppressLint("NewApi")
    public PaymentPacket pay(TransactionPacket transactionPacket, Client sender, Client receiver, TextView mProgressTextView, Context context) {
        ClientDatabase clientDatabase= Room.databaseBuilder(context,
                ClientDatabase.class, "client-database")
                .allowMainThreadQueries()
                .build();
        ClientDao clientDao= clientDatabase.clientDao();

        mProgressTextView.append("Invoking TA.Pay...\n\n");

        //1. Abort if T.cert = ⊥ or T.bal < 𝑥;
        //we skip the cert step since the TA registration protocol has not been implemented yet
        mProgressTextView.append("1. Abort if T.cert = ⊥ or T.bal < x;\n\n");

        //check if sender has enough balance
        int currentBalance= sender.getBalance();
        int amount= transactionPacket.getAmount();
        if(currentBalance<amount){
            return null;
        }

        //2. T.bal←T.bal−𝑥;
        balance= balance- amount;
        sender.setBalance(currentBalance- amount);
        clientDao.updateClient(sender);
        mProgressTextView.append("2. T.bal←T.bal−x;\n\n");

        //3. T.j←T.j+1;
        mProgressTextView.append("3. T.j←T.j+1;\n\n");

        //4. 𝑃.amount ← 𝑥; 𝑃.sender ← T.cert; 𝑃.receiver ← receiver; 𝑃.index ← T.j;
        try {
            Signature sign = Signature.getInstance("NONEwithRSA");

            //convert privatekey string to privatek key
            byte[] data = Base64.getDecoder().decode((privateKey.getBytes()));
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            PrivateKey privateK= fact.generatePrivate(spec);

            Log.d("==TrustedApplication", "privatekey:"+ privateKey);

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

            byte[] signatureBytes = sign.sign();

            //create payment packet P
            PaymentPacket P= new PaymentPacket(amount, sender.getCertificate(),
                    receiver.getCertificate(), /*Base64.getEncoder().encodeToString(signatureBytes.toString().getBytes()),*/
                    signatureBytes,
                    sign);
            mProgressTextView.append("4. P.amount ← x; P.sender ← T.cert; P.receiver ← receiver; P.index ← T.j;\n\n");

            //5. Output 𝑃, where 𝑃.sig←Sign([𝑃.amount,𝑃.sender,𝑃.receiver,𝑃.index],T.sk).
            mProgressTextView.append("5. Output P, where P.sig←Sign([P.amount,P.sender,P.receiver,P.index],T.sk).\n\n");

            //send P back to receiver
            return P;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException e) {
            e.printStackTrace();
            Log.d("==TrustedApplication", "error: "+e.getMessage());
        }

        return null;
    }
}
