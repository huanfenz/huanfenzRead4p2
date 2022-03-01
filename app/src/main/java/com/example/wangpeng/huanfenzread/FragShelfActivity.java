package com.example.wangpeng.huanfenzread;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wangpeng.huanfenzread.utils.FileUtil;
import com.example.wangpeng.huanfenzread.utils.MyDataUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.example.wangpeng.huanfenzread.utils.FileUtil.deleteFile;
import static com.example.wangpeng.huanfenzread.utils.FileUtil.isHaveFile;
import static com.example.wangpeng.huanfenzread.utils.FileUtil.writeTxtToFile;
import static com.example.wangpeng.huanfenzread.utils.MyTextDisposeUtils.mySubstring2;
import static com.example.wangpeng.huanfenzread.utils.TextDisposeUtils.TxtUtil.TxtDivision;

public class FragShelfActivity extends Fragment {

    private  View view;
    private GridView gridView;
    private TextView tv_sx;

    private AlertDialog alertDialog1; //信息框
    public static List<MyShelfBook> shelfList;
    public static List<Bitmap> bmList;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //frag
        view = inflater.inflate(R.layout.activity_frag_shelf, container, false);

        gridView = (GridView)view.findViewById(R.id.dashboard_gv);
        tv_sx = (TextView)view.findViewById(R.id.tv_shelf_title);

        //获取书架信息和bm
        MyDataUtils.getShelfInformation();
        MyDataUtils.getBmList();

        if(shelfList == null){
            shelfList = new ArrayList<MyShelfBook>();
            bmList = new ArrayList<Bitmap>();
        }else if(shelfList!=null && bmList==null){
            bmList = new ArrayList<Bitmap>();
            for(int i=0;i<shelfList.size();i++){
                Resources res = getResources();
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.beijing);
                bmList.add(bmp);
            }
        }else if(shelfList!=null && bmList!=null && shelfList.size() != bmList.size()){
            for(int i=0;i< shelfList.size()-bmList.size() ;i++){
                Resources res = getResources();
                Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.beijing);
                bmList.add(bmp);
            }
        }

        tv_sx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gridViewEvent();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        gridViewEvent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case Activity.RESULT_OK://该结果码与FragmentActivity中是保持一致的
                //在这里获取你需要的数据
                //获取文件管理器返回的路径
                Uri uri = data.getData();//得到uri，后面就是将uri转化成file的过程。
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor actualimagecursor = getActivity().getContentResolver().query(uri,proj,null,null,null);
                int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                actualimagecursor.moveToFirst();
                String img_path = actualimagecursor.getString(actual_image_column_index);
                File file = new File(img_path);
                Log.d("TestFile",file+"");

                //获取文本
//                String charset = FileUtil.getCharset(file.getAbsolutePath());
//                String content = FileUtil.getFileOutputString(file.getAbsolutePath(),charset);
//                Log.d("TestFile",content.length()+"");

                //写文本
//                String filePath = MyDataUtils.shelfSolderPath+"/testbook";
//                String fileName = "/data.txt";
//                writeTxtToFile("huanfenzRead", filePath, fileName);

                String bookname = mySubstring2(file.toString(),"/",".txt");
                String[] catalogs = TxtDivision(bookname,file);
                //添加一个书架
                String cachePath = MyDataUtils.shelfSolderPath+"/BookCache/"+bookname+shelfList.size();
                String fileName = cachePath + "/"+ 1 +".txt";

                MyShelfBook sb = new MyShelfBook(0,bookname,shelfList.size(),catalogs,cachePath,fileName,1,1);
                FragShelfActivity.shelfList.add(sb);
                //得到默认图的bitmap
                Resources res = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.bendishuji);
                //添加到书架图片列表
                FragShelfActivity.bmList.add(bitmap);
                Toast.makeText(getActivity(),"添加成功",Toast.LENGTH_SHORT).show();
                gridViewEvent();

                break;

        }
    }

    private void gridViewEvent() {

        ArrayList<HashMap<String,Object>> list=new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map=null;

        Log.d("shelfList information",shelfList.size()+"\n");

        for(int i=0;i<shelfList.size();i++)
        {
            map=new HashMap<String,Object>();
            //放置
            map.put("ItemImage",bmList.get(i));
            map.put("ItemText",shelfList.get(i).name);
            list.add(map);
        }
        //从资源里找图片转成bitmap
        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, R.drawable.addpic);

        //增加“添加图书”图标和文字到map
        map=new HashMap<String,Object>();
        map.put("ItemImage",bmp);
        map.put("ItemText","添加本地书籍");
        list.add(map);

        SimpleAdapter adapter = new SimpleAdapter(getActivity(),list,R.layout.dashboard_grid_view_item,new String[]{"ItemImage","ItemText"},new int[]{R.id.dashboard_item_image,R.id.dashboard_item_title});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder(){

            public boolean setViewValue(View view, Object data,
                                        String textRepresentation) {
                //判断是否为我们要处理的对象
                if(view instanceof ImageView && data instanceof Bitmap){
                    ImageView iv = (ImageView) view;
                    iv.setImageBitmap((Bitmap) data);
                    return true;
                }else
                    return false;
            }
        });

        gridView.setAdapter(adapter);

        //添加消息处理
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //position是下标
                        //下标等于书籍数，说明是添加书籍
                        if(position == shelfList.size()){
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/txt");//设置类型，这里设置后缀为txt。
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent,1);
                        }else{
                            Intent intent = new Intent(getActivity(),ReadActivity.class);
                            intent.putExtra("shelf_index",position);
                            intent.putExtra("shelf_mode",shelfList.get(position).mode);
                            Log.d("TestFile","mode:"+shelfList.get(position).mode);
                            startActivity(intent);
                        }
                    }
                }, 0);
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //showNormalDialog(i);
                showList(i);
                return true;
            }
        });
    }

    private void showNormalDialog(final int index){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(getActivity());
        normalDialog.setIcon(R.drawable.beijing);
        normalDialog.setTitle("考虑好了吗");
        normalDialog.setMessage("你确定要删除该书籍吗?");
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        MyShelfBook msb = shelfList.get(index);
                        if(msb.mode == 0 || msb.mode == 1){
                            //如果有缓存路径，那么删除,并且删除文件夹
//                            if(isHaveFile(msb.cachePath)){
//                                deleteFile(new File(msb.cachePath),true);
//                            }
                        }
                        shelfList.remove(index);
                        bmList.remove(index);
                        gridViewEvent();
                        MyDataUtils.saveShelfInformation();
                        MyDataUtils.saveShelfAllPic();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    public void showList(final int index){
        final String[] items = {"添加/修改封面", "从书架中删除书籍"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle("请选择操作");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 1){
                    showNormalDialog(index);
                }
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

}