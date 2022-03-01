package com.example.wangpeng.huanfenzread;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangpeng.huanfenzread.utils.MyDataUtils;
import com.example.wangpeng.huanfenzread.utils.MyTextDisposeUtils;
import com.example.wangpeng.huanfenzread.utils.StatusBarUtil;
import com.example.wangpeng.huanfenzread.view.LoadingView;
import com.example.wangpeng.huanfenzread.view.MyImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.wangpeng.huanfenzread.FragShelfActivity.shelfList;

public class InformationActivity extends AppCompatActivity {

    private static final String baseUrl = "https://www.x88du.com";

    private TextView tv_big_title,tv_title,tv_author,tv_updata_time,tv_description,tv_book_type;
    private MyImageView myImageView;
    private Button bt_addShelf;

    private LoadingView loading;

    String curAddr,webStr;

    String theAddr;

    String title,author,utime,des,type,surl;

    int shelfIndex = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

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

        tv_big_title = (TextView)findViewById(R.id.tv_big_name);
        tv_title = (TextView)findViewById(R.id.textView_book_name);
        tv_author = (TextView)findViewById(R.id.textView_book_author);
        tv_updata_time = (TextView)findViewById(R.id.textView_updata_time);
        tv_description = (TextView)findViewById(R.id.textView_description);
        tv_book_type = (TextView)findViewById(R.id.textView_book_type);
        myImageView = (MyImageView)findViewById(R.id.imageView_book_picture);
        bt_addShelf = (Button)findViewById(R.id.button_addShelf);

        loading = new LoadingView(InformationActivity.this,R.style.CustomDialog);
        loading.show();

        Intent intent = getIntent();
        curAddr = intent.getStringExtra("book_information");
        Log.d("fuckdogdog",curAddr);

        shelfIndex = 9999;
        bt_addShelf.setText("加入书架");
        bt_addShelf.setEnabled(true);
        for (int i = 0 ; i<shelfList.size() ; i++) {
            String stmp = shelfList.get(i).catalogAddr;
            if(curAddr.equals(stmp)){
                shelfIndex = i;
                bt_addShelf.setText("已加入");
                bt_addShelf.setEnabled(false);
            }
        }
        httpGet();
    }

    /**
     * 暂停存数据
     */
    @Override
    protected void onPause() {
        super.onPause();
        MyDataUtils.saveSettings();
        MyDataUtils.saveShelfInformation();
        MyDataUtils.saveShelfAllPic();
    }

    public void ck_addShelf(View view){
        String name = title;

        myImageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(myImageView.getDrawingCache());
        myImageView.setDrawingCacheEnabled(false);

        String catalogAddr = curAddr;
        String readAddr = baseUrl + theAddr;

        MyShelfBook sb = new MyShelfBook(name,shelfList.size(),null,catalogAddr,readAddr,1,1);
        shelfList.add(sb);
        FragShelfActivity.bmList.add(bitmap);

        shelfIndex = shelfList.size() - 1;

        bt_addShelf.setText("已加入");
        bt_addShelf.setEnabled(false);

        Toast.makeText(InformationActivity.this,"添加成功",Toast.LENGTH_SHORT).show();
    }

    public void ck_read(View view){
        Intent intent = new Intent(InformationActivity.this,ReadActivity.class);
        if(shelfIndex == 9999){
            MyDataUtils.curChapter = 1;
            MyDataUtils.curPage = 1;
            intent.putExtra("read_url",baseUrl + theAddr);
        }else{
            intent.putExtra("shelf_index",shelfIndex);
        }
        startActivity(intent);

    }

    public void ck_mulu(View view){
        MyDataUtils.curChapter = shelfIndex==9999 ? 1 : shelfList.get(shelfIndex).curChapter;
        MyDataUtils.curPage = shelfIndex==9999 ? 1 : shelfList.get(shelfIndex).curPage;
        MyDataUtils.catalog_mode = 0;
        MyDataUtils.cunAddr = curAddr;
        Log.d("Information, curAddr", curAddr);
        Intent intent = new Intent(InformationActivity.this,CatalogActivity.class);
        startActivity(intent);
    }

    public void ck_fanhui(View view){
        onBackPressed();
    }

    /**
     * 取第一章的地址
     */
    private void catchCatalog(){
        //第一步截取
        String all = MyTextDisposeUtils.mySubstring(webStr,"<ul>","</ul>").trim();
        //采集
        theAddr = MyTextDisposeUtils.mySubstring(all,"<a href=\"","\"");
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

                surl = MyTextDisposeUtils.mySubstring(webStr,"image\" content=\"","\"");
                myImageView.setImageURL(baseUrl + surl, null);

                title = MyTextDisposeUtils.mySubstring(webStr,"book_name\" content=\"","\"");
                author = MyTextDisposeUtils.mySubstring(webStr,"author\" content=\"","\"");
                type = MyTextDisposeUtils.mySubstring(webStr,"category\" content=\"","\"");

                utime = MyTextDisposeUtils.mySubstring(webStr,"update_time\" content=\"","\"");
                des = MyTextDisposeUtils.mySubstring(webStr,"description\" content=\"","\"");
                des = des.replaceAll("&nbsp;", "");

                tv_big_title.setText(title);
                tv_title.setText(title);
                tv_author.setText("作者：" + author);
                tv_book_type.setText("类型：" + type);
                tv_updata_time.setText("更新时间：" + utime);
                tv_description.setText(des);

                // 采集第一页的地址
                catchCatalog();

                loading.dismiss();
            }
        });
    }
}
