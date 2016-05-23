package com.kc.supcattle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kc.supcattle.adpter.MusicListAdapter;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 音乐
 *
 */
public class MusicFragment extends Fragment {

	private List<Map<String,String>> musicList = new ArrayList<Map<String,String>>();
	private PullToRefreshListView mListView;
	private LinearLayout loadingLayout;
	private ImageView layoutImg;
	private MusicListAdapter musicAdatper;
	private Handler handler;
	
	private static boolean isRefreshList = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new MsgHandler(this);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.music_fragment, container,false);

		loadingLayout = (LinearLayout)view.findViewById(R.id.loading_layout);
		layoutImg  = (ImageView)view.findViewById(R.id.loading_img);
		layoutImg.setAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.loading));
		mListView = (PullToRefreshListView )view.findViewById(R.id.muicListView);
		mListView.setVisibility(View.GONE);
		
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Music data = (Music)parent.getItemAtPosition(position);
				Intent intent = new Intent(getActivity(),MusicPlayActivity.class);
				intent.putExtra("mid", String.valueOf(data.getId()));

				MusicTools.CURRENT_PLAY_SONG = data.getId();
				startActivity(intent);
				
			}
			
		});

		
		loadMusicTask();		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(musicAdatper!=null){
			musicAdatper.notifyDataSetInvalidated();
		}
	}

	static class MsgHandler extends Handler{
		
		WeakReference<MusicFragment> weekReference;

		public MsgHandler(MusicFragment fragment){
			this.weekReference = new WeakReference<MusicFragment>(fragment);
		}
			
		public void handleMessage(Message msg) {
			
			MusicFragment t = weekReference.get();
			if(t==null)return;
			
			t.musicAdatper = new MusicListAdapter(MusicTools.musicList,t.getContext());
			t.mListView.setAdapter(t.musicAdatper);
			t.mListView.setVisibility(View.VISIBLE);
			t.loadingLayout.setVisibility(View.GONE);
			t.mListView.onRefreshComplete();
			isRefreshList = false;
		}
		
	};


	private void loadMusicTask(){
		Log.d("====SCAN MUSIC====", Thread.currentThread().getName());
		new Thread(){
			public void run() {
				if(MusicTools.musicList.size()==0 || isRefreshList == true){
					MusicTools.scanMusic(getContext());
				}				
				musicList.clear();				
				handler.sendEmptyMessage(0);
			}
		}.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
