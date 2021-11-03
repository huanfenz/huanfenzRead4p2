package com.example.wangpeng.huanfenzread.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.wangpeng.huanfenzread.FragShelfActivity;
import com.example.wangpeng.huanfenzread.MainActivity;
import com.example.wangpeng.huanfenzread.MySettings;
import com.example.wangpeng.huanfenzread.MyShelfBook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.example.wangpeng.huanfenzread.utils.FileUtil.deleteFile;

public class MyDataUtils {
    public static int curChapter = 1;
    public static int curPage = 1;
    public static String cunAddr = "";
    public static int catalog_mode = 0;

    public static String basePath;
    public static String shelfSolderPath;
    public static String shelfInformationFilePath;
    public static String settingsPath;
    public static String picSolerPath;

    /**检查创建书架文件夹*/
    public static void checkAndSetUpShelfSoler(){
        try {
            //创建File

            File folder = new File(shelfSolderPath);
            //如果文件夹不存在，那么创建文件夹
            if(folder.exists() == false){
                folder.mkdirs();
                Log.d("MyDataStorage","saveShelfInformation: 书架文件夹不存在,创建文件夹!");
            }else{
                Log.d("MyDataStorage","saveShelfInformation: 书架信息文件夹已存在");
            }
        }catch (Exception e){
            //输出错误
            Log.e("MyDataStorage","saveShelfInformation: " + e);
        }
    }

    /**检查创建图片文件夹*/
    public static void checkAndSetUpPicSoler(){
        try {
            //创建File
            File folder = new File(picSolerPath);
            //如果文件夹不存在，那么创建文件夹
            if(folder.exists() == false){
                folder.mkdirs();
                Log.d("MyDataStorage","saveShelfInformation: 图片文件夹不存在,创建文件夹!");
            }else{
                Log.d("MyDataStorage","saveShelfInformation: 图片文件夹已存在");
            }
        }catch (Exception e){
            //输出错误
            Log.e("MyDataStorage","saveShelfInformation: " + e);
        }
    }

    /**存书架信息到本地*/
    public static void saveShelfInformation(){
        Log.d("MyDataStorage","saveShelfInformation: 存书架信息开始");
        //检查并创建书架文件夹
        checkAndSetUpShelfSoler();
        //定义对象输出流
        ObjectOutputStream fos=null;
        try {
            //创建用于存数据的text文件
            File file=new File(shelfInformationFilePath);
            //获取输出流
            fos=new ObjectOutputStream(new FileOutputStream(file));
            //这里不能再用普通的write的方法了
            //要使用writeObject
            fos.writeObject(FragShelfActivity.shelfList);
            Log.d("MyDataStorage","saveShelfInformation: 书架信息写入成功！");
        } catch (Exception e) {
            Log.e("MyDataStorage","saveShelfInformation: " + e);
        }finally{
            try {
                if (fos!=null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e("MyDataStorage","saveShelfInformation: " + e);
            }
        }
    }

    /**存设置信息到本地*/
    public static void saveSettings(){
        Log.d("MyDataStorage","saveSettings: 存设置信息开始");
        //检查并创建设置信息
        checkAndSetUpShelfSoler();
        //定义对象输出流
        ObjectOutputStream fos=null;
        try {
            //创建用于存数据的text文件
            File file=new File(settingsPath);
            //获取输出流
            fos=new ObjectOutputStream(new FileOutputStream(file));
            //这里不能再用普通的write的方法了
            //要使用writeObject
            fos.writeObject(MainActivity.mySettings);
            Log.d("MyDataStorage","saveSettings: 设置写入成功！");
        } catch (Exception e) {
            Log.e("MyDataStorage","saveSettings: " + e);
        }finally{
            try {
                if (fos!=null) {
                    fos.close();
                }
            } catch (IOException e) {
                Log.e("MyDataStorage","saveSettings: " + e);
            }
        }
    }

    /**从本地获取书架信息*/
    public static void getShelfInformation(){
        ObjectInputStream ois=null;
        Log.d("MyDataStorage","getShelfInformation: 读书架信息开始");
        //如果文件不存在，直接返回
        File folder = new File(shelfInformationFilePath);
        if(folder.exists() == false){
            Log.d("MyDataStorage","getShelfInformation: 文件不存在，结束读取书架信息");
            return;
        }
        try {
            //获取信息文件的输入流
            ois=new ObjectInputStream(new FileInputStream(new File(shelfInformationFilePath)));
            //获取文件中的数据
            Object object=ois.readObject();
            FragShelfActivity.shelfList = (List<MyShelfBook>) object;
            Log.d("MyDataStorage","getShelfInformation:读书架信息成功");
        } catch (Exception e) {
            Log.e("MyDataStorage","getShelfInformation: " + e);
            //如果出错，删除文件夹
            deleteFile(folder,true);
        }finally{
            try {
                if (ois!=null) {
                    ois.close();
                }
            } catch (IOException e) {
                Log.e("MyDataStorage","getShelfInformation: " + e);
            }
        }
    }

    /**从本地获取设置信息*/
    public static void getSettings(){
        ObjectInputStream ois=null;
        Log.d("MyDataStorage","getSettings: 读设置信息开始");
        //如果文件不存在，直接返回
        File folder = new File(settingsPath);
        if(folder.exists() == false){
            Log.d("MyDataStorage","getSettings: 文件不存在，结束读取设置信息");
            return;
        }
        try {
            //获取信息文件的输入流
            ois=new ObjectInputStream(new FileInputStream(new File(settingsPath)));
            //获取文件中的数据
            Object object=ois.readObject();
            MainActivity.mySettings = (MySettings) object;
            Log.d("MyDataStorage","getSettings:读设置信息成功");
        } catch (Exception e) {
            Log.e("MyDataStorage","getSettings: " + e);
            //如果出错，删除文件夹
            deleteFile(folder,true);
        }finally{
            try {
                if (ois!=null) {
                    ois.close();
                }
            } catch (IOException e) {
                Log.e("MyDataStorage","getSettings: " + e);
            }
        }
    }

    /** 保存书架位图到本地 */
    public static void saveShelfBitmap(Bitmap bm, int i) {
        Log.d("MyDataStorage","saveShelfBitmap: 存书架位图开始");
        //检查图片文件夹路径
        checkAndSetUpPicSoler();
        try {
            File file = new File(picSolerPath + "/"+ i + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Log.d("MyDataStorage","saveShelfBitmap: 存书架位图成功");
        } catch (Exception e) {
            Log.e("MyDataStorage","saveShelfBitmap: " + e);
        }
    }

    /**存所有位图到文件夹*/
    public static void saveShelfAllPic() {
        deleteFile(new File(picSolerPath),true);
        for(int i=0;i<FragShelfActivity.bmList.size();i++){
            saveShelfBitmap(FragShelfActivity.bmList.get(i),i);
        }
    }

    /**取书架图片返回Bitmap*/
    public static Bitmap getShelfPicture(int i){
        String path = picSolerPath + "/"+ i + ".jpg";
        if(new File(path).exists() == false){
            return null;
        }else{
            return BitmapFactory.decodeFile(path);
        }
    }

    /**从本地取图片到bmList*/
    public static void getBmList(){
        //如果为书架信息null，直接返回
        if(FragShelfActivity.shelfList == null)return;
        //如果书架信息为空，直接返回
        if(FragShelfActivity.shelfList.size()<=0){
            Log.d("MyDataStorage","getBmList: 书籍信息为空，结束读取图片");
            return;
        }
        //如果文件夹不存在，直接返回
        File file = new File(picSolerPath);
        if(file.exists() == false){
            Log.d("MyDataStorage","getBmList: 文件不存在，结束读取图片");
            return;
        }

        FragShelfActivity.bmList = new ArrayList<Bitmap>();

        for(int i=0; i<FragShelfActivity.shelfList.size(); i++){
            Bitmap bm = getShelfPicture(i);
            if(bm!=null)FragShelfActivity.bmList.add(bm);
        }
        Log.d("MyDataStorage","getBmList: 读取图片成功");
    }
}
