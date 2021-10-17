package com.example.wangpeng.huanfenzread;

import java.io.Serializable;

public class MySettings implements Serializable {
    int fontSize;
    int backColor;

    MySettings(int a,int b){
        fontSize = a;
        backColor = b;
    }
}
