package com.example.wangpeng.huanfenzread.utils.TextDisposeUtils;

import android.util.Log;

import com.example.wangpeng.huanfenzread.utils.MyDataUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.wangpeng.huanfenzread.FragShelfActivity.shelfList;
import static com.example.wangpeng.huanfenzread.utils.FileUtil.getCharset;
import static com.example.wangpeng.huanfenzread.utils.FileUtil.writeTxtToFile;

public class TxtUtil {

    public static String[] TxtDivision(String bookName,File file) {
        String filePath = MyDataUtils.shelfSolderPath+"/BookCache/"+bookName+shelfList.size();
        List<String> catalogList = new ArrayList<String>();
        try {
            // 编码格式
            String encoding = getCharset(file.toString());

            if (file.isFile() && file.exists()) { // 判断文件是否存在
                // 输入流
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                Long count = (long) 0;
                boolean bflag=false;
                int n=0;
                String newStr=null;
                String titleName=null;
                String newChapterName = null;//新章节名称
                String substring=null;
                int indexOf=0;
                int indexOf1=0;
                int line=0;
                //小说内容类
                Content content;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    content=new Content();
                    //小说名称
                    content.setName(bookName);

                    count++;
                    // 正则表达式
                    Pattern p = Pattern.compile("(^.*\\s*第)(.{1,9})[章节卷集部篇回](\\s{0,9})(.{0,30})(\\s*$)");

                    Matcher matcher = p.matcher(lineTxt);
                    Matcher matcher1 = p.matcher(lineTxt);

                    newStr=newStr+lineTxt+"\n";

                    while (matcher.find()) {
                        titleName = matcher.group();
                        //章节去空
                        newChapterName = titleName.trim();
                        //获取章节

                        content.setChapter(newChapterName);
                        catalogList.add(newChapterName);

                        indexOf1=indexOf;
                        //System.out.println(indexOf);
                        indexOf = newStr.indexOf(newChapterName);
                        // System.out.println(newChapterName + ":" + "第" + count + "行"); // 得到返回的章
                        if(bflag) {
                            bflag=false;
                            break;
                        }
                        if(n==0) {
                            indexOf1 = newStr.indexOf(newChapterName);
                        }
                        n=1;
                        bflag=true;
                        //System.out.println(chapter);
                    }
                    while(matcher1.find()) {
                        if(indexOf!=indexOf1) {
                            //根据章节数量生成
                            content.setNumber(++line);
                            content.setId(line);
                            substring = newStr.substring(indexOf1, indexOf);
                            content.setContent(substring);

                            String fileName = "/"+ content.getId() + ".txt";
                            writeTxtToFile(content.getContent(), filePath, fileName);
                        }

                    }
                }
                bufferedReader.close();
                String[] retStrs = catalogList.toArray(new String[catalogList.size()]);
                return retStrs;
            } else {
                System.out.println("找不到指定的文件");
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return null;
    }
}
