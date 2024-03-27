package com.silencekeep.eldercarewebview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaRecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

public class AudioRecorderRes {

    private MediaRecorder mediaRecorder = new MediaRecorder();

    private final File tempFile;
    private Context caller;
    public AudioRecorderRes(Context caller) {
        this.caller = caller;
        mediaRecorder = new MediaRecorder(caller);
        try {
            tempFile = File.createTempFile("rec", "m4a");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressLint("MissingPermission")
    public void startRecording() throws IOException {
        try {
            tempFile.delete();
            mediaRecorder.reset();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(96000); // 设置音频编码比特率
            mediaRecorder.setAudioChannels(1); // 设置音频通道数
            mediaRecorder.setAudioSamplingRate(44100); // 设置音频采样率
            mediaRecorder.setOutputFile(tempFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try {
            mediaRecorder.stop();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
    public String retrieveStreamData() throws Exception {
        File file = new File(tempFile.getAbsolutePath());
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(data);
        fis.close();
        String s = Base64.getEncoder().encodeToString(data);
        return s;
    }
    public String baiduRecognizeStreamData() throws Exception {
        return Apis.speechToText(tempFile.getAbsolutePath());
    }
}