package com.kc.supcattle.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kc.supcattle.vo.Music;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MusicTools {
	
	public static List<Music> musicList = new ArrayList<Music>();
	private static Map<String,String> idxMap = new HashMap<String,String>();
	
	public static int CURRENT_PLAYING = 0;//0 未播放，1播放
	public final static String MUSIC_BORDCAST = "music.bordercast";
	
	
	public static void scanMusic(Context mContext){
		
		Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{
				MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.DISPLAY_NAME,
				MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.SIZE
		}, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		
		if(cursor!=null && cursor.moveToFirst()){
			musicList.clear();
			int index = 0;
			try{
				do{
					Music music = new Music();
					int 	id 		= 	cursor.getInt(0);
					music.setId(id);
					
					String 	title	= 	cursor.getString(1);
					music.setTitle(title);
					
					String 	album   = 	cursor.getString(2);
					music.setAlbum(album);
					
					String 	albumId = 	cursor.getString(3);
					music.setAlbumId(albumId);
					
					String 	artist	= 	cursor.getString(4);
					music.setArtist(artist);
					
					String 	url		= 	cursor.getString(5);
					music.setUrl(url);
					
					String 	name	= 	cursor.getString(6);
					music.setDisplayName(name);
					
					int		duration=	cursor.getInt(7);
					music.setDuration(duration);
					
					int		size	=	cursor.getInt(8);
					music.setSize(size);
					
					Log.d("MUSIC_LIST", music.toString());
					
					musicList.add(music);
					idxMap.put(Integer.toString(id), Integer.toString(index++));
					
				}while(cursor.moveToNext());
			}catch(Exception ex){
				ex.printStackTrace();
			}finally{
				cursor.close();
			}
		}
		
	}
	
	public static Music getMusic(String musicId){
		return musicList.get(Integer.parseInt(idxMap.get(musicId)));
	}
	
	public static Music getMusic(int idx){
		if((idx +1 ) < musicList.size()){
			return musicList.get(idx);
		}
		return null;
	}
	
	public static int getCurrentPlayIdx(String musicId) {
		return Integer.parseInt(idxMap.get(musicId));
	}

	public static String getDuration(int duration){
		float sec = duration/1000;
		int minute = (int)(sec/60);
		int mill = (int)(sec%60);
		return (minute < 10 ? ("0" + minute) : minute) +":" + (mill < 10 ? ("0" + mill) : mill);
	}
	
	
	private static String getAlbumArt(Context mContext,String albumId){

		String albumArt = null;
		Uri uri = Uri.parse("content://media/external/audio/albums".concat("/").concat(albumId));
		Cursor cur = mContext.getContentResolver().query(uri, new String[]{
				MediaStore.Audio.Albums.ALBUM_ART
		}, null, null, null);		
		try{
			if(cur!=null && cur.getCount()>0 && cur.getColumnCount()>0){
				cur.moveToFirst();
				albumArt = cur.getString(0);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			cur.close();
		}		
		return albumArt;
	}
	
	public static Bitmap getAlbumBitmap(Context mContext,String albumId){		
		Bitmap bitmap = null;		
		String albumArt = getAlbumArt(mContext,albumId);
		if(albumArt == null){
			
		}else{
			bitmap = BitmapFactory.decodeFile(albumArt);
		}
		return bitmap;
	}
	

}
