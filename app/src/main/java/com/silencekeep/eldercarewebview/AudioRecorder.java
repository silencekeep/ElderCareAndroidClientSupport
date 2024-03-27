package com.silencekeep.eldercarewebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Objects;

public class AudioRecorder {
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);

    private AudioRecord audioRecord;
    private boolean isRecording = false;

    byte[] buffer;
    private Context caller;
    private final File tempFile;
    private FileOutputStream fos;
    public AudioRecorder(Context caller){
        try {
            tempFile = File.createTempFile("rec", "pcm");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressLint("MissingPermission")
    public void startRecording() throws IOException {
        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        audioRecord.startRecording();

        isRecording = true;

        buffer = new byte[BUFFER_SIZE];
        fos = new FileOutputStream(tempFile);
        Thread t = new Thread(()->{
            while (isRecording) {
                int bytesRead = audioRecord.read(buffer, 0, BUFFER_SIZE);
                // 处理录制的音频数据，这里可以将数据保存为二进制文件或进行其他处理
                if (bytesRead > 0) {
                    try {
                        fos.write(buffer, 0, bytesRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public void stopRecording() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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