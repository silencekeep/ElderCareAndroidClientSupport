package com.silencekeep.eldercarewebview;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class PSClass {
    private static Context caller;
    public static void setMActivity(Context context){
        if(Objects.isNull(caller))
            caller = context;
    }
    public static String ReadToken(){
        SharedPreferences sharedPref = caller.getSharedPreferences("loginToken", Context.MODE_PRIVATE);
        String token = sharedPref.getString("token", null);
        return token;
    }
    public static void writeToken(String token){
        SharedPreferences sharedPref = caller.getSharedPreferences("loginToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("token", token);
        editor.apply();
    }
    public static void clearToken(){
        SharedPreferences sharedPref = caller.getSharedPreferences("loginToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }
}
