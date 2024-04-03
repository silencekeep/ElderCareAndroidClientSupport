package com.silencekeep.eldercarewebview;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import android.Manifest;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FallDetectionService extends Service implements SensorEventListener {

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private static Location currentLoc;
    private AcceleatorProc accProc;
    private static Context m_activity;
    private SensorManager sensorManager;
    private int performanceCounter;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;

    public static Location getCurrentLoc(){
        return FallDetectionService.currentLoc;
    }
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
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    // 处理获取到的位置信息
                    sendLocationBroadcast(location);
                }

                @Override
                public void onLocationChanged(@NonNull List<Location> locations) {
                    LocationListener.super.onLocationChanged(locations);
                }

                @Override
                public void onFlushComplete(int requestCode) {
                    LocationListener.super.onFlushComplete(requestCode);
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }
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
    private void sendLocationBroadcast(Location location) {
        currentLoc = location;
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
                if(ave > FALL_THRESHOLD) {
                    ((MainActivity) m_activity).speakText("您似乎摔倒了");
                    Toast.makeText(m_activity,"您似乎摔倒了",Toast.LENGTH_SHORT).show();
                    try {
                        PSClass.reportFalling();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
//                StringBuffer sb = new StringBuffer();
//                for(int i=0;i<(ave);i++) sb.append("=");
//                Log.e("length",String.format("%3.2f%s",(ave / accProc.getRecentData().size()),sb.toString()));
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
