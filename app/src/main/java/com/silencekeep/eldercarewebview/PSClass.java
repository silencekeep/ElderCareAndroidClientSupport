package com.silencekeep.eldercarewebview;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.Toast;

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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PSClass {
    private static Context caller;
    public static void setMActivity(Context context){
        if(Objects.isNull(caller))
            caller = context;
    }
    private static String chineseToUnicode(String chineseString) {
        StringBuilder unicodeString = new StringBuilder();
        for (int i = 0; i < chineseString.length(); i++) {
            char c = chineseString.charAt(i);
            if (c < 128) {
                // ASCII字符直接拼接
                unicodeString.append(c);
            } else {
                // 非ASCII字符转换为Unicode编码格式
                unicodeString.append("\\u").append(Integer.toHexString(c | 0x10000).substring(1));
            }
        }
        return unicodeString.toString();
    }
    private static long lastSuccess = 0;
    private static boolean isPermitDo(long nowSucc){
        if(nowSucc - lastSuccess > 1000 * 15){
            lastSuccess = nowSucc;
            return true;
        }
        return false;
    }
    public static void reportFalling() throws Exception {
        if(!isPermitDo(System.currentTimeMillis())) {
            Toast.makeText(caller,"已经成功上报信息！",Toast.LENGTH_SHORT).show();
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    // 创建 JSON 对象并添加数据
                    // 在这里执行你的网络请求逻辑
                    JSONObject postData = new JSONObject();
                    postData.put("uid", PSClass.ReadToken());
                    postData.put("message", chineseToUnicode("您的亲人发生了摔倒"));
                    postData.put("addr_text", chineseToUnicode("北京市海淀区学院路30号北京科技大学"));
                    Location loc = FallDetectionService.getCurrentLoc();
                    if(Objects.isNull(loc)) return false;
                    postData.put("lon", loc.getLongitude());
                    postData.put("lat", loc.getLatitude());
                    postData.put("timestamp", System.currentTimeMillis() / 1000);

                    // 设置请求的 URL
                    URL url = new URL("https://api.soulter.top/cccc2024/notice");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    //conn.setDoOutput(true);

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
                    int result = jsonResponse.getInt("code");
                    // 关闭连接
                    conn.disconnect();
                    if(result == 0)
                        return true; // 请求成功
                } catch (Exception e) {

                }
                return false; // 请求失败
            }
        };


        Future<Boolean> future = executor.submit(callable);

        try {
            Boolean result = future.get();
            if (result) {
                Toast.makeText(caller,"信息上报成功！",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(caller,"信息上报失败！可能是未开启定位！",Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();
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
