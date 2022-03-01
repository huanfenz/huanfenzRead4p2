package com.example.wangpeng.huanfenzread;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wangpeng.huanfenzread.utils.MyDataUtils;
import com.example.wangpeng.huanfenzread.utils.MyTextDisposeUtils;
import com.example.wangpeng.huanfenzread.utils.StatusBarUtil;
import com.example.wangpeng.huanfenzread.view.LoadingView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.wangpeng.huanfenzread.FragShelfActivity.shelfList;

public class CatalogActivity extends AppCompatActivity {

    private static final String baseUrl = "https://www.x88du.com";

    private ListView lv_catalog;
    private LoadingView loading;

    String webStr = null;
    String curAddr = "";
    String[] everyStr = null;
    String[] everyAddr = null;

    private Handler handler;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this,true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }

        lv_catalog = (ListView)findViewById(R.id.lv_catalog);
        loading = new LoadingView(CatalogActivity.this,R.style.CustomDialog);
        loading.show();

        Intent intent = getIntent();
        final int readMode = intent.getIntExtra("read_mode",2);
        int shelfIndex = intent.getIntExtra("read_index",9999);
        if(readMode==0){
            getLocalCatalog(shelfIndex);
        }else{
            curAddr = MyDataUtils.cunAddr;
            httpGet();
        }

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                loading.dismiss();
                Toast.makeText(CatalogActivity.this,"加载超时，请检查网络连接!",Toast.LENGTH_SHORT).show();
                finish();
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.currentThread().sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(loading.isShowing() == true){
                    Message message = new Message();
                    handler.sendMessage(message);
                }
            }
        }).start();

        // 选项点击事件
        lv_catalog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MyDataUtils.curChapter = i + 1;
                MyDataUtils.curPage = 1;
                Intent intent;
                if(readMode==0){
                    intent = new Intent();
                    intent.putExtra("index_return",i+1);
                    setResult(RESULT_OK,intent);
                    finish();
                }else{
                    if(MyDataUtils.catalog_mode == 0){
                        intent = new Intent(CatalogActivity.this,ReadActivity.class);
                        intent.putExtra("read_url",baseUrl + everyAddr[i]);
                        startActivity(intent);
                    }else{
                        intent = new Intent();
                        intent.putExtra("data_return",baseUrl + everyAddr[i]);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                }
            }
        });
    }

    //暂停存数据
    @Override
    protected void onPause() {
        super.onPause();
        //打开preference，名称为someData,如果没有，那么创建
        SharedPreferences someData = getSharedPreferences("someData",0);
        //让someData处于编辑状态
        SharedPreferences.Editor editor = someData.edit();
        //存数据
        editor.putString("curAddr","");
        editor.putInt("chapter",1);
        editor.putInt("page",1);
        //完成提交
        editor.commit();
    }

    public void ck_cfanhui(View view){
        onBackPressed();
    }

    private void catchCatalog(){
        //第一步截取
        String all = MyTextDisposeUtils.mySubstring(webStr,"<ul>","</ul>");
        //去掉收尾空
        all = all.trim();

        //<a href="36869968.html">第一章：不要放弃治疗</a>
        //<li><a href="51686783.html">第一章 穿越的肝帝</a></li>
        //获得数量
        int count = 0;
        int start = 0;
        while (true){
            int index = all.indexOf("<li><a href=",start);
            if(index== - 1){
                break;
            }
            else{
                count ++;
                start = index + "<li><a href=".length();
            }
        }

        everyAddr = new String[count];
        everyStr = new String[count];

        //采集
        start = 0;
        for(int i=0;i<count;i++){
            everyAddr[i] = MyTextDisposeUtils.myStartSubstring(all,start,"<a href=\"","\"");
            everyStr[i] = everyStr[i] = MyTextDisposeUtils.myStartSubstring(all, start,"title=\"","\"");
            if(i == MyDataUtils.curChapter - 1){
                everyStr[i] = everyStr[i] + " ✔";
            }
            start = all.indexOf("</a> </li>", start) + "</a> </li>".length();
        }
    }

    public void httpGet() {
        //开子线程网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                BufferedReader reader=null;
                String urls= curAddr;

                try {
                    URL url=new URL(urls);
                    connection=(HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(5000);
                    connection.setConnectTimeout(5000);
                    InputStream in=connection.getInputStream();

                    reader = new BufferedReader( new InputStreamReader(in,"gbk"));
                    StringBuilder response =new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null){
                        response.append(line);
                    }
                    showResponse(response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (reader!=null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    //切换为主线程
    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webStr = new String(response.getBytes());
                catchCatalog();

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        CatalogActivity.this,android.R.layout.simple_list_item_1,everyStr);
                lv_catalog.setAdapter(adapter);

                int index = MyDataUtils.curChapter - 1;

                lv_catalog.setSelection(Math.max(index - 6, 0));

                loading.dismiss();
            }
        });
    }

    private void getLocalCatalog(int shelfIndex){
        everyStr = shelfList.get(shelfIndex).catalogs;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                CatalogActivity.this,android.R.layout.simple_list_item_1,everyStr);
        lv_catalog.setAdapter(adapter);
        int index = MyDataUtils.curChapter - 1;
        lv_catalog.setSelection(index-6<0 ? 0 : index-6);
        loading.dismiss();

    }

}
