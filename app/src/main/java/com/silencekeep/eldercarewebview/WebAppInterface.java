package com.silencekeep.eldercarewebview;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WebAppInterface {
    private Context mContext;

    public WebAppInterface(Context context) {
        this.mContext = context;
    }

    @JavascriptInterface
    public void speakText(String message) {
        if (mContext instanceof MainActivity)
            ((MainActivity)mContext).speakText(message);
    }
    @JavascriptInterface
    public void speechBegin() {
        if (mContext instanceof MainActivity)
            ((MainActivity)mContext).startRecording();
    }
    @JavascriptInterface
    public void speechEnd() {
        if (mContext instanceof MainActivity)
            ((MainActivity)mContext).stopRecording();
    }
    @JavascriptInterface
    public Boolean confirmSpeechResult() throws InterruptedException {
        if (mContext instanceof MainActivity) {
            return ((MainActivity)mContext).getRecordResult();
        }
        return false;
    }
    @JavascriptInterface
    public String fetchSpeechResultBase64() throws InterruptedException {
        if (mContext instanceof MainActivity) {
            return ((MainActivity)mContext).retrieveData();
        }
        return null;
    }
    @JavascriptInterface
    public String getSpeechResult() throws InterruptedException {
        if (mContext instanceof MainActivity) {
            try {
                return ((MainActivity)mContext).baiduSpeechToText();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    @JavascriptInterface
    public String getToken() throws InterruptedException {
        return PSClass.ReadToken();
    }
    @JavascriptInterface
    public void setToken(String token) throws InterruptedException {
        PSClass.writeToken(token);
    }
    @JavascriptInterface
    public void clearToken() throws InterruptedException {
        PSClass.clearToken();
    }
}