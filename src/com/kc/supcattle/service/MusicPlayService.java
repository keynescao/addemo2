package com.kc.supcattle.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MusicPlayService extends Service {
	
	private MediaPlayer mp;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mp = new MediaPlayer();		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int cmd = intent.getIntExtra("cmd", 0);
		if(cmd == 0){
			String cmdval = intent.getStringExtra("path");
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
				mp.setDataSource(path);
				mp.prepare();
				mp.start();
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
