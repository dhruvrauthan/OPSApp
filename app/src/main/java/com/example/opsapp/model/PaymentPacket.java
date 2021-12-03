package com.example.opsapp.model;

public class PaymentPacket {

    private int amount;
    private String sender;
    private String receiver;
    private int index; //TA
    private String sig;

    public PaymentPacket(int amount, String sender, String receiver, String sig){
        this.amount= amount;
        this.sender= sender;
        this.receiver= receiver;
        //this.index= index;
        this.sig= sig;
    }

}