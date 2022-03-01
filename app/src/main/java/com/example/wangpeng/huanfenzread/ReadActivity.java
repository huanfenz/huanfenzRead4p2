package com.example.wangpeng.huanfenzread;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.animation.ObjectAnimator;

import com.example.wangpeng.huanfenzread.utils.FileUtil;
import com.example.wangpeng.huanfenzread.utils.MyDataUtils;
import com.example.wangpeng.huanfenzread.utils.MyTextDisposeUtils;
import com.example.wangpeng.huanfenzread.utils.StatusBarUtil;
import com.example.wangpeng.huanfenzread.view.JustifyTextView;
import com.example.wangpeng.huanfenzread.view.LoadingView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import static com.example.wangpeng.huanfenzread.FragShelfActivity.shelfList;

public class ReadActivity extends AppCompatActivity {

    private static final String baseUrl = "https://www.x88du.com";

    //控件对象
    private JustifyTextView text;   // 阅读文本
    private TextView title,page,tv_fontSize,name,tv_time;   // 标题、页、字号、名字、时间
    private LinearLayout lin_menu,lin_body,lin_setting,lin_top,lin_bottom;  // 菜单、主体、设置、顶部、底部
    private EditText et_addr;   // 地址输入
    private LoadingView loading;    // 加载

    //内容web的全部内容
    private String webStr = null;   // web的全部内容

    //坐标变量
    private float mPosX,mCurPosX,mCurPosY,maxHeight,maxWidth;

    //每一页的内容
    private String[] everyStr = null;
    //所有内容
    private String allStr = null;
    //每一页的最后一个字符的位置（累计）
    private int[] everyPage = null;
    //当前页和最大页,书号
    private int curPage = 1;
    private int maxPage = 1;
    //最大章节
    private int maxChapter = 1;
    //目标页
    int targetPage = 1;

    //目录地址和章节地址
    private String catalogAddr = "";
    private String curAddr = "";

    //handler
    private Handler handler;

    private int readMode = 2; //默认是2，表示临时模式
    private int shelfIndex = 0;

    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak", "WrongViewCast", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this,false);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
        //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }

        //对象实例化
        name = (TextView)findViewById(R.id.tv_name);
        text = (JustifyTextView) findViewById(R.id.tv_text);
        title = (TextView) findViewById(R.id.tv_title);
        page = (TextView) findViewById(R.id.tv_page);
        tv_fontSize = (TextView)findViewById(R.id.textView_fontSize);
        tv_time =(TextView)findViewById(R.id.tv_curTime);
        lin_menu = (LinearLayout)findViewById(R.id.linear_menu);
        lin_body = (LinearLayout)findViewById(R.id.linear_body);
        lin_setting = (LinearLayout)findViewById(R.id.linear_setting);
        lin_top = (LinearLayout)findViewById(R.id.linear_topfuck);
        lin_bottom = (LinearLayout)findViewById(R.id.linear_bottom);
        et_addr = (EditText)findViewById(R.id.et_addr);
        loading = new LoadingView(ReadActivity.this,R.style.CustomDialog);

        //获取状态栏高度
        //状态栏高度
        int statusBarHeight = getStatusBarHeight(this);
        //测量lin_top的height
        lin_top.measure(0, 0);
        int height = lin_top.getMeasuredHeight();
        //加起来
        int sbh = statusBarHeight + height;
        //修改lin_top的height
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) lin_top.getLayoutParams();
        linearParams.height = sbh;// 控件的高强制设成statusBarHeight
        lin_top.setLayoutParams(linearParams);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

        new TimeThread().start(); //启动新的线程,更新时间

        //设置字体大小
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP,MainActivity.mySettings.fontSize);
        tv_fontSize.setText(MainActivity.mySettings.fontSize+"");

        //设置背景
        setBackground(MainActivity.mySettings.backColor);

        //获取屏幕长和宽
        DisplayMetrics dm = getResources().getDisplayMetrics();
        maxHeight = dm.heightPixels;
        maxWidth = dm.widthPixels;
        Log.d("DisplayMetrics","size：" + maxHeight + " " + maxWidth);

        int cpt=1, tpg=1;
        String tstr, str="";

        Intent intent = getIntent();
        tstr = intent.getStringExtra("read_url");
        shelfIndex = intent.getIntExtra("shelf_index",9999);
        readMode = intent.getIntExtra("shelf_mode",2);

        //如果是书架本地模式
        if(readMode == 0){
            Log.d("TestFile","readMode:" + readMode);
            MyDataUtils.curChapter = cpt = shelfList.get(shelfIndex).curChapter;
            curPage = MyDataUtils.curPage = tpg = shelfList.get(shelfIndex).curPage;
            maxChapter = shelfList.get(shelfIndex).catalogs.length;
        }
        //如果是书架网络模式
        else if(readMode == 1){
            MyDataUtils.curChapter = cpt = shelfList.get(shelfIndex).curChapter;
            curPage = MyDataUtils.curPage = tpg = shelfList.get(shelfIndex).curPage;
            str = shelfList.get(shelfIndex).readAddr;
            //Log.d("ReadActivity",cpt + "\t" + tpg  + "\t"  + str + "");
        }
        //如果是临时模式
        else if(readMode == 2){
            cpt = MyDataUtils.curChapter;
            //目标页等于当前页
            tpg = MyDataUtils.curPage;
            str = tstr;
        }

        //如果是书架网络模式和临时模式，那么直接访问网站并解码
        if(readMode==1||readMode==2){
            if(str.length()>2){
                //设置并访问
                setCurAddrAndGo(str,cpt,tpg);
            }
        } else if(readMode == 0){
            TxtGet();
        }

        //监听触摸事件
        lin_body.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:   // 按下获取x坐标
                        mPosX = motionEvent.getX();
                        Log.d("PushMessage","原x坐标是：" + mPosX);
                        break;
                    case MotionEvent.ACTION_UP: // 抬起获取(x,y)坐标
                        mCurPosX = motionEvent.getX();
                        mCurPosY = motionEvent.getY();
                        Log.d("PushMessage","当前坐标为：" + mCurPosX + " " + mCurPosY);

                        //如果菜单开启，那么关闭
                        if(lin_menu.getVisibility() == View.VISIBLE){
                            //隐藏状态栏
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            closeMenu();    //关闭菜单
                        } else if (mCurPosX - mPosX > 0 && (Math.abs(mCurPosX - mPosX) > 100)) {
                            lastPage(); //上一页
                        } else if (mCurPosX - mPosX < 0 && (Math.abs(mCurPosX - mPosX) > 100)) {
                            nextPage(); //下一页
                        }else{  //点击操作
                            //如果Y坐标在上1/3或者下1/3
                            if(mCurPosY < maxHeight / 3 || mCurPosY > maxHeight / 3 * 2){
                                //如果X坐标在左边
                                if(mCurPosX < maxWidth / 2) lastPage(); //上一页
                                //在右边
                                else nextPage(); //下一页
                            }
                            //如果Y左边在中间
                            else{   // 开启惨淡
                                //显示状态栏
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

                                //显示整个菜单
                                lin_menu.setVisibility(View.VISIBLE);
                                //动画展开top
                                ObjectAnimator animator1 = ObjectAnimator.ofFloat(lin_top, "translationY", -200, 0);
                                animator1.setDuration(250);
                                animator1.start();
                                //动画展开bottom
                                ObjectAnimator animator2 = ObjectAnimator.ofFloat(lin_bottom, "translationY", 280, 0);
                                animator2.setDuration(250);
                                animator2.start();
                            }
                        }
                        break;
                }
                return true;
            }
        });


        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                dividePage();
                curPage = targetPage==9999?maxPage:targetPage;
                setPage();
            }
        };

    }

    //获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            do {
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;  //消息(一个整型值)
                    mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    //在主线程里面处理消息并更新UI界面
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long sysTime = System.currentTimeMillis();//获取系统时间
                    CharSequence sysTimeStr = DateFormat.format("HH:mm:ss", sysTime);//时间显示格式
                    tv_time.setText(sysTimeStr); //更新时间
                    break;
                default:
                    break;

            }
        }
    };

    //关菜单
    private void closeMenu(){
        lin_setting.setVisibility(View.INVISIBLE);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(lin_top, "translationY", 0, -200);
        animator1.setDuration(250);
        animator1.start();
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(lin_bottom, "translationY", 0, 280);
        animator2.setDuration(250);
        animator2.start();

        animator1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                lin_menu.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animator2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                lin_menu.setVisibility(View.INVISIBLE);
                lin_setting.setVisibility(View.INVISIBLE);
                //隐藏状态栏
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    //暂停存数据
    @Override
    protected void onPause() {
        super.onPause();
        MyDataUtils.saveSettings();
        MyDataUtils.saveShelfInformation();
        MyDataUtils.saveShelfAllPic();
    }

    //音量键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                //上一页
                if(lin_menu.getVisibility() == View.VISIBLE){
                    closeMenu();
                }else{
                    lastPage();
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                //下一页
                if(lin_menu.getVisibility() == View.VISIBLE){
                    closeMenu();
                }else{
                    nextPage();
                }
                return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    //上一页
    private void lastPage(){
        //如果是要网络读的，网址为空则返回
        if(readMode==1||readMode==2){
            if(webStr==null)return;
        }
        //上一页
        if(curPage > 1){
            curPage--;
            //如果是书架模式，需要更新书架类的当前页
            if(readMode==0||readMode==1){
                shelfList.get(shelfIndex).curPage = curPage;
            }
            setPage();
        }else{
            //如果是第1章了，直接返回
            if(MyDataUtils.curChapter==1)return;
            //网络的
            if(readMode==1||readMode==2) {
                //获得上一章的url
                String url = MyTextDisposeUtils.mySubstring2(webStr,"<a href=\"","\" target=_top><< 上一章");
                //正则匹配，是否是阅读页url
                boolean isRead = Pattern.matches(".+\\d{8}.html",url);
                //是阅读页，那么处理加载
                if(isRead){
                    url = baseUrl + url;
                    MyDataUtils.curChapter--;
                    if(readMode==1){
                        shelfList.get(shelfIndex).curChapter = MyDataUtils.curChapter;
                    }
                    setCurAddrAndGo(url,0,9999);
                }
            }
            //本地的
            else{
                MyDataUtils.curChapter--;
                shelfList.get(shelfIndex).curChapter--;
                shelfList.get(shelfIndex).renewReadPath();
                String curPath = shelfList.get(shelfIndex).readPath;
                setNewPathAndGo(curPath,0,9999);
            }

        }
    }

    //下一页
    private void nextPage(){
        //如果是要网络读的，网址为空则返回
        if(readMode==1||readMode==2){
            if(webStr==null)return;
        }
        //下一页
        if(curPage<maxPage){
            curPage++;
            //如果是书架模式，需要更新书架类的当前页
            if(readMode==0||readMode==1){
                shelfList.get(shelfIndex).curPage = curPage;
            }
            setPage();
        }else{
            //网络
            if(readMode==1||readMode==2){
                //获得下一章的url
                String url = MyTextDisposeUtils.mySubstring2(webStr,"<a href=\"","\" target=_top>下一章");
                //正则匹配，是否是阅读页url
                boolean isRead = Pattern.matches(".+\\d{8}.html",url);
                //是阅读页，那么处理加载
                if(isRead){
                    url = baseUrl + url;
                    Log.d("fuckdog",url);
                    MyDataUtils.curChapter++;
                    if(readMode==1){
                        shelfList.get(shelfIndex).curChapter = MyDataUtils.curChapter;
                    }
                    setCurAddrAndGo(url,0,1);
                }
            }
            //本地
            else{
                if(MyDataUtils.curChapter == maxChapter)return;
                MyDataUtils.curChapter++;
                shelfList.get(shelfIndex).curChapter++;
                shelfList.get(shelfIndex).renewReadPath();
                String curPath = shelfList.get(shelfIndex).readPath;
                setNewPathAndGo(curPath,0,1);
            }

        }
    }

    //地址确定
    public void ck_addrEnter(View view){
        String str = et_addr.getText().toString();
        setCurAddrAndGo(str,1,1);
    }

    //按钮点击--返回
    public void ck_return(View view){
        onBackPressed();
    }

    //按钮点击——目录
    public void ck_catalog(View view){
        MyDataUtils.cunAddr = catalogAddr;
        MyDataUtils.catalog_mode = 1;//模式1回传
        Intent it = new Intent(ReadActivity.this,CatalogActivity.class);
        it.putExtra("read_mode",readMode);
        if(readMode==0){
            it.putExtra("read_index",shelfIndex);
        }
        startActivityForResult(it,1);
    }
    //按钮点击——菜单
    public void ck_setting(View view){
        lin_setting.setVisibility(View.VISIBLE);
    }

    //按钮点击--缩小字体
    public void ck_narrow_font(View view){
        if(MainActivity.mySettings.fontSize>15){
            MainActivity.mySettings.fontSize--;
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP,MainActivity.mySettings.fontSize);
            text.setText(allStr);
            tv_fontSize.setText(MainActivity.mySettings.fontSize+"");
            targetPage = curPage;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.currentThread().sleep(5);
                    }catch (Exception e){}
                    handler.sendMessage(new Message());
                }
            }).start();

        }
    }

    //按钮点击--放大字体
    public void ck_enlarge_font(View view){
        if(MainActivity.mySettings.fontSize<25){
            MainActivity.mySettings.fontSize++;
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP,MainActivity.mySettings.fontSize);
            text.setText(allStr);
            tv_fontSize.setText(MainActivity.mySettings.fontSize+"");
            targetPage = curPage;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.currentThread().sleep(10);
                    }catch (Exception e){}
                    handler.sendMessage(new Message());
                }
            }).start();
        }
    }

    //设置背景图片
    private void setBackground(int num){
        switch (num){
            case 0:
                lin_body.setBackgroundResource(R.drawable.shape_bg_readbook_black);
                text.setTextColor(Color.parseColor("#ffffff"));
                title.setTextColor(Color.parseColor("#ffffff"));
                page.setTextColor(Color.parseColor("#ffffff"));
                break;
            case 1:
                lin_body.setBackgroundResource(R.drawable.shape_bg_readbook_white);
                text.setTextColor(Color.parseColor("#343434"));
                title.setTextColor(Color.parseColor("#343434"));
                page.setTextColor(Color.parseColor("#343434"));
                break;
            case 2:
                lin_body.setBackgroundResource(R.drawable.shape_bg_new_yellow);
                text.setTextColor(Color.parseColor("#343434"));
                title.setTextColor(Color.parseColor("#343434"));
                page.setTextColor(Color.parseColor("#343434"));
                break;
            case 3:
                lin_body.setBackgroundResource(R.drawable.shape_bg_new_green);
                text.setTextColor(Color.parseColor("#343434"));
                title.setTextColor(Color.parseColor("#343434"));
                page.setTextColor(Color.parseColor("#343434"));
                break;
        }
    }

    public void ck_black(View view){
        MainActivity.mySettings.backColor = 0;
        setBackground(0);
    }
    public void ck_white(View view){
        MainActivity.mySettings.backColor = 1;
        setBackground(1);
    }
    public void ck_yellow(View view){
        MainActivity.mySettings.backColor = 2;
        setBackground(2);
    }
    public void ck_green(View view){
        MainActivity.mySettings.backColor = 3;
        setBackground(3);
    }

    //从目录窗口返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){
            case 1:{
                if(resultCode == RESULT_OK){
                    lin_menu.setVisibility(View.INVISIBLE);
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏

                    if(readMode==0){
                        int num = data.getIntExtra("index_return",1);//从目录页返回的数据
                        shelfList.get(shelfIndex).curChapter = num;
                        shelfList.get(shelfIndex).curPage = 1;
                        shelfList.get(shelfIndex).renewReadPath();
                        String curPath = shelfList.get(shelfIndex).readPath;
                        setNewPathAndGo(curPath,30,1);
                    }else if(readMode == 1 || readMode == 2){
                        String str = data.getStringExtra("data_return");//从目录页返回的数据
                        if (shelfIndex != 9999) {
                            shelfList.get(shelfIndex).curChapter = MyDataUtils.curChapter;
                            shelfList.get(shelfIndex).curPage = 1;
                            shelfList.get(shelfIndex).readAddr = str;
                        }
                        setCurAddrAndGo(str,0,1);
                    }
                }
            }break;
            case 2:{
                if(resultCode == RESULT_OK){
                    String str = data.getStringExtra("url_return");
                    setCurAddrAndGo(str,1,1);
                }
            }break;
            default:break;
        }
    }

    //网络获取
    public void httpGet() {
        //开子线程网络请求

        loading.show();
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

    //获得指定行的高度
//    private int getLineHeight(int line,TextView view){
//        Rect rect = new Rect();
//        view.getLineBounds(line,rect);
//        return rect.bottom - rect.top;
//    }

    //获得该页可以显示的行数
    private int getPageLineCount(JustifyTextView jtv){
        int h=jtv.getBottom()-jtv.getTop()-jtv.getPaddingTop()-jtv.getPaddingBottom();
//        Log.d("fuckfuckfuck1",h+"");
//        int firstH=getLineHeight(0,view);
//        Log.d("fuckfuckfuck2",firstH+"");
//        int otherH=getLineHeight(1,view);
//        Log.d("fuckfuckfuck3",otherH+"");
//        return (int)((h-firstH)/(double)otherH + 1);//向下取整
        int everyH = jtv.getmTextHeight();
        return (int)(h/(double)everyH);
    }

    //获得每页的最后一个字符的下标
    private int[] getPage(JustifyTextView textView){
        //获得总共多少行
        int count = textView.getLineCount();
        Log.d("fuck_dog",""+count);
        //获得一页多少行
        int pCount=getPageLineCount(textView);
        //获得页数,需要向上取整
        int pageNum= (int)Math.ceil((double)count/(double)pCount);
        //返回变量,浪费一个0索引
        int page[]=new int[pageNum + 1];
        for(int i=1;i < pageNum + 1;i++){
            //获得对应行的最后一个字符的下标
            //最后一页特殊处理
            int tmp;
            //如果是最后一页
            if(i == pageNum){
                //那么取总行数-1
                tmp = count - 1;
            }
            //不是最后一页
            else{
                //取i*每页的行数 - 1
                tmp = i*pCount-1;
            }
            //得到每一页最后一个字符的位置
            page[i]=textView.getLayout().getLineEnd(tmp);
        }
        return page;
    }

    //获得每页的字符串
    private void getString(){
        int start = 0;
        for(int i=1;i<=maxPage;i++){
            everyStr[i] = allStr.substring(start,everyPage[i]);//有头没有尾
            start = everyPage[i];
        }
    }

    //分页函数，需要先放置好text
    private void dividePage(){
        //获得每页的最后一个字符的下标
        everyPage = getPage(text);
        //得到最大页
        maxPage = everyPage.length - 1;

        //创建全局everyStr
        everyStr = new String[everyPage.length];
        //获得每页的字符串
        getString();
    }

    //放置内容
    @SuppressLint("SetTextI18n")
    private void setPage(){
        if(curPage>maxPage)curPage=maxPage;
        text.setText(everyStr[curPage]);
        page.setText(curPage + "/" + maxPage + "页");
    }

    private void setCurAddr(String str,int newcpt){
        if(newcpt!=0){
            MyDataUtils.curChapter = newcpt;
            if(readMode==1){
                shelfList.get(shelfIndex).curChapter = MyDataUtils.curChapter;
            }
        }
        curAddr = str;
        et_addr.setText(str);
        int index = curAddr.lastIndexOf('/');
        catalogAddr = curAddr.substring(0,index+1);
    }

    private void setCurAddrAndGo(String str,int newcpt,int targetPg){
        setCurAddr(str,newcpt);
        targetPage = targetPg;
        if(curAddr.length()>2){
            if(readMode==1){
                shelfList.get(shelfIndex).readAddr = curAddr;
            }
            httpGet();
        }
    }

    private void setNewPathAndGo(String pathStr, final int waitTime, int targetPg){
        String titleStr = shelfList.get(shelfIndex).catalogs[shelfList.get(shelfIndex).curChapter];
        title.setText(titleStr);
        name.setText(titleStr+"-"+shelfList.get(shelfIndex).name);

        File file = new File(pathStr);
        String charset = FileUtil.getCharset(file.getAbsolutePath());
        String content = FileUtil.getFileOutputString(file.getAbsolutePath(),charset);

        allStr = content;
        text.setText(allStr);
        //等30毫秒后分页
        targetPage = targetPg;
        if(waitTime==0){
            dividePage();
            shelfList.get(shelfIndex).curPage = curPage = targetPage==9999?maxPage:targetPage;
            setPage();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.currentThread().sleep(waitTime);
                    }catch (Exception e){}
                    handler.sendMessage(new Message());
                }
            }).start();
        }
    }

    //切换为主线程
    private void showResponse(final String response){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //把获取的网页数据赋值给变量response，并设置给TextView控件
                webStr = new String(response.getBytes());
                Log.d("fuckddd",webStr);
                if(webStr.length()<10)return;
                //菜单大标题
                String nameStr = MyTextDisposeUtils.mySubstring(webStr,"<title>","- 88读书网");
                name.setText(nameStr+"");
                //标题
                String titleStr = MyTextDisposeUtils.mySubstring(webStr,"<h1>","</h1>");
                title.setVisibility(View.VISIBLE);
                title.setText(titleStr+"");
                //获取前标志和后标志
                final String startStr = "<div class=\"yd_text2\">";
                final String endStr = "</div>";
                //取文本中间
                String tempStr =  MyTextDisposeUtils.mySubstring(webStr,startStr,endStr);
                //去掉两端多余空格
                tempStr = tempStr.trim();
                Log.d("TextFuck",tempStr);
                //字符串替换，将html中的换行和空格替换进来
                tempStr =  tempStr.replace("&nbsp;","\u0020\u0020");
                tempStr =  tempStr.replace("<br /><br />","\n");
                tempStr =  tempStr.replace("<br />","\n");

                //放置全局str
                allStr = tempStr;
                text.setText(allStr);
                dividePage();
                //目标页是9999说明是目标是最后一页
                curPage = targetPage==9999?maxPage:targetPage;
                setPage();
                if(readMode==0||readMode==1){
                    shelfList.get(shelfIndex).curPage = curPage;
                }

                loading.dismiss();
            }
        });
    }

    private void TxtGet(){
        shelfList.get(shelfIndex).renewReadPath();
        String curPath = shelfList.get(shelfIndex).readPath;
        setNewPathAndGo(curPath,100,MyDataUtils.curPage);
        loading.dismiss();
    }

}
