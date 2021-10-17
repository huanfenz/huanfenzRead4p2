package com.example.wangpeng.huanfenzread.utils;
import android.content.Context; import android.widget.Toast;

public class ToastUtil {
    private static Toast myToast;
    public static void toast(Context context, String text){
        if (myToast != null) {
            myToast.cancel();
            myToast=Toast.makeText(context,text,Toast.LENGTH_SHORT);
        }else{
            myToast=Toast.makeText(context,text,Toast.LENGTH_SHORT);
        }
        myToast.show();
    }

    public static void cancel(){
        myToast.cancel();
    }
}
