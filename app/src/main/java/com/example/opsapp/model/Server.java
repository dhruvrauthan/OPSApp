package com.example.opsapp.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.room.Room;

import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.dao.ServerDao;
import com.example.opsapp.database.ClientDatabase;
import com.example.opsapp.database.ServerDatabase;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.BitSet;

public class Server {

    private static Server single_instance = null;
    private static final String TAG = "==Server";

    private ClientDatabase mClientDatabase;
    private ServerDatabase mServerDatabase;
    private ClientDao mClientDao;
    private ServerDao mServerDao;
    private KeyPair mKeyPair;

    public Server(Context context) {
        mClientDatabase = Room.databaseBuilder(context,
                ClientDatabase.class, "client-database")
                .allowMainThreadQueries()
                .build();
        mServerDatabase = Room.databaseBuilder(context,
                ServerDatabase.class, "server-database")
                .allowMainThreadQueries()
                .build();

        mClientDao = mClientDatabase.clientDao();
        mServerDao= mServerDatabase.serverDao();

        //server keys
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            mKeyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //singleton class
    public static Server getInstance(Context context) {
        if (single_instance == null) {
            single_instance = new Server(context);
        }

        return single_instance;
    }

    public boolean checkClientIdExists(String userID) {
        return mServerDao.idExists(userID);
    }

    //abort if (vk,.) belongs to S.Registry
    public boolean checkPublicKeyExists(String publicKey) {
        return mServerDao.publicKeyExists(publicKey);
    }

    //for base64 encoder
    @SuppressLint("NewApi")
    //add (vk,) to S.Registry
    public void registerClient(String userID, KeyPair keyPair) {
        String publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyString = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        //S.onBal(A) -> 0
        Client client = new Client(userID, 0, publicKeyString, privateKeyString);
        RegisteredClient registeredClient= new RegisteredClient(userID, 0, publicKeyString);

        //add client to local database and server database. the registered client does not have the private key
        mClientDao.addClient(client);
        mServerDao.addClientToServerDatabase(registeredClient);

        createClientCertificate(publicKeyString);
    }

    //create cert, such that cert.vk->vk(A) and cert.sign->Sign(vk(A), sk(S))
    public void createClientCertificate(String publicKeyString) {
        try {
            //sign with server private key
            Signature sign = Signature.getInstance("NONEwithRSA");
            sign.initSign(mKeyPair.getPrivate());

            byte[] dataBytes= publicKeyString.getBytes();
            dataBytes= java.util.Arrays.copyOf(dataBytes, 256/8);

            //sign client's public key with server's private key
            sign.update(dataBytes);

            byte[] signature = sign.sign();

            Certificate cert = new Certificate(publicKeyString, signature.toString());

            //send cert back to client here

            //Log.i(TAG, cert.toString());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
    }

}
