package com.renyu.sostar.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.sostar.params.CommonParams;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by renyu on 2017/1/18.
 */

public class LocationService extends Service {

    LocationClient mLocClient;

    // 上一次定位的坐标位置
    public static BDLocation lastBdLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                if (bdLocation == null || bdLocation == null) {
                    return;
                }

                EventBus.getDefault().post(bdLocation);
                Log.d("LocationService", bdLocation.getLongitude() + " " + bdLocation.getLatitude());
                uploadLocation(bdLocation);
            }

            @Override
            public void onConnectHotSpotMessage(String s, int i) {

            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setOpenAutoNotifyMode(60000, 10, LocationClientOption.LOC_SENSITIVITY_MIDDLE);
        option.setIsNeedAddress(true);
        mLocClient.setLocOption(option);
        mLocClient.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocClient.stop();
    }

    private void uploadLocation(BDLocation bdLocation) {
        // 只有雇员才上报位置
        if (ACache.get(this).getAsString(CommonParams.USER_TYPE).equals("0")) {
            boolean needUpload=false;
            // 首次上报
            if (lastBdLocation==null) {
                lastBdLocation=bdLocation;
                needUpload=true;
            }
            // 移动距离超过500米才上报
            else {
                LatLng now=new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                LatLng last=new LatLng(lastBdLocation.getLatitude(), lastBdLocation.getLongitude());
                if (DistanceUtil.getDistance(now, last)>500) {
                    lastBdLocation=bdLocation;
                    needUpload=true;
                }
            }
            if (needUpload) {

            }
        }
    }
}
