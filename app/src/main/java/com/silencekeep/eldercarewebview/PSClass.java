package com.silencekeep.eldercarewebview;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Objects;

public class PSClass {
    private static Context caller;
    public static void setMActivity(Context context){
        if(Objects.isNull(caller))
            caller = context;
    }
    public static void reportFalling() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    // 创建 JSON 对象并添加数据
                    JSONObject postData = new JSONObject();
                    postData.put("user_id", PSClass.ReadToken());
                    postData.put("message", "您的亲人发生了摔倒");
                    postData.put("addr_text", "北京市海淀区学院路30号北京科技大学");
                    Location loc = FallDetectionService.getCurrentLoc();
                    postData.put("lon", String.format("%f",loc.getLongitude()));
                    postData.put("lat", String.format("%f",loc.getLatitude()));
                    postData.put("timestamp", System.currentTimeMillis() / 1000);

                    // 设置请求的 URL
                    URL url = new URL("https://api.soulter.top/cccc2024/notice");

                    // 创建 HttpURLConnection 对象并设置请求方法
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // 将 JSON 数据写入请求的输出流
                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(postData.toString());
                    wr.flush();
                    wr.close();

                    // 获取请求的响应数据
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // 解析返回的 JSON 数据
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String result = jsonResponse.getString("result");
                    System.out.println("Result: " + result);

                    // 关闭连接
                    conn.disconnect();
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }.execute();
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
