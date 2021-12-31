package com.example.opsapp.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "==TransactionActivity";

    private Button mReceiveButton;
    private EditText mAmountEditText, mSenderEditText, mReceiverEditText;
    private TextView mProgressTextView;

    private Server mServer;
    private Client mSender, mReceiver;
    private ClientDatabase mClientDatabase;
    private ClientDao mClientDao;
    private String mTAPublicKey;
    private String mTAPrivateKey;
    private PaymentPacket mPaymentPacket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        mServer = Server.getInstance(getApplicationContext());
        mClientDatabase = Room.databaseBuilder(this,
                ClientDatabase.class, "client-database")
                .allowMainThreadQueries()
                .build();
        mClientDao = mClientDatabase.clientDao();

        initUI();
    }

    private void initUI() {
        mAmountEditText = findViewById(R.id.edittext_amount);
        mSenderEditText = findViewById(R.id.edittext_sender);
        mReceiverEditText = findViewById(R.id.edittext_receiver);
        mReceiveButton = findViewById(R.id.button_receive);
        mProgressTextView = findViewById(R.id.textview_transaction_status);
        mProgressTextView.setMovementMethod(new ScrollingMovementMethod());

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
            KeyPairGenerator keyGen = null;
            try {
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(1024);
                KeyPair keyPair = keyGen.generateKeyPair();

                mTAPublicKey = Base64.getEncoder().
                        encodeToString(keyPair.getPublic().getEncoded());
                mTAPrivateKey = Base64.getEncoder().
                        encodeToString(keyPair.getPrivate().getEncoded());

                Log.d(TAG, "private key created: " + mTAPrivateKey);

                TrustedApplication ta = new TrustedApplication(mSender.getId(), mTAPrivateKey,
                        mTAPublicKey, mSender.getBalance());

                //2. Upon receiving [RequestPayment, 𝑥, receiver] from B, client A sends 𝑃 ← TA.Pay(𝑥, receiver) to B.
                mProgressTextView.append("2. Upon receiving [RequestPayment, x, receiver] from B, client A sends P ← TA.Pay(x, receiver) to B.\n\n");
                mPaymentPacket = ta.pay(transactionPacket, mSender, mReceiver, mProgressTextView, this);
                if (mPaymentPacket == null) {
                    showToast("Sender does not have enough money!");
                    return;
                }

                //3. Upon receiving 𝑃 from A, client B performs the following steps:
                //(a) Abort if any of the following conditions is true:
                mProgressTextView.append("3. Upon receiving P from A, client B performs the following steps:\n\n");
                mProgressTextView.append("(a) Abort if any of the following conditions is true:\n\n");

                //- PayVerify(𝑃) ≠ 1,
                mProgressTextView.append("- PayVerify(P) ≠ 1,\n\n");
                if (!PayVerify(mPaymentPacket)) {
                    Log.d(TAG, "payverify != 1");
                    mProgressTextView.append("ABORTED\n\n");
                    return;
                }

                mProgressTextView.append("- P.receiver ≠ receiver, or\n\n");
                if (!mPaymentPacket.getReceiver().equals(mReceiver.getCertificate())) { //- 𝑃.receiver ≠ receiver, or
                    mProgressTextView.append("ABORTED\n\n");
                    return;
                }
                //𝑃 ∈ B.inPaymentLog;
                mProgressTextView.append("- P ∈ B.inPaymentLog;\n\n");

                ArrayList<PaymentPacket> arrayList = mReceiver.getInPaymentLog();
                if (arrayList.contains(mPaymentPacket)) {
                    mProgressTextView.append("ABORTED\n\n");
                    return;
                }


                //(b) B adds 𝑃 to B.inPaymentLog and sends [ReceivedPayment] to A;
                ArrayList<PaymentPacket> arrayList1 = mReceiver.getInPaymentLog();
                arrayList1.add(mPaymentPacket);
                mReceiver.setInPaymentLog(arrayList1);
                mProgressTextView.append("(b) B adds P to B.inPaymentLog and sends [ReceivedPayment] to A;\n\n");

                //send [ReceivedPayment] to A
                mClientDao.updateClient(mReceiver);

                //(c) If 𝑃.receiver.type = “TA”, then B calls TB.Collect(𝑃);
                //(d) Otherwise, B engages in the Claim protocol with S (Figure 9) as soon as B is online.
                mProgressTextView.append("(c) If P.receiver.type = “TA”, then B calls TB.Collect(P);\n\n");
                mProgressTextView.append("(d) Otherwise, B engages in the Claim protocol with S (Figure 9) as soon as B is online.\n\n");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean PayVerify(PaymentPacket P) {
        //Return 1 if and only if all of the following conditions hold:

        //1. TACertVerify(𝑃.sender) =? 1, and

        //2. SigVerify([𝑃.amount,𝑃.sender,𝑃.receiver,𝑃.index],𝑃.sig,𝑃.sender.vk) =? 1.
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(String.valueOf(P.getAmount()));
        arrayList.add(P.getSender());
        arrayList.add(P.getReceiver());

        boolean sigCheck = SigVerify(arrayList, P.getSignature(), mTAPublicKey);

        if (!sigCheck) {
            //abort
            Log.d(TAG, "sig verify false");
            return false;
        }

        return true;
    }

    @SuppressLint("NewApi")
    private boolean SigVerify(ArrayList<String> arrayList, Signature signature, String mTAPublicKey) {
        try {
            byte[] data = Base64.getDecoder().decode((mTAPublicKey.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = null;
            fact = KeyFactory.getInstance("RSA");
            PublicKey publicKey = fact.generatePublic(spec);

            Log.d(TAG, "tapublickey 2: " + Base64.getEncoder().
                    encodeToString(publicKey.getEncoded()));

            signature.initVerify(publicKey);

            byte[] dataBytes = arrayList.toString().getBytes();
            dataBytes = java.util.Arrays.copyOf(dataBytes, 256 / 8);

            signature.update(dataBytes);

            return signature.verify(mPaymentPacket.getSignatureBytes());

//            Signature signature1 = Signature.getInstance("NONEwithRSA");
//
//            signature1.initVerify(publicKey);
//            byte[] dataBytes = arrayList.toString().getBytes();
//            dataBytes = java.util.Arrays.copyOf(dataBytes, 256 / 8);
//
//            signature1.update(dataBytes);
//            byte[] signature1Bytes= signature1.sign();
//
//            return signature1.verify(mPaymentPacket.getSignatureBytes());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}