package com.example.opsapp.model;

public class TransactionPacket {

    private String type;
    private int amount;
    private String receiver;    //receiver certificate

    public TransactionPacket(int amount, String receiver){
        type= "RequestPayment";
        this.amount= amount;
        this.receiver= receiver;
    }

    public int getAmount() {
        return amount;
    }
}
