package com.kc.supcattle.service;

import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.Visualizer;
import android.os.IBinder;
import android.util.Log;

public class MusicPlayService extends Service {
	
	private MediaPlayer mp;
	private String currentMid;
	private boolean TASK_RUN = true;
	private Visualizer  visualizer;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mp = new MediaPlayer();	
		mp.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();//开始播放
				MusicTools.CURRENT_PLAYING = 1;
			}
		});
		mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.stop();	
				MusicTools.CURRENT_PLAYING = 0;
				changeSong(1);
				
			}
		});
		mp.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				mp.reset();
				MusicTools.CURRENT_PLAYING = 0;
				return true;
			}
		});
		
		visualizer = new Visualizer(mp.getAudioSessionId());	
		visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
		visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener(){
			public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {}

			@Override
			public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
				
				byte[] model = new byte[fft.length / 2 + 1];
				model[0] = (byte) Math.abs(fft[1]);
				int j = 1;

				for (int i = 2; i < 18;) {
					model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
					i += 2;
					j++;
				}

				Intent intent = new Intent(MusicTools.MUSIC_BORDCAST_PP);
				intent.putExtra("wave", model);
				sendBroadcast(intent);	
			}
			
		}, Visualizer.getMaxCaptureRate() / 2, false, true);
		visualizer.setEnabled(true);
		
		
		new Thread(updateSeek).start();
	}
	
	private void changeSong(int flag){
		int idx = (MusicTools.getCurrentPlayIdx(currentMid) + flag);
		Music m = MusicTools.getMusic(idx);
		if(m!=null){
			currentMid = String.valueOf(m.getId());
			initPlay(m.getUrl());
			
			Intent intent = new Intent(MusicTools.MUSIC_BORDCAST);
			intent.putExtra("cmd", 0);
			intent.putExtra("mid", currentMid);
			sendBroadcast(intent);
		}		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int cmd = intent.getIntExtra("cmd", 0);
		if(cmd == 0){
			String mid = intent.getStringExtra("mid");
			if(!mid.equals(currentMid)){
				currentMid = mid;
				Music m = MusicTools.getMusic(currentMid);
				initPlay(m.getUrl());		
			}else{
				if(mp!=null){
					Intent msg = new Intent(MusicTools.MUSIC_BORDCAST);
					msg.putExtra("cmd", 1);
					msg.putExtra("seek", mp.getCurrentPosition());
					sendBroadcast(msg);
				}
			}
		}else if(cmd == 1){
			play();
		}else if(cmd == 2){
			pause();
		}else if(cmd == 3){//next
			changeSong(1);
		}else if(cmd == 4){//prev
			changeSong(-1);
		}else if(cmd == 5){//seek
			seekTo(intent.getIntExtra("seek",0));
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	
	private void seekTo(int seek){
		if(mp!=null){
			mp.seekTo(seek);
		}
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
			MusicTools.CURRENT_PLAYING = 1;
		}		
	}
	
	private void pause(){
		mp.pause();
		MusicTools.CURRENT_PLAYING = 0;
	}
	
	Runnable updateSeek = new Runnable() {
		public void run() {
			while(true){
				if(mp.isPlaying()){
					Intent msg = new Intent(MusicTools.MUSIC_BORDCAST);
					msg.putExtra("cmd", 1);
					msg.putExtra("seek", mp.getCurrentPosition());
					sendBroadcast(msg);
				}
				try {
					Thread.sleep(900);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(!TASK_RUN)break;
			}			
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d("Music Service", "distory...");
		if(mp!=null){
			if(mp.isPlaying())mp.stop();
			mp.release();
		}
		visualizer.release();
		MusicTools.CURRENT_PLAYING = 0;
		TASK_RUN = false;
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
