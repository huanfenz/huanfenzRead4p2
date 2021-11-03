package com.example.wangpeng.huanfenzread;

import java.io.Serializable;

public class MySettings implements Serializable {
    int fontSize;   // 字号
    int backColor;  // 背景颜色

    MySettings(int a,int b){
        fontSize = a;
        backColor = b;
    }
}
