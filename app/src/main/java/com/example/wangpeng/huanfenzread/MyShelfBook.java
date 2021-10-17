package com.example.wangpeng.huanfenzread;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.wangpeng.huanfenzread.utils.MyDataUtils;

import java.io.Serializable;
import java.util.List;


public class MyShelfBook implements Serializable {
    //模式,0是本地书籍,1是网络书籍
    int mode;

    //网络书名
    String name;
    //书架索引
    int index;
    //缓存文件路径
    String cachePath;
    //当前阅读的路径
    String readPath;
    //本地目录
    String[] catalogs;
    //当前阅读章节号，从1开始
    int curChapter;
    //当前阅读页，从1开始
    int curPage;

    //目录页地址
    String catalogAddr;
    //当前阅读章节地址
    String readAddr;

    //本地书籍
    public MyShelfBook(int mode,String name, int index,String[] catalogs,String cachePath,String readPath, int curChapter, int curPage){
        super();
        this.mode = mode;
        this.name = name;
        this.index = index;
        this.catalogs = catalogs;
        this.cachePath = cachePath;
        this.readPath = readPath;
        this.curChapter = curChapter;
        this.curPage = curPage;
    }

    //网络书籍
    public MyShelfBook(String name, int index,String[] catalogs,String catalogAddr, String readAddr, int curChapter, int curPage){
        super();
        this.mode = 1;
        this.name = name;
        this.index = index;
        this.catalogs = catalogs;
        this.catalogAddr = catalogAddr;
        this.readAddr = readAddr;
        this.curChapter = curChapter;
        this.curPage = curPage;
    }

    @Override
    public String toString() {
        String ret = "error";
        if(mode==0){
            ret =  "new book information"+name+"\n"+catalogs+"\n"+curChapter+"\n"+curPage+"\n";
        }else if(mode==1){
            ret =  "new book information"+name+"\n"+catalogAddr+"\n"+readAddr+"\n"+curChapter+"\n"+curPage+"\n";
        }
        return ret;
    }

    public void renewReadPath(){
        String filePath = MyDataUtils.shelfSolderPath+"/BookCache/"+name+index;
        String fileName = "/"+ curChapter +".txt";
        String ret = filePath + fileName;
        readPath = ret;
    }
}
