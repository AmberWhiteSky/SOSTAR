package com.renyu.sostar.activity;

import android.Manifest;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.receiver.BaiduMapSDKReceiver;
import com.blankj.utilcode.utils.FileUtils;
import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.ACache;
import com.renyu.sostar.BuildConfig;
import com.renyu.sostar.R;
import com.renyu.sostar.application.SostarApp;
import com.renyu.sostar.params.CommonParams;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by renyu on 2016/12/27.
 */

public class SplashActivity extends BaseActivity {

    @Override
    public void initParams() {
        String[] permissions={Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        checkPermission(permissions, "请授予SD卡读写权限", new OnPermissionCheckedListener() {
            @Override
            public void checked(boolean flag) {

            }

            @Override
            public void grant() {
                // 初始化文件夹
                FileUtils.createOrExistsDir(CommonParams.IMAGE_PATH);
                FileUtils.createOrExistsDir(CommonParams.HOTFIX_PATH);
                FileUtils.createOrExistsDir(CommonParams.FILE_PATH);

                if (ACache.get(SplashActivity.this).getAsString("hotfix_version")!=null &&
                        !ACache.get(SplashActivity.this).getAsString("hotfix_version").equals(BuildConfig.VERSION_NAME)) {
                    // 删除老版本的热修复补丁
                    FileUtils.deleteFilesInDir(CommonParams.HOTFIX_PATH);
                    // 更新热修复补丁版本
                    ACache.get(SplashActivity.this).put("hotfix_version", BuildConfig.VERSION_NAME);
                }
                // 加载热修复补丁
                ((SostarApp) getApplication()).mPatchManager.loadPatch();
                List<File> hotfixs = FileUtils.listFilesInDir(CommonParams.HOTFIX_PATH);
                for (File hotfix : hotfixs) {
                    try {
                        ((SostarApp) getApplication()).mPatchManager.addPatch(hotfix.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void denied() {

            }
        });
    }

    @Override
    public int initViews() {
        return R.layout.activity_splash;
    }

    @Override
    public void loadData() {
        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        BaiduMapSDKReceiver mReceiver = new BaiduMapSDKReceiver();
        registerReceiver(mReceiver, iFilter);
    }

    @Override
    public int setStatusBarColor() {
        return ContextCompat.getColor(this, R.color.colorPrimaryDark);
    }

    @Override
    public int setStatusBarTranslucent() {
        return 0;
    }
}
