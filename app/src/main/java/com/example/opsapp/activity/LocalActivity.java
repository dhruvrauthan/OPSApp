package com.example.opsapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.opsapp.R;

public class LocalActivity extends AppCompatActivity {

    private Button mClientActivityButton, mServerActivityButton, mTransactionActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);

        initUI();
    }

    private void initUI() {
        mClientActivityButton= findViewById(R.id.button_client_activity);
        mServerActivityButton= findViewById(R.id.button_server_activty);
        mTransactionActivityButton= findViewById(R.id.button_transaction_activity);

        initClicks();
    }

    private void initClicks() {
        mClientActivityButton.setOnClickListener(v->{
            Intent intent= new Intent(LocalActivity.this, ClientRegistrationActivity.class);
            startActivity(intent);
        });

        mServerActivityButton.setOnClickListener(v->{
            Intent intent= new Intent(LocalActivity.this, ServerActivity.class);
            startActivity(intent);
        });

        mTransactionActivityButton.setOnClickListener(v->{
            Intent intent= new Intent(LocalActivity.this, TransactionActivity.class);
            startActivity(intent);
        });


    }
}