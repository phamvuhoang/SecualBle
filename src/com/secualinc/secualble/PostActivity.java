/**
 * Make the screen of test post data
 * @author Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.secualble;

import com.secualinc.http.*;
import com.secualinc.common.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PostActivity extends Activity {
	
	private float ACCEPT_VALUE = 1.0f;
	
	private EditText mAppId;
	private EditText mPassword;
	private EditText mName;
	private Spinner mAlert;
	private ArrayAdapter<String> mListAlert;
	private TextView mResult;
	private String mStrResult;
	private ProgressBars mProgressbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		
		mAppId = (EditText)findViewById(R.id.app_id);
		mPassword = (EditText)findViewById(R.id.password);
		mName = (EditText)findViewById(R.id.name);
		mAlert = (Spinner)findViewById(R.id.alert);
		mListAlert = new ArrayAdapter<String>(this, 
				android.R.layout.simple_spinner_dropdown_item, 
				new String[]{"true", "false"});
		mAlert.setAdapter(mListAlert);
		mResult = (TextView)findViewById(R.id.result);
		mStrResult = "";
		
		mProgressbar = new ProgressBars(this);
		
	}
	
	//　メニュー展開（画面上部）
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.main, menu);
			
			menu.findItem(R.id.demo_stop).setVisible(false);
			menu.findItem(R.id.demo_start).setVisible(false);
			
			menu.findItem(R.id.post_server).setVisible(false);
			menu.findItem(R.id.back_main).setVisible(true);
			return true;
		}
		
		//　メニュー選択時
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			
			// Rederect to main activity
				case R.id.back_main:
					Intent intent = new Intent(this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
					startActivity(intent);
					finish();
					
					break;
			}
			invalidateOptionsMenu();
			return true;
		}
		
	private float[] makeTestData(){
		return new float[] { 0.9777f, 0.23100f, 1.000006f, 0.9777f, 0.23100f, 0.000006f };
	}
	public void onButtonPostClick(View v){
		mProgressbar.show("接続中...");
		
		float[] sensorLogs = makeTestData();
		analization(sensorLogs);
		
	}
	
	private void analization(float[] sensorLogs){
		if (sensorLogs == null) {
			return;
		}
		
		if (sensorLogs.length < 1) {
			return;
		}
		
		for(int cnt = 0; cnt < sensorLogs.length; cnt++){
			if (sensorLogs[cnt] >= ACCEPT_VALUE){
				post2Server();
			}
		}
	}
	
	private void post2Server(){
		
		String url = "/SECUAL-GATEWAY-00001/sensors/SECUAL-SENSOR-00001/secual_events";
		
		AsyncHttpRequest request = new AsyncHttpRequest(url);
		PostResponse postResponse = new PostResponse(this);
		
		/* Make parameter json */
		Object secualEvent = request.makeParams(
				new String[]{"name", "alert"},  
				new Object[]{mName.getText(), mAlert.getSelectedItem().toString() == "true"});
		
		request.addParams(
				new String[]{"appID", "password", "secual_event"},  
				new Object[]{mAppId.getText(), mPassword.getText(), secualEvent});
		
		/* Post to server */
		request.post2Server(this, postResponse);
	}
	
	/**
	 * Handler post data to server finish
	 */
	private class PostResponse extends AsyncHttpResponse {
	    
	    private Context _context;

	    public PostResponse(Context context) {
	        this._context = context;
	    }

	    @Override
	    public void onFinish() {
	    	super.onFinish();
	    	Log.d("Debug", "PostResponse - onFinish");
	    	int status = this.getStatus();
	    	/* Status okie */
	        if (status == 200 || status == 204){
	        	String line = "";
	            try {
					line = new String(this.getData(), "UTF-8");
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	Toast.makeText(this._context, "Success: \n header=" + status 
						+ "\n; data =\n" + line, Toast.LENGTH_SHORT).show();
	        	Log.d("Debug","Success: " + "Header="+ status + "; data =" + line);
	        	mStrResult = mStrResult + "\nHeader="+ status + "; data =" + line + "\n";
	        	
	            mResult.setText(mStrResult);
	            mProgressbar.hide();
	        }
	        /* Status error */
	        else 
	        {
	        	Toast.makeText(this._context, "Error: header=" 
						+ status, Toast.LENGTH_SHORT).show();
	        	Log.d("Debug","Error: " + "Header="+ status);
	        	mStrResult = mStrResult + "\nError with header="+ status + "\n";
				mResult.setText(mStrResult);
				mProgressbar.hide();
	        }
	    	
	    }
	}
	
}


