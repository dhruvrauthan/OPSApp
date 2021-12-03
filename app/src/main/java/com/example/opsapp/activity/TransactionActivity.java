package com.example.opsapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.opsapp.R;
import com.example.opsapp.model.Client;
import com.example.opsapp.model.PaymentPacket;
import com.example.opsapp.model.Server;
import com.example.opsapp.model.TransactionPacket;
import com.example.opsapp.model.TrustedApplication;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class TransactionActivity extends AppCompatActivity {

    private Button mSendButton, mReceiveButton;
    private EditText mAmountEditText, mSenderEditText, mReceiverEditText;

    private Server mServer;
    private Client mSender, mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        mServer = Server.getInstance(getApplicationContext());

        initUI();
    }

    private void initUI() {
        mSendButton = findViewById(R.id.button_send);
        mReceiveButton = findViewById(R.id.button_receive);

        mReceiveButton.setOnClickListener(v -> {
            requestPayment();
        });
    }

    @SuppressLint("NewApi")
    private void requestPayment() {
        String amount = mAmountEditText.getText().toString();
        String senderID = mSenderEditText.getText().toString();
        String receiverID = mReceiverEditText.getText().toString();

        if (amount.isEmpty() || senderID.isEmpty() || receiverID.isEmpty()) {
            showToast("Please enter a value in both fields");
        } else {
            //check if id exists
            if (!mServer.checkClientIdExists(senderID) || !mServer.checkClientIdExists(receiverID)) {
                showToast("This id does not exist!");
                return;
            }

            mSender = mServer.getClient(senderID);
            mReceiver = mServer.getClient(receiverID);

            //1. He then sends [RequestPayment, 𝑥, receiver] to A.
            TransactionPacket transactionPacket = new TransactionPacket(Integer.parseInt(amount), mReceiver.getCertificate());

            //create TA of client
            //generate keys
            KeyPairGenerator keyGen= null;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair keyPair = keyGen.generateKeyPair();

                String publicKeyString = Base64.getEncoder().
                        encodeToString(keyPair.getPublic().getEncoded());
                String privateKeyString = Base64.getEncoder().
                        encodeToString(keyPair.getPrivate().getEncoded());

                TrustedApplication ta= new TrustedApplication(mSender.getId(), privateKeyString,
                        publicKeyString);

                //2. Upon receiving [RequestPayment, 𝑥, receiver] from B, client A sends 𝑃 ← TA.Pay(𝑥, receiver) to B.
                PaymentPacket P= ta.pay(transactionPacket, mSender, mReceiver);
                if(P==null){
                    showToast("Sender does not have enough money!");
                    return;
                }

                //send P to the receiver


            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}