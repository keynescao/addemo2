package com.kc.supcattle.utils;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kc.supcattle.vo.Music;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

public class MusicTools {
	
	public static List<Music> musicList = new ArrayList<Music>();
	private static Map<String,String> idxMap = new HashMap<String,String>();
	
	public static int CURRENT_PLAYING = 0;//0 未播放，1播放
	public final static String MUSIC_BORDCAST = "music.bordercast";
	
	private final static String MUSIC_NET_GET_URL = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.search.common&format=json&page_no=1&page_size=1";
	
	private final static String MUSIC_GET_SONG_DETAIL = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=qianqian&version=2.1.0&method=baidu.ting.song.getInfos&format=json&ts=1408284347323&e=JoN56kTXnnbEpd9MVczkYJCSx%2FE1mkLx%2BPMIkTcOEu4%3D&nw=2&ucf=1&res=1";
	private final static String MUSIC_SOURCE_NET = "http://musicdata.baidu.com";
	private final static String MUSIC_LRC_DIR = "/supcattle/lrc";
	private final static String MUSIC_ALBUM_DIR = "/supcattle/album";
	
	
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
			idxMap.clear();
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
		int len = musicList.size();
		if((idx +1 ) > len){			
			idx = 0;
		}else if(idx < 0){
			idx = len - 1;
		}
		return musicList.get(idx);
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
	
	public static void displayAlbumAndLrc(final Music m,final Handler handler){
		
		boolean picExists = false,lrcExists = false;
		
		String fileName = m.getHashCode() + ".jpg";	
		String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath().concat(MUSIC_ALBUM_DIR);
		String picPath = rootDir.concat("/").concat(fileName);
		File file = new File(picPath);
		if(file.exists()){
			handler.obtainMessage(1, picPath).sendToTarget();
			picExists = true;
		}
		
		fileName = m.getHashCode()+".lrc";			
		rootDir = Environment.getExternalStorageDirectory().getAbsolutePath().concat(MUSIC_LRC_DIR);
		String lrcPath = rootDir.concat("/").concat(fileName);
		file = new File(lrcPath);
		if(file.exists()){
			handler.obtainMessage(2, lrcPath).sendToTarget();
			lrcExists = true;
		}
		if(!picExists || !lrcExists){
			downloadNetMusicDet(m,lrcPath,picPath,lrcExists,picExists,handler);
		}
	}

	
	private static void downloadNetMusicDet(Music m,final String lrcTarget,final String picTarget,final boolean lrcExists,
			final boolean picExists,final Handler handler){
		HttpUtils http = new HttpUtils();
		try{
			String url = MUSIC_NET_GET_URL.concat("&query=") + URLEncoder.encode(m.getTitle(),"UTF-8");
			Log.d("MUSIC_GET_NET_DETAIL", "=============="+url);
			http.send(HttpMethod.GET, url, new RequestCallBack<String>() {
				public void onSuccess(ResponseInfo<String> responseInfo) {
					String result = responseInfo.result;				
					try{
						JSONObject json = new JSONObject(result);
						JSONArray songlist = json.getJSONArray("song_list");						
						if(!lrcExists){
							String lrcUrl = songlist.getJSONObject(0).getString("lrclink");
							if(!StringUtil.isEmpty(lrcUrl)){
								downloadMusicFile(lrcUrl,lrcTarget,2,handler);
							}else{
								handler.obtainMessage(2, "err").sendToTarget();
							}
						}						
						//Pic
						if(!picExists){
							String songid = songlist.getJSONObject(0).getString("song_id");
							Log.d("MUSIC_GET_SONGID", "========================="+ songid +"==========================");
							if(!StringUtil.isEmpty(songid)){
								getSongAlbumPic(songid,picTarget,handler);
							}else{
								handler.obtainMessage(1, "err").sendToTarget();
							}
						}
					}catch(Exception ex){
						if(!lrcExists)handler.obtainMessage(2, "err").sendToTarget();						
						if(!picExists)handler.obtainMessage(1, "err").sendToTarget();
						Log.e("MUSIC_GET_SONGID", ex +"==========================");
					}
				}
				public void onFailure(HttpException err, String msg) {
					if(!lrcExists)handler.obtainMessage(2, "err").sendToTarget();						
					if(!picExists)handler.obtainMessage(1, "err").sendToTarget();
					Log.d("MUSIC_GET_SONG_DETAIL", "==============GET SONG_INFO FAILED==========="+ msg +"==========================");
				}			
			});
		}catch(Exception ex){
			if(!lrcExists)handler.obtainMessage(2, "err").sendToTarget();						
			if(!picExists)handler.obtainMessage(1, "err").sendToTarget();
		}
	}
	
	
	private static void getSongAlbumPic(String songId,final String target,final Handler handler){
		
		HttpUtils http = new HttpUtils();
		try{			
			String url = MUSIC_GET_SONG_DETAIL.concat("&songid=") + songId;
			Log.d("MUSIC_UTILS", "=============="+url);
			http.send(HttpMethod.GET, url, new RequestCallBack<String>() {
				public void onSuccess(ResponseInfo<String> responseInfo) {
					String result = responseInfo.result;
					try{
						JSONObject json = new JSONObject(result);
						JSONObject detail = json.getJSONObject("songinfo");
						String picUrl = detail.getString("artist_480_800");	
						if(picUrl == null || "".equals(picUrl)){
							picUrl = detail.getString("artist_500_500");	
						}
						Log.d("MUSIC_GET_SONGID", "========================="+ picUrl +"==========================");
						downloadMusicFile(picUrl,target,1,handler);
					}catch(Exception ex){
						handler.obtainMessage(1, "err").sendToTarget();
					}
				}
				public void onFailure(HttpException err, String msg) {
					handler.obtainMessage(1, "err").sendToTarget();
					Log.d("MUSIC_GET_SONGID", "==============GET SONG_PIC FAILED==========="+ msg +"==========================");
				}			
			});

		}catch(Exception ex){
			handler.obtainMessage(1, "err").sendToTarget();
		}
	}
	
	
	private static void downloadMusicFile(String url,String target,final int flag,final Handler handler){
		HttpUtils http = new HttpUtils();
		try{
			if(url.indexOf("http")==-1){
				url =  MUSIC_SOURCE_NET + url;
			}
			http.download(url, target,true, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> resp) {
					handler.obtainMessage(flag, resp.result.getAbsoluteFile()).sendToTarget();
				}
				
				@Override
				public void onFailure(HttpException err, String errMsg) {
					Log.d("MUSIC_GET_LRC", "==============GET SONG_LRC FAILED==========="+ errMsg +"==========================");
					handler.obtainMessage(flag, "err").sendToTarget();
				}
			});
		}catch(Exception ex){
			handler.obtainMessage(flag, "err").sendToTarget();
		}
	}
}
	