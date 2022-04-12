package com.xpf.appupdate.update.net.impl;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.xpf.appupdate.update.net.INetCallback;
import com.xpf.appupdate.update.net.INetDownloadCallback;
import com.xpf.appupdate.update.net.INetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.xpf.appupdate.MainActivity.TAG;

public class OkHttpNetManager implements INetManager {

    private static OkHttpClient sOkHttpClient;
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    static {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(15, TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
        //http
        //https自签名，OkHttp握手的错误
        //builder.sslSocketFactory() 处理证书的相关操作

    }

    @Override
    public void get(String url, final INetCallback callback,Object tag) {
        //Request.Builder-->Request-->Call-->execute/enqueue
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
//        Response response = call.execute();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                //非UI线程
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try{
                    final String string = response.body().string();
                    //确保在主线程执行
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(string);
                        }
                    });
                } catch (Throwable e){
                    e.printStackTrace();
                    callback.failed(e);
                }
            }
        });

    }

    @Override
    public void download(String url, final File targetFile, final INetDownloadCallback downloadCallback,Object tag) {
        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
        }
        Request.Builder builder = new Request.Builder();
        Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;
                try{
                final long totalLen = response.body().contentLength();

                is = response.body().byteStream();
                os = new FileOutputStream(targetFile);
                byte[] buffer = new byte[8*1024];
                long curLen = 0;
                int bufferLen = 0;
                while (!call.isCanceled() && (bufferLen = is.read(buffer)) != -1) {
                    os.write(buffer,0,bufferLen);
                    os.flush();
                    curLen += bufferLen;
                    final long finalCurLen = curLen;
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //为什么要*1.0f ,比如 1 / 5 * 100 = 0 但是1*1.0/5 * 100 = 20
                            downloadCallback.progress((int) (finalCurLen *1.0f/totalLen*100));
                        }
                    });
                }
                    //避免去执行下载操作
                    if (call.isCanceled()) {
                        return;
                    }

                targetFile.setExecutable(true,false);//文件设置可执行
                targetFile.setReadable(true,false);//文件设置可读
                targetFile.setWritable(true,false);//文件设置可写

                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadCallback.success(targetFile);
                    }
                });
                }catch (final Exception e) {
                    e.printStackTrace();
                    if (call.isCanceled()) {
                        return;
                    }
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            downloadCallback.failed(e);
                        }
                    });
                } finally {
                    if (is != null) {
                        is.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                }
            }
        });
    }

    @Override
    public void cancel(Object tag) {
        List<Call> queuedCalls = sOkHttpClient.dispatcher().queuedCalls();
        if (queuedCalls != null) {
            for (Call call : queuedCalls) {
                if (tag.equals(call.request().tag())){
                    Log.d(TAG, "cancel: find call = " + tag);
                    call.cancel();
                }
            }
        }

        List<Call> runningCalls = sOkHttpClient.dispatcher().runningCalls();
        if (runningCalls != null) {
            for (Call call : runningCalls) {
                if (tag.equals(call.request().tag())){
                    Log.d(TAG, "cancel: find call = " + tag);
                    call.cancel();
                }
            }
        }
    }
}
