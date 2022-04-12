package com.xpf.appupdate.update.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.xpf.appupdate.MainActivity;
import com.xpf.appupdate.R;
import com.xpf.appupdate.update.AppUpdater;
import com.xpf.appupdate.update.bean.DownloadBean;
import com.xpf.appupdate.update.net.INetDownloadCallback;
import com.xpf.appupdate.update.utils.AppUtils;

import java.io.File;

import static com.xpf.appupdate.MainActivity.TAG;

public class UpdateVersionShowDialog extends DialogFragment {
    private static final String KEY_DOWNLOAD_BEAN = "download_bean";
    private DownloadBean mDownloadBean;
    private View view;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mDownloadBean = (DownloadBean) arguments.getSerializable(KEY_DOWNLOAD_BEAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_updater, container, false);
        bindEvents(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void bindEvents(View view) {
        TextView titleTextView = view.findViewById(R.id.tv_title);
        TextView contentTextView = view.findViewById(R.id.tv_content);
        final Button updateButton = view.findViewById(R.id.btn_update);

        titleTextView.setText(mDownloadBean.title);
        contentTextView.setText(mDownloadBean.content);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //不能一直点击更新按钮
                updateButton.setEnabled(false);
                //4.点击下载
                final File targetFile = new File(getActivity().getCacheDir(),"target.apk");
                AppUpdater.getInstance().getNetManager().download(mDownloadBean.url, targetFile, new INetDownloadCallback() {
                    @Override
                    public void success(File apkFile) {
                        updateButton.setEnabled(true);//下载完成之后，就可以再次点击
                        //安装的代码
                        Log.d(TAG, "success: " + apkFile.getAbsolutePath());
                        dismiss();
                        //MD5校验
                        String fileMd5 = AppUtils.getFileMd5(targetFile);
                        Log.d(TAG, "md5 = " + fileMd5);
                        if (fileMd5 != null && fileMd5.equals(mDownloadBean.md5)) {
                            AppUtils.installApk(getActivity(),apkFile);
                        } else {
                            Toast.makeText(getActivity(),"md5检测失败",Toast.LENGTH_SHORT).show();
                        }
                        AppUtils.installApk(getActivity(),apkFile);
                    }

                    @Override
                    public void progress(int progress) {
                        //更新界面的代码
                        Log.d(TAG, "progress = " + progress);
                        updateButton.setText(progress+"%");

                    }

                    @Override
                    public void failed(Throwable throwable) {
                        updateButton.setEnabled(true);
                        Toast.makeText(getActivity(),"文件下载失败",Toast.LENGTH_SHORT).show();
                    }
                },UpdateVersionShowDialog.this);
            }
        });
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss: ");
        AppUpdater.getInstance().getNetManager().cancel(this);
    }

    public static void show(FragmentActivity activity, DownloadBean bean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DOWNLOAD_BEAN,bean);
        UpdateVersionShowDialog dialog = new UpdateVersionShowDialog();
        dialog.setArguments(bundle);
        dialog.show(activity.getSupportFragmentManager(),"updateVersionShowDialog");
    }
}
