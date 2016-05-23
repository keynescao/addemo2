package com.kc.supcattle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kc.supcattle.adpter.MusicListAdapter;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;
import com.kc.supcattle.wedgit.VisualizerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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
	private VisualizerView currSelectView;
	private TextView textview;
	
	private static boolean isRefreshList = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new MsgHandler(this);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST_PP);
		getActivity().registerReceiver(receiver, filter);
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
				
				data.setPlaying(true);
				musicAdatper.notifyDataSetChanged();
				if(currSelectView!=null)currSelectView.updateVisualizer(null);
				currSelectView = (VisualizerView)view.findViewById(R.id.music_pp);
				MusicTools.CURRENT_PLAY_SONG = data.getId();
				startActivity(intent);
				
			}
			
		});
		
		/*mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>(){

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				//isRefreshList = true;
				//loadMusicTask();				
			}
		});
		
		ILoadingLayout  layoutlab =  mListView.getLoadingLayoutProxy(true,false);
		layoutlab.setPullLabel("下拉刷新...");
		layoutlab.setRefreshingLabel("加载中...");
		layoutlab.setReleaseLabel("松开刷新...");*/
		
		loadMusicTask();
		
		return view;
	}
	
	
	BroadcastReceiver receiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {			
			byte wave[] = intent.getByteArrayExtra("wave");
			if(wave!=null && currSelectView!=null){
				currSelectView.updateVisualizer(wave);
				Log.d("TAG", "============"+MusicTools.CURRENT_PLAY_SONG+"============"+currSelectView.isPlaying());
			}
		}
	};

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
		getActivity().unregisterReceiver(receiver);
	}

	/*private void scanMusic(String path){
		File file = new File(path);

		Log.d("==========",">>>>>>>>>>>>>>>>>>"+file.exists()+"=============="+file.canRead());
		if(file.isDirectory()){
			File []list = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {

					if(pathname.isDirectory()){
						return true;
					}else{
						String fileName = pathname.getName().toLowerCase();
						if(fileName.endsWith(".mp3")){
							return true;
						}
						return false;
					}
				}
			});
			File []list = file.listFiles();
			if(list!=null && list.length>0) {
				for (File f : list) {
					scanMusic(f.getAbsolutePath());
				}
			}
		}else{
			Map<String,String> data = new HashMap<String,String>();
			data.put("name", file.getName());
			data.put("path", file.getAbsolutePath());
			musicList.add(data);
		}

	}

	*//**
	 * 获取SD卡根目录路径
	 *
	 * @return
	 *//*
	private String getSdCardPath() {
		boolean exist = isSdCardExist();
		String sdpath = "";
		if (exist) {
			sdpath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			sdpath = "";
		}
		return sdpath;

	}

	*//**
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
	 *
	 * @return
	 *//*
	private boolean isSdCardExist() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}*/
	
}
