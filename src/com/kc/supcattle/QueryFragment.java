package com.kc.supcattle;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * 音乐
 *
 */
public class QueryFragment extends Fragment {

	private List<Map<String,String>> musicList = new ArrayList<Map<String,String>>();
	private PullToRefreshListView mListView;
	private LinearLayout loadingLayout;
	private ImageView layoutImg;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab2_fragment, container,false);

		loadingLayout = (LinearLayout)view.findViewById(R.id.loading_layout);
		layoutImg  = (ImageView)view.findViewById(R.id.loading_img);
		layoutImg.setAnimation(AnimationUtils.loadAnimation(getContext(),R.anim.loading));
		mListView = (PullToRefreshListView )view.findViewById(R.id.muicListView);
		mListView.setVisibility(View.GONE);
		
		mListView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				Map<String,String> data = (Map<String,String>)parent.getItemAtPosition(position);
				//Toast.makeText(getContext(), data.get("name"), Toast.LENGTH_SHORT).show();
				
				Intent intent = new Intent(getActivity(),MusicPlayActivity.class);
				intent.putExtra("mid", data.get("mid"));
				startActivity(intent);
			}
			
		});
		
		ILoadingLayout  layoutlab =  mListView.getLoadingLayoutProxy(true,false);
		layoutlab.setPullLabel("下拉刷新...");
		layoutlab.setRefreshingLabel("加载中...");
		layoutlab.setReleaseLabel("松开刷新...");
		
		loadMuslic();
		
		return view;
	}

	private void loadMuslic(){
		Log.d("============","==============start=============");
		String path = getSdCardPath();
		if(!path.equals("")){
			loadMusicTask(path);
		}else{
			Toast.makeText(getContext(), "data err", Toast.LENGTH_SHORT).show();
		}
	}

	Handler handler = new Handler(){

		public void handleMessage(Message msg) {

			mListView.setAdapter(new SimpleAdapter(getContext(), musicList,R.layout.music_item,new String[]{
					"name","artist","duration"
			},new int[]{
					R.id.music_name,
					R.id.music_artist,
					R.id.music_duration
			}));
			mListView.setVisibility(View.VISIBLE);
			loadingLayout.setVisibility(View.GONE);

		}
	};


	private void loadMusicTask(final String path){
		new Thread(){
			public void run() {
				if(MusicTools.musicList.size()==0){
					MusicTools.scanMusic(getContext());
				}
				for(Music m : MusicTools.musicList){
					Map<String,String> data = new HashMap<String,String>();
					data.put("name", m.getDisplayName());
					data.put("artist", m.getArtist());
					data.put("duration", MusicTools.getDuration(m.getDuration()));
					data.put("mid", String.valueOf(m.getId()));
					musicList.add(data);
				}			
				
				handler.sendEmptyMessage(0);

			}
		}.start();
	}



	private void scanMusic(String path){
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
			/*File []list = file.listFiles();*/
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

	/**
	 * 获取SD卡根目录路径
	 *
	 * @return
	 */
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

	/**
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
	 *
	 * @return
	 */
	private boolean isSdCardExist() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
	
}
