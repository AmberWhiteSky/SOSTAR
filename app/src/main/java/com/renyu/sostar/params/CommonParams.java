package com.renyu.sostar.params;

import android.os.Environment;

import java.io.File;

/**
 * Created by renyu on 2016/12/26.
 */

public class CommonParams {

    // 腾讯bugly appId
    public static String BUGLY_APPID="95c4c316f5";

    // 项目根目录
    public static String ROOT_PATH= Environment.getExternalStorageDirectory().getPath()+ File.separator + "sostar";
    // 项目图片目录
    public static String IMAGE_PATH= ROOT_PATH + File.separator + "image";
    // 项目文件目录
    public static String FILE_PATH= ROOT_PATH + File.separator + "file";
    // 项目热修复目录
    public static String HOTFIX_PATH= ROOT_PATH + File.separator + "hotfix";

    public final static int RESULT_PERMISSION=1000;
}
