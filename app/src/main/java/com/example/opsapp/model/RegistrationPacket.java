package com.example.opsapp.model;

public class RegistrationPacket {

    private String type;
    private String clientPublicKey;

    public RegistrationPacket(String clientPublicKey){
        this.type= "RegisterClient";
        this.clientPublicKey= clientPublicKey;
    }

}
