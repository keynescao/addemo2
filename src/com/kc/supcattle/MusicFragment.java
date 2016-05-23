package com.kc.supcattle;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kc.supcattle.adpter.MusicListAdapter;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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

	private PullToRefreshListView mListView;
	private LinearLayout loadingLayout;
	private ImageView layoutImg;
	private MusicListAdapter musicAdatper;
	private LocalBroadcastManager localBroadCast;
	private BroadcastReceiver receiver;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		localBroadCast = LocalBroadcastManager.getInstance(getContext());
		receiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
				int cmd = intent.getIntExtra("cmd", 0);
				if(cmd == 0 && musicAdatper!=null){
					musicAdatper.notifyDataSetChanged();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST);
		localBroadCast.registerReceiver(receiver, filter);		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		new LoadMusicTask().execute();
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
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(musicAdatper!=null){
			musicAdatper.notifyDataSetChanged();
		}
	}
	
	private class LoadMusicTask extends AsyncTask<Void, Void, Void>{

		protected Void doInBackground(Void... params) {
			if(MusicTools.musicList.size()==0){
				MusicTools.scanMusic(getContext());
			}	
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			musicAdatper = new MusicListAdapter(MusicTools.musicList,MusicFragment.this.getContext());
			mListView.setAdapter(musicAdatper);
			mListView.setVisibility(View.VISIBLE);
			loadingLayout.setVisibility(View.GONE);
			mListView.onRefreshComplete();
			
		}
		
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		localBroadCast.unregisterReceiver(receiver);
	}

}
