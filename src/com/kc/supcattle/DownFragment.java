package com.kc.supcattle;

import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.wedgit.VisualizerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *
 */
public class DownFragment extends Fragment {
	
	private VisualizerView currSelectView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicTools.MUSIC_BORDCAST_PP);
		getActivity().registerReceiver(receiver, filter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.main_tab3_fragment, container,false);
		
		currSelectView = (VisualizerView)view.findViewById(R.id.music_pp_demo);
		currSelectView.setPlaying(true);
		return view;
	}
	
	
	BroadcastReceiver receiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {			
			byte wave[] = intent.getByteArrayExtra("wave");
			if(wave!=null && currSelectView!=null){
				currSelectView.updateVisualizer(wave);
			}
		}
	};
	
	public void onDestroy() {
		super.onDestroy();
		getActivity().unregisterReceiver(receiver);
	};

}
