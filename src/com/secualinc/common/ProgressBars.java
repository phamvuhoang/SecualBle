/**
 * The class manage progress dialog 
 * @author Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.common;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressBars {
	
	private Context _context;
	private ProgressDialog progressDialog;
	
	public ProgressBars(Context context) {
		if (context == null){
			return;
		}
		_context = context;
		progressDialog = new ProgressDialog(_context);
	}
	
	/*
	 * Show Progress Dialog on top screen with message
	 */
	public void show(String message){
		progressDialog.setMessage(message);
		progressDialog.show();
	}
	
	/*
	 * Hide Progress Dialog, if it is showing. 
	 */
	public void hide(){
		progressDialog.dismiss();
	}
	
}
