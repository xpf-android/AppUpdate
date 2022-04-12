package com.xpf.appupdate.update;

import com.xpf.appupdate.update.net.INetManager;
import com.xpf.appupdate.update.net.impl.OkHttpNetManager;

public class AppUpdater {
    private static AppUpdater sInstance = new AppUpdater();

    //网络请求，下载的能力
    private INetManager mNetManager = new OkHttpNetManager();

//    public void setNetManager(INetManager mNetManager) {
//        this.mNetManager = mNetManager;
//    }


    public INetManager getNetManager() {
        return mNetManager;
    }

    public static AppUpdater getInstance() {
        return sInstance;
    }


}
