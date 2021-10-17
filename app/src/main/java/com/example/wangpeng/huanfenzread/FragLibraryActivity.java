package com.example.wangpeng.huanfenzread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.wangpeng.huanfenzread.utils.MyTextDisposeUtils;
import com.example.wangpeng.huanfenzread.view.LoadingView;

public class FragLibraryActivity extends Fragment {

    private WebView webView;
    private WebSettings settings;
    private LoadingView loading;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_frag_library,container,false);

        webView = (WebView) view.findViewById(R.id.WebView);

        //设置WebView属性，能够执行Javascript脚本
        settings = webView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);//开启DOM缓存，关闭的话H5自身的一些操作是无效的
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setBlockNetworkImage(false);

        //伪装成PC端
        settings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
        //自适应屏幕
        settings.setUseWideViewPort(true); settings.setLoadWithOverviewMode(true);
        //自动缩放
        settings.setBuiltInZoomControls(true); settings.setSupportZoom(true);
        //支持获取手势焦点
        webView.requestFocusFromTouch();

        //加载需要显示的网页
        webView.loadUrl("https://m.88dushu.com/");

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                loading = new LoadingView(getActivity(),R.style.CustomDialog);
                loading.show();
            }

            //覆盖shouldOverrideUrlLoading 方法
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                String tmpStr = url;
                int num = tmpStr.indexOf("info");
                //如果是目录页，那么进入介绍界面
                if(num!=-1) {
                    //替换成PC端网页
                    String booknum = MyTextDisposeUtils.mySubstring(tmpStr,"info/","/");
                    String booknumst = booknum.substring(0,booknum.length()-3);

                    String data = "https://www.88dushu.com/xiaoshuo/"+ booknumst +"/"+ booknum +"/";

                    Log.d("fuckdogdog",data);

                    //存数据跳转
                    Intent intent = new Intent(getActivity(),InformationActivity.class);
                    intent.putExtra("book_information",data);
                    startActivity(intent);
                }else{
                    view.loadUrl(url);
                }
                return true;
            }

        });



        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                //当进度走到100的时候做自己的操作
                if(progress == 100){
                    loading.dismiss();
                }
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK ) {
                        //这里处理返回键事件
                        if (webView.canGoBack()) {
                            webView.goBack();// 返回前一个页面
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        //究极返回
        return view;
    }
}

