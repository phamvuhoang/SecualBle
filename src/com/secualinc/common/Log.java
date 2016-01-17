/**
 * The class manage log of application. 
 * @author Pham Vu Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.common;

//import android.widget.Toast;

public class Log {
	
	public static void ShowLog(String tag, String message){
		android.util.Log.d(tag, message);
	}
	
	public static void ShowLog(String tag, String message, Throwable tr){
		android.util.Log.d(tag, message, tr);
	}
	
	public static void ShowLogError(String tag, String message){
		android.util.Log.e(tag, message);
	}
	
	public static void ShowLogError(String tag, String message, Throwable tr){
		android.util.Log.e(tag, message, tr);
	}
	
	public static void ShowLogWarning(String tag, String message){
		android.util.Log.w(tag, message);
	}
	
	public static void ShowLogWarning(String tag, String message, Throwable tr){
		android.util.Log.w(tag, message, tr);
	}
	
//	public static void ShowToast(String tag, String message){
//		Toast.makeText(SecualBle, message, Toast.LENGTH_SHORT);
//	}
}
