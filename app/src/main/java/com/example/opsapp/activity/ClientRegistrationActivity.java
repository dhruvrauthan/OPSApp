package com.example.opsapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opsapp.R;
import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.database.ClientDatabase;
import com.example.opsapp.model.RegistrationPacket;
import com.example.opsapp.model.Server;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ClientRegistrationActivity extends AppCompatActivity {

    private static final String TAG= "==ClientRegActivity";

    //ui
    private Button mRegisterClientButton;
    private EditText mUserIDEditText;
    private ProgressBar mRegistrationProgressBar;
    private TextView mProgressTextView;

    private KeyPair mKeyPair;
    private Server mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_registration);

        mServer= Server.getInstance(getApplicationContext());

        initUI();
    }

    private void initUI() {
        mRegisterClientButton= findViewById(R.id.button_client_register);
        mUserIDEditText= findViewById(R.id.edittext_user_id);
        mRegistrationProgressBar= findViewById(R.id.progress_registration);
        mProgressTextView= findViewById(R.id.textview_registration_status);

        mRegisterClientButton.setOnClickListener(v->{
            registerClient();
        });
    }

    //for base64 encoder
    @SuppressLint("NewApi")
    private void registerClient() {
        String userID= mUserIDEditText.getText().toString();

        if(userID.isEmpty()){
            showToast("Please enter a value");
        } else{
            //check if id exists locally
            if(mServer.checkClientIdExists(userID)){
                showToast("This id already exists");
                return;
            }

            //generate keys
            KeyPairGenerator keyGen= null;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                mKeyPair= keyGen.generateKeyPair();

                //send public key to server
                String publicKeyString = Base64.getEncoder().
                        encodeToString(mKeyPair.getPublic().getEncoded());

                //1. A sends [RegisterClient, vk ] to S
                RegistrationPacket registrationPacket= new RegistrationPacket(publicKeyString);
                mProgressTextView.setText("");
                mProgressTextView.append("1. A sends [RegisterClient, vk ] to S\n\n");

                //2a. Abort if (vkA, ·) ∈ S.Registry;
                if(mServer.checkPublicKeyExists(publicKeyString)){
                    showToast("This public key already exists");
                    mProgressTextView.append("2a. Abort if (vkA, ·) ∈ S.Registry\n\n");
                    return;
                }

                mProgressTextView.append("2a. (vkA, ·) ∉ S.Registry\n\n");
                //2b.
                mServer.registerClient(userID, mKeyPair, mProgressTextView);
                showToast("Client registered");

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}