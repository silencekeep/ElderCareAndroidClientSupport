package com.silencekeep.eldercarewebview;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;

public class FallDetectionService extends Service implements SensorEventListener {

    private AcceleatorProc accProc;
    private static Context m_activity;
    private SensorManager sensorManager;
    private int performanceCounter;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private static final float FALL_THRESHOLD = 65.0f * 0.81847f; // 摔倒阈值
    private boolean isFallDetected = false;

    public static void setM_activity(Context context){
        if(Objects.isNull(m_activity)) m_activity = context;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "ElderCareForegroundServiceChannel",
                    "ElderCare Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        performanceCounter = 0;
        accProc = new AcceleatorProc(20);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 在此处执行你想要在后台持续执行的任务

        // 创建一个通知以将服务置于前台
        createNotificationChannel();
        Notification notification = new Notification.Builder(this, "ElderCareForegroundServiceChannel")
                .setContentTitle("ElderCare")
                .setContentText("正在后台保障您的健康安全")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            performanceCounter++;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            //Log.d("Acceleration: ", String.format("%2.2f  %2.2f  %2.2f",x,y,z));
            accProc.pushAcceleatorData(accProc.new AccVec3d(x,y,z));
            accProc.lazyEvaluateFalldown();

            if(performanceCounter % 20 == 0){
                performanceCounter -= 20;
                Iterator<Double> iterator = accProc.getRecentData().iterator();
                double ave = 0;
                while (iterator.hasNext()) {
                    ave += iterator.next();
                }
                ave /= accProc.getRecentData().size();
                if(ave > FALL_THRESHOLD)
                    ((MainActivity)m_activity).speakText("摔倒了");
                //StringBuffer sb = new StringBuffer();
                //for(int i=0;i<();i++) sb.append("=");
                //Log.e("length",String.format("%3.2f%s",(ave / accProc.getRecentData().size()),sb.toString()));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 空实现
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        stopForeground(true);
    }

}
