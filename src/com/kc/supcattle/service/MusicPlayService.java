package com.kc.supcattle.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.IBinder;
import android.util.Log;

public class MusicPlayService extends Service {
	
	private MediaPlayer mp;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mp = new MediaPlayer();	
		mp.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();//开始播放
			}
		});
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();
			}
		});
		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.reset();
				return true;
			}
		});
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int cmd = intent.getIntExtra("cmd", 0);
		if(cmd == 0){
			String cmdval = intent.getStringExtra("path");
			Log.d("PlayServer", "==========================="+ cmdval +"===============================");
			initPlay(cmdval);
			
		}else if(cmd == 1){
			play();
		}else if(cmd == 2){
			pause();
		}
		
		return super.onStartCommand(intent, flags, startId);
	}
	
	private void initPlay(String path){
		try{
			if(mp!=null){
				mp.reset();
				mp.setDataSource(path);
				mp.prepare();
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	private void play(){
		
		if(!mp.isPlaying()){
			mp.start();
		}		
	}
	
	private void pause(){
		mp.pause();
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Music Service", "distory...");
		if(mp!=null){
			mp.release();
		}
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
