package com.silencekeep.eldercarewebview;

import com.baidu.aip.speech.AipSpeech;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

public class Apis {
    // 设置APPID/AK/SK
    public static final String APP_ID = "58416193";
    public static final String API_KEY = "gceVOXgDovgtMMtVBcMyXUrp";
    public static final String SECRET_KEY = "99HGUbGkEPW8syvVfhG6Pkm5qZw2omlb";
    public static String speechToText(String tempFile){
        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

        // 可以设置请求参数，例如设置语言为中文
        HashMap<String, Object> options = new HashMap<>();
        options.put("lan", "zh");

        // 调用语音识别接口
        try {
            // 语音识别
            JSONObject res = client.asr(tempFile, "pcm", 16000, options);
            // 获取结果
            String result = res.getJSONArray("result").getString(0);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
