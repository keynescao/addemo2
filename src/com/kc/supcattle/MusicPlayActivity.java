package com.kc.supcattle;

import java.lang.ref.WeakReference;

import com.kc.supcattle.service.MusicPlayService;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.utils.StringUtil;
import com.kc.supcattle.vo.Music;
import com.kc.supcattle.wedgit.LrcView;
import com.lidroid.xutils.BitmapUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author keynes
 * 播放音乐
 */
public class MusicPlayActivity extends Activity implements OnClickListener{
	
	private ImageView mPlayBtn,mNextBtn,mPrevBtn;
	private TextView  musicTitle,musicLte,musicRte;
	private SeekBar   musicSeekBar;
	private LinearLayout playback;
	private BitmapUtils bitmapUtil;
	private LrcView mLrc;
	private Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_layout);
		
		handler = new PlayHandler(this);
		
		mPlayBtn = (ImageView)findViewById(R.id.music_play);
		mNextBtn  = (ImageView)findViewById(R.id.music_next);
		mPrevBtn = (ImageView)findViewById(R.id.music_prev);
		musicTitle = (TextView)findViewById(R.id.music_title);
		musicLte = (TextView)findViewById(R.id.music_lte);
		musicRte = (TextView)findViewById(R.id.music_rte);
		musicSeekBar = (SeekBar)findViewById(R.id.music_seekbar);
		playback = (LinearLayout)findViewById(R.id.playback);
		mLrc = (LrcView)findViewById(R.id.music_lrc);
		
		
		mPlayBtn.setOnClickListener(this);
		mNextBtn.setOnClickListener(this);
		mPrevBtn.setOnClickListener(this);
		
		bitmapUtil = new BitmapUtils(this);
		bitmapUtil.configDefaultLoadingImage(R.drawable.bg);
		bitmapUtil.configDefaultLoadFailedImage(R.drawable.bg);
		
		Log.d("PALY_TAG", "=================================PALY_CREATE====================================");
		
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
				mLrc.changeCurrent(progress);
			}
		});
					
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST);
		registerReceiver(receiver, filter);		
		
		initPlayService();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.d("TEST","TEST");
		
	}
	
	static class PlayHandler extends Handler{
		
		WeakReference<MusicPlayActivity> weakRefrence;
		
		public PlayHandler(MusicPlayActivity activity){
			this.weakRefrence = new WeakReference<MusicPlayActivity>(activity);
		}

		public void handleMessage(android.os.Message msg) {
			
			MusicPlayActivity act = this.weakRefrence.get();
			if(act == null)return;
			
			if(msg.what == 1){
				Log.d("PALY_MUSIC","==============="+ msg.obj);
				String path = String.valueOf(msg.obj);
				if(!path.equals(StringUtil.ERR_FLAG)){
					Bitmap bitmap = BitmapFactory.decodeFile(String.valueOf(msg.obj));
					act.playback.setBackground(new BitmapDrawable(act.getResources(),bitmap));
				}else{
					act.playback.setBackgroundResource(R.drawable.bg);
				}
			}else if(msg.what == 2){
				String path = String.valueOf(msg.obj);
				if(!path.equals(StringUtil.ERR_FLAG)){
					try {
						Log.d("PALY_MUSIC_LRC","==============="+ msg.obj);
						act.mLrc.setLrcPath(String.valueOf(msg.obj));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}else{
					Toast.makeText(act, "未找到合适的歌词", Toast.LENGTH_SHORT).show();
				}
			}
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
				mLrc.changeCurrent(progress);
			}
		}
	};
	
	private void changeSong(int flag){
		mPlayBtn.setImageResource(R.drawable.ic_pause);
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		intentmusic.putExtra("cmd", flag);
		startService(intentmusic);
	}
	

	private void changeSongDetail(String mid){
		
		Music music = MusicTools.getMusic(mid);
		mLrc.clearLrcText();
		musicTitle.setText(music.getTitle());
		musicSeekBar.setMax(music.getDuration());
		musicSeekBar.incrementProgressBy(music.getDuration()/1000);
		musicSeekBar.setProgress(0);

		musicLte.setText("00:00");
		musicRte.setText(MusicTools.getDuration(music.getDuration()));
		
		//先读取内置图片，读不到则从网络取
		Bitmap bitmap = MusicTools.getAlbumBitmap(this,music.getAlbumId());
 		if(bitmap !=null){
			playback.setBackground(new BitmapDrawable(getResources(),bitmap));;
 		}else{
 			MusicTools.displayAlbumAndLrc(music, handler);
 		}
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
