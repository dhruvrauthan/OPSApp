package com.example.opsapp.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import androidx.room.Room;

import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.dao.ServerDao;
import com.example.opsapp.database.ClientDatabase;
import com.example.opsapp.database.ServerDatabase;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;

import javax.security.cert.CertificateEncodingException;

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
                .fallbackToDestructiveMigration()
                .build();
        mServerDatabase = Room.databaseBuilder(context,
                ServerDatabase.class, "server-database")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
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

    //2a.
    public boolean checkPublicKeyExists(String publicKey) {
        return mServerDao.publicKeyExists(publicKey);
    }

    //for base64 encoder
    @SuppressLint("NewApi")

    //2b. Add (vkA, ⊥) to S.Registry;
    public void registerClient(String userID, KeyPair keyPair, TextView mProgressTextView) {
        String publicKeyString = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKeyString = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        //2c. S.onBalA ← 0;
        Client client = new Client(userID, 100, publicKeyString, privateKeyString, "");
        RegisteredClient registeredClient= new RegisteredClient(userID, 100, publicKeyString);

        //add client to local database and server database. the registered client does not have the private key
        mClientDao.addClient(client);
        mServerDao.addClientToServerDatabase(registeredClient);

        mProgressTextView.append("2b. Add (vkA, ⊥) to S.Registry\n\n");
        mProgressTextView.append("2c. S.onBalA ← 0;\n\n");

        createClientCertificate(keyPair.getPublic(), client, mProgressTextView);
    }

    //for base64 encoder
    @SuppressLint("NewApi")

    //2d. Create certA such that certA.vk ← vkA and certA.sig ← Sign(vkA, skS);
    public void createClientCertificate(PublicKey publicKey, Client client, TextView mProgressTextView) {
        String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        try {
            //sign with server private key
            Signature sign = Signature.getInstance("NONEwithRSA");
            sign.initSign(mKeyPair.getPrivate());

            byte[] dataBytes= publicKeyString.getBytes();
            dataBytes= java.util.Arrays.copyOf(dataBytes, 256/8);

            //sign client's public key with server's private key
            sign.update(dataBytes);

            byte[] signature = sign.sign();

            //testing
            String encoded= Base64.getEncoder().encodeToString(signature.toString().getBytes());
            Log.d(TAG, "encoded: "+encoded);
            String decoded= new String(Base64.getDecoder().decode(encoded.getBytes()));
            Log.d(TAG, "decoded: "+decoded);
            Log.d(TAG, "original value: "+ signature);

            client.setCertificate(signature.toString());

            mClientDao.updateClient(client);

            mProgressTextView.append("2d. Create certA such that certA.vk ← vkA and certA.sig ← Sign(vkA, skS)\n\n");

            //2e. Send certA to A.
            mProgressTextView.append("2e. Send certA to A.\n\n");

            //Certificate cert = new Certificate(publicKeyString, signature.toString());
            //CertificateFactory certificateFactory= CertificateFactory.getInstance("X.509");
            //ByteArrayInputStream stream= new ByteArrayInputStream(signature);
            //Certificate cert= CertificateFactory.getInstance("X.509").generateCertificate(stream);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            mProgressTextView.append("An error occurred: "+ e.getMessage());
        }
    }

    public boolean checkClientIdExists(String userID) {
        return mServerDao.idExists(userID);
    }

    public Client getClient(String id){
        return mClientDao.getClient(id);
    }

}
