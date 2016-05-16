package com.kc.supcattle;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * 音乐
 *
 */
public class QueryFragment extends Fragment {

	private TextView tipsTxt;
	private List<String> musicList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab2_fragment, container,false);

		tipsTxt = (TextView)view.findViewById(R.id.query_txt);


		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("============","==============start=============");
		String path = getSdCardPath();
		if(!path.equals("")){
			loadMusicTask(path);
		}else{
			tipsTxt.setText("路径错误");
		}
	}


	Handler handler = new Handler(){

		public void handleMessage(Message msg) {

			StringBuilder builder = new StringBuilder();
			for(String str : musicList){
				builder.append(str);
				builder.append("\r\n");
			}
			tipsTxt.setText(builder.toString());

		}
	};


	private void loadMusicTask(final String path){
		new Thread(){
			public void run() {
				scanMusic(path);
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
					Log.d("==========",">>>>>>>>>>55555555>>>>>>>>"+f.canRead());
					scanMusic(f.getAbsolutePath());
				}
			}
		}else{
			musicList.add(file.getName());
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
