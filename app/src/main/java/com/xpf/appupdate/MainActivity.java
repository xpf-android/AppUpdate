package com.xpf.appupdate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xpf.appupdate.update.bean.DownloadBean;
import com.xpf.appupdate.update.net.INetCallback;
import com.xpf.appupdate.update.net.INetDownloadCallback;
import com.xpf.appupdate.update.AppUpdater;
import com.xpf.appupdate.update.ui.UpdateVersionShowDialog;
import com.xpf.appupdate.update.utils.AppUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private Button mBtnUpdater;
    private static final String url = "http:59.110.162.30/app_updater_version.json";
    public static final String TAG = "AppUpdate";
    //http://59.110.162.30/v450_imooc_updater.apk

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnUpdater = findViewById(R.id.btn_updater);
        mBtnUpdater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUpdater.getInstance().getNetManager().get(url, new INetCallback() {
                    @Override
                    public void success(String response) {
                        //1.解析json
                        /**
                         * {
                         *     "title":"4.5.0更新啦！",
                         *     "content":"1. 优化了阅读体验；\n2. 上线了 hyman 的课程；\n3. 修复了一些已知问题。",
                         *     "url":"http://59.110.162.30/v450_imooc_updater.apk",
                         *     "md5":"14480fc08932105d55b9217c6d2fb90b",
                         *     "versionCode":"450"
                         * }
                         */

                        Log.d(TAG, "response=" + response);

                        //如果需要更新

                        DownloadBean bean = DownloadBean.parse(response);
                        if (bean == null) {
                            Toast.makeText(MainActivity.this,"版本检测接口返回数据异常",Toast.LENGTH_SHORT).show();
                        }
                        //2.做版本匹配
                        try {
                            //versionCode异常 潜在风险，加上try catch
                            long versionCode = Long.parseLong(bean.versionCode);
                            if (versionCode <= AppUtils.getVersionCode(MainActivity.this)){
                                Toast.makeText(MainActivity.this,"已经是最新版本,无需更新",Toast.LENGTH_SHORT).show();
                                return;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"版本检测接口返回版本号异常",Toast.LENGTH_SHORT).show();
                        }
                        //3.弹框
                        UpdateVersionShowDialog.show(MainActivity.this,bean);

                    }

                    @Override
                    public void failed(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(MainActivity.this, "版本更新失败", Toast.LENGTH_SHORT).show();
                    }
                },MainActivity.this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUpdater.getInstance().getNetManager().cancel(this);
    }
}
