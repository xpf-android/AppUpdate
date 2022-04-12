package com.xpf.appupdate.update.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class DownloadBean implements Serializable {
    public String title;
    public String content;
    public String url;
    public String md5;
    public String versionCode;

    public static DownloadBean parse(String response) {

        try {
            JSONObject responseJson = new JSONObject(response);
            String title = responseJson.optString("title");
            String content = responseJson.optString("content");
            String url = responseJson.optString("url");
            String md5 = responseJson.optString("md5");
            String versionCode = responseJson.optString("versionCode");
            DownloadBean bean = new DownloadBean();
            bean.title = title;
            bean.content = content;
            bean.url = url;
            bean.md5 = md5;
            bean.versionCode = versionCode;
            return bean;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
