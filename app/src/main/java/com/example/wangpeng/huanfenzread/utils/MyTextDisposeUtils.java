package com.example.wangpeng.huanfenzread.utils;

public class MyTextDisposeUtils {

    //取字符串中间，从前面开始取
    public static String mySubstring(String sourceStr,String startStr,String endStr){
        int startIndex = sourceStr.indexOf(startStr) + startStr.length();
        int endIndex = sourceStr.indexOf(endStr,startIndex);
        return sourceStr.substring(startIndex,endIndex);
    }

    //取字符中间，需要源字符串起始位置
    public static String myStartSubstring(String sourceStr,int start,String startStr,String endStr){
        int startIndex = sourceStr.indexOf(startStr,start) + startStr.length();
        int endIndex = sourceStr.indexOf(endStr,startIndex);
        return sourceStr.substring(startIndex,endIndex);
    }

    //取字符中间，从后面开始取
    public static String mySubstring2(String sourceStr,String startStr,String endStr){
        //从最后找endStr
        int endIndex = sourceStr.lastIndexOf(endStr);
        //从endStr向前找startStr
        int startIndex = sourceStr.lastIndexOf(startStr,endIndex)+startStr.length();
        return sourceStr.substring(startIndex,endIndex);
    }

}
