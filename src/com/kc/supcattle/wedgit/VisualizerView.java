package com.kc.supcattle.wedgit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author keynes
 * 频谱
 */
public class VisualizerView extends View {

	// bytes数组保存了波形抽样点的值
    private byte[] bytes;
    private Paint paint = new Paint();
    private Rect rect = new Rect();
    private float []mPoints;
    private int pointNum = 9;
    private boolean playing = false;
    
    public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView();
	}
    
    public VisualizerView(Context context){
        super(context);
        initView();
    }
    
    private void initView(){
    	bytes = null;
        // 设置画笔的属性
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);//抗锯齿
        paint.setColor(Color.WHITE);//画笔颜色
        paint.setStyle(Style.FILL);
    }

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	};
	
    public void updateVisualizer(byte[] fft){
    	if(playing){
	        bytes = fft;  
	        // 通知该组件重绘自己。
    	}else{
    		bytes = null;
    	}
    	invalidate();
    }
   
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if (bytes == null){
            return;
        }
        // 绘制白色背景
       
        //if (mPoints == null || mPoints.length <= bytes.length * 4) {
        	
            mPoints = new float[bytes.length * 4];
            rect.set(0, 0, getWidth(), getHeight());

            final int baseX = rect.width()/pointNum;  
            final int height = rect.height();  
  
            for (int i = 0; i < pointNum ; i++) {  
                if (bytes[i] < 0) {  
                    bytes[i] = 127;  
                }  
                  
                final int xi = baseX*i + baseX/2;  
                mPoints[i * 4] = xi;  
                mPoints[i * 4 + 1] = height;  
                mPoints[i * 4 + 2] = xi;  
                mPoints[i * 4 + 3] = height - bytes[i];  
            }  
            
            
            canvas.drawLines(mPoints, paint);
            
        //}   
    }

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}
    
    
    
}
