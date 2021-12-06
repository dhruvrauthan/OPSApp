package com.example.opsapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.opsapp.R;
import com.example.opsapp.dao.ClientDao;
import com.example.opsapp.database.ClientDatabase;
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

    private Button mReceiveButton;
    private EditText mAmountEditText, mSenderEditText, mReceiverEditText;
    private TextView mProgressTextView;

    private Server mServer;
    private Client mSender, mReceiver;
    private ClientDatabase mClientDatabase;
    private ClientDao mClientDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        mServer = Server.getInstance(getApplicationContext());
        mClientDatabase= Room.databaseBuilder(this,
                ClientDatabase.class, "client-database")
                .allowMainThreadQueries()
                .build();
        mClientDao= mClientDatabase.clientDao();

        initUI();
    }

    private void initUI() {
        mAmountEditText= findViewById(R.id.edittext_amount);
        mSenderEditText= findViewById(R.id.edittext_sender);
        mReceiverEditText= findViewById(R.id.edittext_receiver);
        mReceiveButton = findViewById(R.id.button_receive);
        mProgressTextView= findViewById(R.id.textview_transaction_status);

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

            //1. Client B sets receiver ← TB.cert if TB ≠ ⊥. Otherwise, receiver ← certB.
            // He then sends [RequestPayment, 𝑥, receiver] to A.
            TransactionPacket transactionPacket = new TransactionPacket(Integer.parseInt(amount), mReceiver.getCertificate());
            mProgressTextView.setText("");
            mProgressTextView.setText("1. Client B sets receiver ← TB.cert if TB ≠ ⊥. Otherwise, receiver ← certB. He then sends [RequestPayment, x, receiver] to A.\n\n");

            //create TA of sender
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
                        publicKeyString, mSender.getBalance());

                //2. Upon receiving [RequestPayment, 𝑥, receiver] from B, client A sends 𝑃 ← TA.Pay(𝑥, receiver) to B.
                mProgressTextView.append("2. Upon receiving [RequestPayment, x, receiver] from B, client A sends P ← TA.Pay(x, receiver) to B.\n\n");
                PaymentPacket P= ta.pay(transactionPacket, mSender, mReceiver, mProgressTextView, this);
                if(P==null){
                    showToast("Sender does not have enough money!");
                    return;
                }

                //send P to the receiver
                mClientDao.updateClient(mReceiver);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }



    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}