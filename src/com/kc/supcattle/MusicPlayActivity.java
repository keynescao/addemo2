package com.kc.supcattle;

import com.kc.supcattle.service.MusicPlayService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MusicPlayActivity extends Activity implements OnClickListener{
	
	private Button mStartBtn,mBackBtn,mPauseBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.music_layout);	
		mStartBtn = (Button)findViewById(R.id.music_start);
		mBackBtn  = (Button)findViewById(R.id.music_back);
		mPauseBtn = (Button)findViewById(R.id.music_pause);
		mStartBtn.setOnClickListener(this);
		mPauseBtn.setOnClickListener(this);
		mBackBtn.setOnClickListener(this);
		
		initPlayService();
		
	}
	
	private void initPlayService(){
		String path = getIntent().getStringExtra("path");
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		intentmusic.putExtra("cmd", 0);
		intentmusic.putExtra("path", path);
		startService(intentmusic);		
	}
	
	@Override
	public void onClick(View v) {
		Intent intentmusic = new Intent(this,MusicPlayService.class);
		switch (v.getId()) {
			case R.id.music_start:
				intentmusic.putExtra("cmd", 1);
				startService(intentmusic);
				break;
			case R.id.music_pause:
				intentmusic.putExtra("cmd", 2);
				startService(intentmusic);
				break;
			case R.id.music_back:
				stopService(intentmusic);
				finish();
				break;
			default:
				break;
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();		
		
	}
	
	
	
	@Override
	protected void onStop() {
		super.onStop();		
	}
	
}
