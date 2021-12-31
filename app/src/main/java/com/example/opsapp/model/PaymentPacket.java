package com.example.opsapp.model;

import java.security.Signature;

public class PaymentPacket {

    private int amount;
    private String sender;
    private String receiver;
    private int index; //TA
    private byte[] signatureBytes;
    private Signature sign;

    public PaymentPacket(int amount, String sender, String receiver, byte[] signatureBytes, Signature sign){
        this.amount= amount;
        this.sender= sender;
        this.receiver= receiver;
        //this.index= index;
        this.sign= sign;
        this.signatureBytes= signatureBytes;
    }

    public Signature getSignature() {
        return sign;
    }

    public void setSignature(Signature signature) {
        this.sign = signature;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte[] getSignatureBytes() {
        return signatureBytes;
    }

    public void setSignatureBytes(byte[] signatureBytes) {
        this.signatureBytes = signatureBytes;
    }
}