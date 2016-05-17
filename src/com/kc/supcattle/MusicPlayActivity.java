package com.kc.supcattle;

import com.kc.supcattle.service.MusicPlayService;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MusicPlayActivity extends Activity implements OnClickListener{
	
	private ImageView mPlayBtn,mNextBtn,mPrevBtn;
	private ImageView musicImg;
	private TextView  musicTitle,musicLte,musicRte;
	private SeekBar   musicSeekBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_layout);	
		mPlayBtn = (ImageView)findViewById(R.id.music_play);
		mNextBtn  = (ImageView)findViewById(R.id.music_next);
		mPrevBtn = (ImageView)findViewById(R.id.music_prev);
		musicImg  = (ImageView)findViewById(R.id.music_img);
		musicTitle = (TextView)findViewById(R.id.music_title);
		musicLte = (TextView)findViewById(R.id.music_lte);
		musicRte = (TextView)findViewById(R.id.music_rte);
		musicSeekBar = (SeekBar)findViewById(R.id.music_seekbar);

		mPlayBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mPrevBtn.setOnClickListener(this);
		
		
		musicSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				Intent intentmusic = new Intent(MusicPlayActivity.this,MusicPlayService.class);
				intentmusic.putExtra("cmd", 5);
				intentmusic.putExtra("seek", seekBar.getProgress());
				startService(intentmusic);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				musicLte.setText(MusicTools.getDuration(progress));		
			}
		});
		
		handler.postDelayed(updateAction,1000);
				
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST);
		registerReceiver(receiver, filter);
		
		initPlayService();
		
	}
	
	Runnable updateAction = new Runnable(){
		@Override
		public void run() {
			handler.sendEmptyMessage(0);			
		}
	};
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			int progress = musicSeekBar.getProgress() + 1000;
			if(musicSeekBar.getMax() < progress){
				progress = musicSeekBar.getMax();
				musicSeekBar.setProgress(progress);
			}else{
				musicSeekBar.setProgress(progress);
				handler.postDelayed(updateAction, 1000);
			}
			musicLte.setText(MusicTools.getDuration(progress));
		};
	};
	
	private void initPlayService(){
		
		String mid = getIntent().getStringExtra("mid");
		changeSongDetail(mid);
		
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		intentmusic.putExtra("cmd", 0);
		intentmusic.putExtra("mid", mid);

		startService(intentmusic);		
	}
	
	BroadcastReceiver receiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			int cmd = intent.getIntExtra("cmd", 0);
			if(cmd == 0){
				changeSongDetail(intent.getStringExtra("mid"));
			}else if(cmd == 1){
				int progress = intent.getIntExtra("seek", 0);
				musicLte.setText(MusicTools.getDuration(progress));
				musicSeekBar.setProgress(progress);
			}
		}
	};
	
	
	private void changeSongDetail(String mid){
		Music music = MusicTools.getMusic(mid);
		Bitmap bitmap = MusicTools.getAlbumBitmap(this,music.getAlbumId());
		if(bitmap !=null){
			musicImg.setImageBitmap(bitmap);
		}else{
			musicImg.setImageResource(R.drawable.m3img);
		}
		musicTitle.setText(music.getTitle());
		musicSeekBar.setMax(music.getDuration());
		musicSeekBar.incrementProgressBy(music.getDuration()/1000);
		musicSeekBar.setProgress(0);
		
		musicLte.setText("00:00");
		musicRte.setText(MusicTools.getDuration(music.getDuration()));
	}
	
	private void play(){
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		if(MusicTools.CURRENT_PLAYING == 0){
			mPlayBtn.setImageResource(R.drawable.ic_pause);
			intentmusic.putExtra("cmd", 1);
		}else if(MusicTools.CURRENT_PLAYING == 1){
			mPlayBtn.setImageResource(R.drawable.ic_start);
			intentmusic.putExtra("cmd", 2);
		}
		startService(intentmusic);
	}
	
	private void changeSong(int flag){
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		intentmusic.putExtra("cmd", flag);
		startService(intentmusic);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.music_play:
				play();
				break;
			case R.id.music_next:
				changeSong(3);
				break;
			case R.id.music_prev:
				changeSong(4);
				break;
			default:
				break;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
		if(MusicTools.CURRENT_PLAYING == 0){
			Intent intentmusic = new Intent(this,MusicPlayService.class);
			stopService(intentmusic);
		}
	}
}
