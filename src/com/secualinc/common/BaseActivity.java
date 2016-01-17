/**
 * The class base of activitys. 
 * @author Pham Vu Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.common;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

public class BaseActivity extends Activity {

	protected static List<BaseActivity> screenStack = new ArrayList<BaseActivity>();
	protected static Hashtable<String, Object> transferData = new Hashtable<String, Object>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        screenStack.add(this);
	}
		
	@Override
	protected void onResume(){
		super.onResume();
		
	}
	
	/*
	 * Close activity or log out.
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed(){
		if (App.loginID == null){
			finish();
		}
		else if (screenStack.size() > 1){
    		closeActivity();
    	}
   	} 
		
	/*
	 * Called when child activity close by call "closeActivity" method
	 */
	public void onChildActivityClosed(){

	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
	}
	
	protected Activity getActivityContext(){
		return this;
	}
	
	/*
	 * Finish all activities in stack. Except current activity
	 */
	protected void clearActivityHistory(){
		for(int i=screenStack.size() - 1;i>=0; i--){
			BaseActivity activity = screenStack.get(i);
			if (activity != this){
				screenStack.remove(activity);
				activity.finish();
			}
		}
	}
	
	/*
	 * Finish all activities in stack. Then show Activity
	 */
	protected void directToActivity(Class<?> activityClass){
		//if (!this.getClass().toString().equals(activityClass.toString())){
			clearActivityHistory();
			screenStack.remove(this);
			Intent intent = new Intent(this, activityClass);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startActivity(intent);
			finish();
		//}
	}
	
	/*
	 * Set data de tra ve activity truoc day.
	 */
	protected void setActivityParam(String key,Object value){
		if (value != null){
			BaseActivity.transferData.put(key, value);
		}
		else{
			BaseActivity.transferData.remove(key);
		}
	}
	
	/*
	 * Get data
	 */
	protected Object getActivityParam(String key){
		return BaseActivity.transferData.get(key);
	}
		
	/*
	 * Get data
	 */
	protected Object getActivityParam(String key,Object defaultValue){
		if (BaseActivity.transferData.containsKey(key))
			return BaseActivity.transferData.get(key);
		else
			return defaultValue;
		
	}
		
	/*
	 * Add activity to top of stack, and show it on screen.
	 */
	protected void showActivity(Class<?> activityClass){
		Intent intent = new Intent(this, activityClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivityForResult(intent,1);

	} 
	
	/*
	 * Remove this activity from stack, hidden it from screen, finish activity
	 */
	protected void closeActivity(){
		screenStack.remove(this);
		
		if (screenStack.size() > 0){
			screenStack.get(screenStack.size()-1).onChildActivityClosed();
		}
		this.finish();
	}
	
	/*
	 * Show Progress Dialog on top screen with message
	 */
	protected void showProgressDialog(int messageId){
		
	}
	
	/*
	 * Hide Progress Dialog, if it is showing. 
	 */
	protected void hideProgressDialog(){
		
	}
	
	//******************* PRIVATE METHODS ******************************
	protected void setIntSetting(String key, int value){
		SharedPreferences settings = getSharedPreferences(App.SETTINGS, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putInt(key, value);
		editor.commit();
	}
	
	protected int getIntSetting(String key, int defaultValue){
		SharedPreferences settings = getSharedPreferences(App.SETTINGS, MODE_PRIVATE);
		
		return settings.getInt(key, defaultValue);
	}
	
	protected void setStringSetting(String key, String value){
		SharedPreferences settings = getSharedPreferences(App.SETTINGS, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(key, value);
		editor.commit();
	}
	
	protected String getStringSetting(String key, String defaultValue){
		SharedPreferences settings = getSharedPreferences(App.SETTINGS, MODE_PRIVATE);
		return settings.getString(key, defaultValue);
	}
}
