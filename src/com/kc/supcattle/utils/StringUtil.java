package com.kc.supcattle.utils;

public class StringUtil {
	
	public static final String ERR_FLAG = "err";
	
	public static boolean isNullOrEmpty(String str) {
		if (str == null || "".equals(str))
			return true;
		else {
			if (str.trim().equals("")) {
				return true;
			}
			return false;
		}
	}
	
	public static String replaceStripHtml(String content) { 
		content = content.replaceAll("\\<.*?>", ""); 
		content = content.replaceAll("\\s*|\t|\r|\n", "");
		content = content.replaceAll("&nbsp;", "");
		return content; 
		}
	
	public static boolean isEmpty(String str){
		return !StringUtil.isNullOrEmpty(str) ? "null".equals(str) : true;
	}
}
