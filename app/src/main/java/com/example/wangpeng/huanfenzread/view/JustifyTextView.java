package com.example.wangpeng.huanfenzread.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ccheng
 * @Date 3/18/14
 */
public class JustifyTextView extends android.support.v7.widget.AppCompatTextView {

    private int mLineY;
    private int mViewWidth;
    private int mTextHeight;
    public static final String TWO_CHINESE_BLANK = "  ";

    public JustifyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mViewWidth = getMeasuredWidth();
        String text = getText().toString();
        mLineY = 0;
        mLineY += getTextSize();
        Layout layout = getLayout();

        // layout.getLayout()在4.4.3出现NullPointerException
        if (layout == null) {
            return;
        }

        Paint.FontMetrics fm = paint.getFontMetrics();

        double textHeightF = Math.ceil(fm.descent - fm.ascent);
        int textHeight = (int) (textHeightF * layout.getSpacingMultiplier() + layout
                .getSpacingAdd());
        mTextHeight = textHeight;
        //对需要缩放的规则进行了更改
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            float width = StaticLayout.getDesiredWidth(text, lineStart,
                    lineEnd, getPaint());
            String line = text.substring(lineStart, lineEnd);

            //如果需要缩放，进行缩放，否则直接绘制
//            if (needScale(line)) {
//                drawScaledText(canvas, lineStart, line, width);
//            } else {
//                canvas.drawText(line, 0, mLineY, paint);
//            }
            // 老子不缩放了
            canvas.drawText(line, 0, mLineY, paint);

            mLineY += textHeight;
        }
    }

    public int getmTextHeight(){
        return mTextHeight;
    }

    //是否是结束标点符号
    private boolean isEndPunctuation(String str){
        String[] strs = {
                ".","?","!","\'","\"","…",
                "。","？","！","’","”",
        };
        for (String cur : strs) {
            if(str.equals(cur) == true){
                return true;
            }
        }
        return false;
    }

    private void drawScaledText(Canvas canvas, int lineStart, String line,
                                float lineWidth) {
        float x = 0;
        if (isFirstLineOfParagraph(lineStart, line)) {

            //正则匹配，将开头的N个空格放入blanks
            Pattern p = Pattern.compile("\\u0020+");
            Matcher m = p.matcher(line);
            String blanks = "\t\t\t\t";
            if(m.find()){
                blanks = m.group(0);
            }
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(blanks.length());
        }

        int gapCount = line.length() - 1;
        int i = 0;
        if (line.length() > 2 && line.charAt(0) == 12288
                && line.charAt(1) == 12288) {
            String substring = line.substring(0, 2);
            float cw = StaticLayout.getDesiredWidth(substring, getPaint());
            canvas.drawText(substring, x, mLineY, getPaint());
            x += cw;
            i += 2;
        }

        float d = (mViewWidth - lineWidth) / gapCount;
        for (; i < line.length(); i++) {
            String c = String.valueOf(line.charAt(i));
            float cw = StaticLayout.getDesiredWidth(c, getPaint());
            canvas.drawText(c, x, mLineY, getPaint());
            x += cw + d;
        }
    }

    //是否是段落的第一行，规则修改
    private boolean isFirstLineOfParagraph(int lineStart, String line) {
        if(line==null || line.length()==0)return false;
        String startStr = line.substring(0,1);
        boolean startFlag = startStr.equals("\u0020");
        boolean isFirst = line.length()>=2 && startFlag;
        return isFirst;
    }

    /*
    * 是否需要缩放
    * 字符串为空，不需要
    * 最后一个字符为"\n"，不需要
    * 最后一个字符为  标志结束的  标点符号，不需要
    * 其他情况返回需要
    * */
    private boolean needScale(String line) {
        //字符为空，返回false
        if (line == null || line.length() == 0) {
            return false;
        }
        //获得最后一个字符，可能是全角或者半角
        String endStr = line.substring(line.length()-1);
        //如果最后一个是换行符，返回false
        if(endStr.equals("\n") == true){
            return false;
        }
        //如果最后一个是结束标点，那么返回false
        if(isEndPunctuation(endStr) == true){
            return false;
        }
        return true;
    }

}