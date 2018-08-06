package cn.berfy.sdk.demohttp;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.axingxing.demohttp.R;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.berfy.sdk.demohttp.model.Data;
import cn.berfy.sdk.demohttp.rxjavademo.DemoHttpApi;
import cn.berfy.sdk.demohttp.rxjavademo.model.Book;
import cn.berfy.sdk.demohttp.rxjavademo.presenter.DemoHttpPresenter;
import cn.berfy.sdk.demohttp.rxjavademo.view.IDemoHttpView;
import cn.berfy.sdk.demohttp.util.Base64;
import cn.berfy.sdk.demohttp.util.DisplayUtil;
import cn.berfy.sdk.demohttp.util.MD5;
import cn.berfy.sdk.http.HttpApi;
import cn.berfy.sdk.http.callback.HttpCallBack;
import cn.berfy.sdk.http.callback.OnStatusListener;
import cn.berfy.sdk.http.http.okhttp.utils.GsonUtil;
import cn.berfy.sdk.http.model.HttpParams;
import cn.berfy.sdk.http.model.NetError;
import cn.berfy.sdk.http.model.NetResponse;
import cn.berfy.sdk.mvpbase.base.CommonActivity;
import cn.berfy.sdk.mvpbase.util.ToastUtil;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.observers.Subscribers;
import rx.schedulers.Schedulers;

public class MainActivity extends CommonActivity<IDemoHttpView, DemoHttpPresenter> implements View.OnClickListener, IDemoHttpView {

    private AWebView mWebView1;
    private AWebView mWebView2;
    private EditText mEditMd5;
    private WaterWaveView mWaterWaveView;
    private Button mBtnAnim;
    private Button mBtnMd5;
    private Button mBtnMd5Java;
    private Button mBtnMd5HmacJava;
    private Button mBtnMd5Base64;
    private Button mBtnMd5Base64Java;
    private TextView mTvMd5;
    private Button mBtnHttpGET;
    private Button mBtnHttpPOST;
    private Button mBtnHttpRxjavaGET;
    private Button mBtnHttpRxjavaPOST;
    private TextView mTvHttp;

    //加载so库
    static {
        System.loadLibrary("app-lib");
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void initView() {
        showTitleBar();
        getTitleBar().setLeftIcon(false);
        getTitleBar().setTitle(R.string.app_name);
        ToastUtil.init(getApplicationContext());
        HttpApi.init(getApplicationContext());
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ((WaterWaveView) findViewById(R.id.waterWaveView)).getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = DisplayUtil.getDisplayHeight(this);
        mWaterWaveView = findViewById(R.id.waterWaveView);
        mWaterWaveView.setLayoutParams(layoutParams);
        mWaterWaveView.startWave();
        mWebView1 = findViewById(R.id.webView1);
        mWebView2 = findViewById(R.id.webView2);
        mEditMd5 = findViewById(R.id.edit_md5);
        mTvMd5 = findViewById(R.id.tv_md5);
        mBtnMd5 = findViewById(R.id.btn_md5_c);
        mBtnMd5Java = findViewById(R.id.btn_md5_java);
        mBtnMd5HmacJava = findViewById(R.id.btn_md5_hmac);
        mBtnMd5Base64 = findViewById(R.id.btn_md5_base64);
        mBtnMd5Base64Java = findViewById(R.id.btn_md5_base64_java);
        mTvHttp = findViewById(R.id.tv_response);
        mBtnHttpGET = findViewById(R.id.btn_http_get);
        mBtnHttpPOST = findViewById(R.id.btn_http_post);
        mBtnHttpRxjavaGET = findViewById(R.id.btn_http_rxjava_get);
        mBtnHttpRxjavaPOST = findViewById(R.id.btn_http_rxjava_post);
        mBtnAnim = findViewById(R.id.btn_anim);
        mBtnAnim.setOnClickListener(this);
        mBtnMd5.setOnClickListener(this);
        mBtnMd5Java.setOnClickListener(this);
        mBtnMd5HmacJava.setOnClickListener(this);
        mBtnMd5Base64.setOnClickListener(this);
        mBtnMd5Base64Java.setOnClickListener(this);
        mBtnHttpGET.setOnClickListener(this);
        mBtnHttpPOST.setOnClickListener(this);
        mBtnHttpRxjavaGET.setOnClickListener(this);
        mBtnHttpRxjavaPOST.setOnClickListener(this);
        findViewById(R.id.btn_3des).setOnClickListener(this);
        HttpApi.init(getApplicationContext());
        HttpApi.getInstances()
                .setHost("http://223.203.221.49:18610/")
                .setStatusListener(new OnStatusListener() {
                    @Override
                    public void statusCodeError(int i, long usedTime) {
                        Log.d("httpLog", "测试  请求错误码" + i);
                        mTvHttp.setText("请求错误码" + i + "  耗时:" + usedTime + "ms");
                    }

                    @Override
                    public HttpParams addParams(HttpParams rawParams) {
                        Log.d(HttpApi.getInstances().getLogTAG(), "测试  请求参数  " + GsonUtil.getInstance().toJson(rawParams.getParams()));
                        Log.d(HttpApi.getInstances().getLogTAG(), "测试  请求Headers  " + GsonUtil.getInstance().toJson(rawParams.getHeaders()));
                        rawParams.putParam("sign_ts", System.currentTimeMillis() + "");
                        Iterator<Map.Entry<String, Object>> headers = rawParams.getParams().entrySet().iterator();
                        StringBuffer sb = new StringBuffer();
                        while (headers.hasNext()) {
                            Map.Entry<String, Object> entry = headers.next();
                            sb.append(entry.getKey().trim()).append("=").append(entry.getValue().toString());
                        }

                        HttpParams httpParams1 = new HttpParams();
                        httpParams1.putParam("sign_ts", rawParams.getParams().get("ts"));
                        httpParams1.putHeader("sign", HttpApi.getInstances().encodeMD5(false, sb.toString()));
                        return null;
                    }

                    @Override
                    public void receiveSetCookie(String s) {

                    }

                    @Override
                    public HttpParams addCookies() {
                        return null;
                    }
                })
                .setLogTAG("httpLog")
                .finish();
        mWebView1.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();  // 接受所有网站的证书
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            //是否在webview内加载页面
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return false;
            }
        });
        mWebView2.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();  // 接受所有网站的证书
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            //是否在webview内加载页面
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.loadUrl(request.getUrl().toString());
                } else {
                    view.loadUrl(request.toString());
                }
                return false;
            }
        });
        XXPermissions.with(this)
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {

                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {

                    }
                });
        mHandler.sendEmptyMessage(0);
    }

    @Override
    public DemoHttpPresenter initPresenter() {
        return new DemoHttpPresenter();
    }

    @Override
    public void hiddenLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }

    @Override
    public void showLoadingView(@org.jetbrains.annotations.Nullable String msg) {

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mWebView1.loadUrl("http://blog.sina.com.cn/s/blog_472b14140102xxvg.html");
            mWebView2.loadUrl("http://blog.sina.com.cn/s/blog_472b14140102xuw4.html");
            sendEmptyMessageDelayed(0, 1500);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_md5_c:
                long time = System.currentTimeMillis();
                String md5 = mEditMd5.getText().toString().trim();
                mTvMd5.setText(HttpApi.getInstances().encodeMD5(true, md5) + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                break;
            case R.id.btn_md5_java:
                time = System.currentTimeMillis();
                md5 = mEditMd5.getText().toString().trim();
                mTvMd5.setText(MD5.getStringMD5(md5).toUpperCase() + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                break;
            case R.id.btn_md5_hmac:
                time = System.currentTimeMillis();
                md5 = mEditMd5.getText().toString().trim();
                mTvMd5.setText(HttpApi.getInstances().getHmac(md5).toUpperCase() + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                break;
            case R.id.btn_md5_base64:
                time = System.currentTimeMillis();
                md5 = mEditMd5.getText().toString().trim();
                mTvMd5.setText(HttpApi.getInstances().encodeBase64(md5) + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                break;
            case R.id.btn_md5_base64_java:
                time = System.currentTimeMillis();
                md5 = mEditMd5.getText().toString().trim();
                try {
                    mTvMd5.setText(Base64.encode(md5.getBytes("UTF-8")) + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_3des:
                time = System.currentTimeMillis();
                md5 = mEditMd5.getText().toString().trim();
                mTvMd5.setText(HttpApi.getInstances().encode3Des(md5) + "  耗时:" + (System.currentTimeMillis() - time) + "ms");
                break;
            case R.id.btn_http_get:
                HttpParams httpParams = new HttpParams();
                httpParams.putParam("a", 1);
                httpParams.putParam("b", 2);
                httpParams.putHeader("hearfer", 1);
                HttpApi.getInstances().get("", httpParams, new HttpCallBack() {

                    @Override
                    public void onStart() {
                        Log.d("请求开始", "===");
                    }

                    @Override
                    public void onFinish(NetResponse<String> netResponse) {
                        Log.d("返回成功", "statusCode = " + netResponse.statusCode + " 返回值" + netResponse.data + " 耗时:" + netResponse.usedTime + "ms");
                        mTvHttp.setText(netResponse.data + " 耗时:" + netResponse.usedTime + "ms");
                    }

                    @Override
                    public void onError(NetError netError) {
                        Log.d("返回错误", "statusCode = " + netError.statusCode + " 错误信息" + netError.errMsg + " 耗时:" + netError.usedTime + "ms");
                        mTvHttp.setText(netError.errMsg + " 耗时:" + netError.usedTime + "ms");
                    }
                });
                break;
            case R.id.btn_http_post:
                httpParams = new HttpParams();
                httpParams.setContentType(HttpParams.POST_TYPE.POST_TYPE_JSON);
                httpParams.putParam("a", 1);
                httpParams.putParam("b", 2);
                httpParams.putHeader("hearfer", 1);
                HttpApi.getInstances().post("", httpParams, new HttpCallBack() {

                    @Override
                    public void onStart() {
                        Log.d("请求开始", "===");
                    }

                    @Override
                    public void onFinish(NetResponse<String> netResponse) {
                        Log.d("返回成功", "statusCode = " + netResponse.statusCode + " 返回值" + netResponse.data + " 耗时:" + netResponse.usedTime + "ms");
                        mTvHttp.setText(netResponse.data + " 耗时:" + netResponse.usedTime + "ms");
                    }

                    @Override
                    public void onError(NetError netError) {
                        Log.d("返回错误", "statusCode = " + netError.statusCode + " 错误信息" + netError.errMsg + " 耗时:" + netError.usedTime + "ms");
                        mTvHttp.setText(netError.errMsg + " 耗时:" + netError.usedTime + "ms");
                    }
                });
                break;
            case R.id.btn_anim:
                if (mWaterWaveView.isRunning()) {
                    mBtnAnim.setText("开始动画");
                    mWaterWaveView.stop();
                    mWaterWaveView.setVisibility(View.GONE);
                } else {
                    mBtnAnim.setText("关闭动画");
                    mWaterWaveView.setVisibility(View.VISIBLE);
                    mWaterWaveView.startWave();
                }
                break;
            case R.id.btn_http_rxjava_get:
                getPresenter().checkUpdate("1.4.6", new rx.Observer<Data>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ToastUtil.getInstances().showShort(e.getMessage());
                    }

                    @Override
                    public void onNext(Data book) {
                        if (null != book) {
                            ToastUtil.getInstances().showShort("获取成功" + GsonUtil.getInstance().toJson(book));
                        } else {
                            ToastUtil.getInstances().showShort("获取成功book=null");
                        }
                    }
                });
                break;
            case R.id.btn_http_rxjava_post:
                getPresenter().getSearchBooks("1", "", 1, 10, new rx.Observer<Book>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        ToastUtil.getInstances().showShort(e.getMessage());
                    }

                    @Override
                    public void onNext(Book book) {
                        if (null != book) {
                            ToastUtil.getInstances().showShort("获取成功" + GsonUtil.getInstance().toJson(book));
                        } else {
                            ToastUtil.getInstances().showShort("获取成功book=null");
                        }
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWaterWaveView.stop();
        mHandler.removeCallbacksAndMessages(null);
    }
}
