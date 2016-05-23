package com.kc.supcattle.wedgit;

import com.kc.supcattle.utils.MusicTools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.support.v4.content.LocalBroadcastManager;
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
    private Context mContext;
    private LocalBroadcastManager localBroadCast;
	private BroadcastReceiver receiver;
    
    public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}
    
    public VisualizerView(Context context){
        super(context);
        initView(context);
    }
    
    private void initView(Context mContext){
    	bytes = null;
        // 设置画笔的属性
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);//抗锯齿
        paint.setColor(Color.WHITE);//画笔颜色
        paint.setStyle(Style.FILL);
        
        localBroadCast = LocalBroadcastManager.getInstance(mContext);
        receiver = new BroadcastReceiver(){
    		public void onReceive(Context context, Intent intent) {
    			
    			int cmd = intent.getIntExtra("cmd", 0);
    			if(cmd == 2){
    				byte wave[] = intent.getByteArrayExtra("wave");
    				if(wave!=null){
    					updateVisualizer(wave);
    				}
    			}
    		}
    	};
    	
        IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST);
		localBroadCast.registerReceiver(receiver, filter);
        
    }

	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		localBroadCast.unregisterReceiver(receiver);
	};
	
    public void updateVisualizer(byte[] fft){
    	if(this.getVisibility() == this.VISIBLE){
    		bytes = fft;
    		invalidate();
    	}
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

}
