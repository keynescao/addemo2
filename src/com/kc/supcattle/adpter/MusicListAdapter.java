package com.kc.supcattle.adpter;

import java.util.List;

import com.kc.supcattle.R;
import com.kc.supcattle.utils.MusicTools;
import com.kc.supcattle.vo.Music;
import com.kc.supcattle.wedgit.VisualizerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter {

	private List<Music> data;
    private LayoutInflater layoutInflater;
    
    
    public MusicListAdapter(List<Music> list, Context mContext){
        this.data = list;
        this.layoutInflater = LayoutInflater.from(mContext);
    }

	
	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		HoldTag tag;
        if(convertView == null){
            tag = new HoldTag();
            convertView = layoutInflater.inflate(R.layout.music_item,null);
            tag.musicName = (TextView)convertView.findViewById(R.id.music_name);
            tag.artist = (TextView)convertView.findViewById(R.id.music_artist);
            tag.druation = (TextView)convertView.findViewById(R.id.music_duration);
            tag.visua = (VisualizerView)convertView.findViewById(R.id.music_pp);
            convertView.setTag(tag);
        }else{
            tag = (HoldTag)convertView.getTag();
        }
        Music m = (Music)getItem(position);
        tag.musicName.setText(m.getTitle());
        tag.artist.setText(m.getArtist());
        tag.druation.setText(MusicTools.getDuration(m.getDuration()));
        if(MusicTools.CURRENT_PLAY_SONG == m.getId()){
        	tag.visua.setVisibility(View.VISIBLE);
        }else{
        	tag.visua.setVisibility(View.GONE);
        }
		return convertView;
	}

	
	class HoldTag{
		
		TextView musicName;
		TextView artist;
		TextView druation;
		VisualizerView visua;
		
	}
}
