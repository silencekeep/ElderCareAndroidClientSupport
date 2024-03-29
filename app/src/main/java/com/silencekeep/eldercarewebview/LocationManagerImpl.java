package com.silencekeep.eldercarewebview;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationManagerImpl {

    private Context mContext;
    private LocationManager mLocationManager;

    public interface MyLocationListener {
        void onLocationReceived(double latitude, double longitude);
    }

    public LocationManagerImpl(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public void requestLocationUpdates(final MyLocationListener listener) {
        // 检查定位权限和提供者是否可用
        if (mLocationManager != null &&
                mContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    listener.onLocationReceived(location.getLatitude(), location.getLongitude());
                    mLocationManager.removeUpdates(this); // 获取到位置信息后停止更新
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // 在位置提供者状态发生变化时调用
                }

                @Override
                public void onProviderEnabled(String provider) {
                    // 当位置提供者启用时调用
                }

                @Override
                public void onProviderDisabled(String provider) {
                    // 当位置提供者禁用时调用
                }
            };

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public Location getLastKnownLocation() {
        // 检查定位权限和提供者是否可用
        if (mLocationManager != null &&
                mContext.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }
}
