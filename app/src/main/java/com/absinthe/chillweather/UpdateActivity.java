package com.absinthe.chillweather;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;


public class UpdateActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        initWebView();
                    } else {
                        // Oops permission denied
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        showProgressDialog();
        WebView webView = findViewById(R.id.wv_update);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                closeProgressDialog();
            }
        });
        webView.loadUrl("https://github.com/zhaobozhen/ChillWeather/releases");
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            // 指定下载地址
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
            request.allowScanningByMediaScanner();
            // 设置通知的显示类型，下载进行时和完成后显示通知
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            // 设置通知栏的标题，如果不设置，默认使用文件名
            request.setTitle("ChillWeather");
            // 设置通知栏的描述
            request.setDescription("正在下载船新版本..");
            // 允许在计费流量下下载
            request.setAllowedOverMetered(false);
            // 允许该记录在下载管理界面可见
            request.setVisibleInDownloadsUi(true);
            // 设置下载文件保存的路径和文件名
            String fileName  = URLUtil.guessFileName(url, contentDisposition, mimetype);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            // 添加一个下载任务
            downloadManager.enqueue(request);
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.on_checking));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
