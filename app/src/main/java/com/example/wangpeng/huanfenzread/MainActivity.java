package com.example.wangpeng.huanfenzread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.wangpeng.huanfenzread.utils.MyDataUtils;
import com.example.wangpeng.huanfenzread.utils.PermisionUtils;
import com.example.wangpeng.huanfenzread.utils.StatusBarUtil;
import com.example.wangpeng.huanfenzread.view.BottomBarView;

import static com.example.wangpeng.huanfenzread.FragShelfActivity.shelfList;

public class MainActivity extends AppCompatActivity {

    private long exitTime;

    public static MySettings mySettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //向用户请求本地存储读写权限
        PermisionUtils.verifyStoragePermissions(this);

        // 获取当前软件的files目录
        String path = this.getExternalFilesDir("").getPath();   ///storage/emulated/0/Android/data/com.example.wangpeng.huanfenzread/files
        // 根目录定义
        MyDataUtils.basePath = path;
        // 书架目录定义
        MyDataUtils.shelfSolderPath = MyDataUtils.basePath+"/shelf";
        // 书架信息目录定义
        MyDataUtils.shelfInformationFilePath = MyDataUtils.basePath+"/shelf/inf.text";
        // 设置目录定义
        MyDataUtils.settingsPath = MyDataUtils.basePath+"/shelf/settings.text";
        // 图片目录定义
        MyDataUtils.picSolerPath = MyDataUtils.basePath+"/shelf/picture";

        //从本地获取设置信息
        MyDataUtils.getSettings();
        if(mySettings == null) mySettings = new MySettings(19,2);

        //底部菜单显示
        BottomBarView bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.fl_container)
                .setTitleBeforeAndAfterColor("#999999", "#007fff")
                .addItem(FragShelfActivity.class,
                        "书架",
                        R.drawable.icon_shelf_false,
                        R.drawable.icon_shelf_true)
                .addItem(FragLibraryActivity.class,
                        "书库",
                        R.drawable.icon_library_false,
                        R.drawable.icon_library_true)
                .build();
    }

    //跳回界面的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode){
            case Activity.RESULT_OK://该结果码要与Fragment中的一致
                //我这里使用的是根据结果码获取数据，然后加上下面一句代码，其他的什么都不用做
                super.onActivityResult(requestCode,resultCode,data);
                break;
        }
    }

    //暂停存数据
    @Override
    protected void onPause() {
        super.onPause();
        //存基本设置
        MyDataUtils.saveSettings();
        //存书架信息
        MyDataUtils.saveShelfInformation();
        //存书架图片
        MyDataUtils.saveShelfAllPic();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //普通页面的单双击事件设置，这里是双击退出程序的设置。
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                MyDataUtils.saveSettings();
                MyDataUtils.saveShelfInformation();
                MyDataUtils.saveShelfAllPic();
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
