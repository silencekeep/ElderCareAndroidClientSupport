package com.silencekeep.eldercarewebview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private AudioRecorder audioRecorder;
    private LocationManagerImpl locImpl;
    private String recognizeResult;
    public Boolean getRecordResult(){
        try{
            audioRecorder.retrieveStreamData();
            return true;
        } catch(Exception ex){
            return false;
        }
    }

    public void startRecording() {
        try{
            audioRecorder.startRecording();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    // 结束录音
    public void stopRecording() {
        audioRecorder.stopRecording();
    }
    public String retrieveData(){
        try{
            return audioRecorder.retrieveStreamData();
        } catch(Exception ex){
            return null;
        }
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FallDetectionService.setM_activity(this);
        PSClass.setMActivity(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textToSpeech = new TextToSpeech(this, this);
        audioRecorder = new AudioRecorder(this);
        final WebView wv = findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebContentsDebuggingEnabled(true);
        wv.addJavascriptInterface(new WebAppInterface(this), "Android");
        wv.loadUrl("http://59.110.52.50:3000/login");

        Intent intent = new Intent(this, FallDetectionService.class);
        startForegroundService(intent);
//        PackageManager packageManager = this.getPackageManager();
//        List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
//        Toast.makeText(this, String.format("%s",activities.size()), Toast.LENGTH_SHORT).show();

        locImpl = new LocationManagerImpl(this);
    }
    public LocationManagerImpl getLocMgrImpl(){
        return locImpl;
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            }
        } else {

        }
    }
    // 启动语音识别
    public void speakText(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }
    public String baiduSpeechToText() throws Exception {
        try {
            return audioRecorder.baiduRecognizeStreamData();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}