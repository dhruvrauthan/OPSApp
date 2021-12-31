package com.example.opsapp.model;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
public class Converters {
    @TypeConverter
    public static ArrayList<PaymentPacket> restoreList(String listOfString) {
        return new Gson().fromJson(listOfString, new TypeToken<ArrayList<PaymentPacket>>() {}.getType());
    }

    @TypeConverter
    public static String saveList(ArrayList<PaymentPacket> listOfString) {
        return new Gson().toJson(listOfString);
    }
}
