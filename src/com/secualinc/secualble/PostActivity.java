/**
 * Make the screen of test post data
 * @author Pham Vu Hoang
 * @email phamvuhoang@gmail.com
 * @createDate 2016.01.09
 */

package com.secualinc.secualble;

import com.secualinc.http.*;
import com.secualinc.common.*;
import com.secualinc.bluetooth.*;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
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
	private String connectedDeviceName = null;
	
	private final int REQUEST_ENABLE_BT = 3;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private ReceiveService receiveService = null;
	private String deviceAddress = "";
	
	private String mStrName = "";
	private String mStrAlert = "";
	private String mStrThreadhold = "";
	
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
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		//mProgressbar.show("接続中...");
		connectDevice();
		setupService();
	}
	

	@Override
	public void onStart() {
		super.onStart();

		if (!bluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		} else {
			if (receiveService == null)
				setupService();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();

		if (receiveService != null) {
			if (receiveService.getState() == receiveService.STATE_NONE) {
				receiveService.start();
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (receiveService != null)
			receiveService.stop();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				setupService();
			} else {
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	
	private void connectDevice(){
		
		receiveService = new ReceiveService(this, handler);
		
		
		Bundle extras = getIntent().getExtras();
	    if(extras == null) {
	    	deviceAddress= null;
	    } else {
	    	deviceAddress= extras.getString(ReceiveService.DEVICE_ADDRESS);
	    }
		
	}
	
	private void setupService(){
		
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
		receiveService.connect(device, true);
	}
	
	private Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case ReceiveService.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case ReceiveService.STATE_CONNECTED:
					Toast.makeText(PostActivity.this, 
							getString(R.string.title_connected_to, connectedDeviceName), 
							Toast.LENGTH_SHORT).show();
					//mProgressbar.hide();
					break;
				case ReceiveService.STATE_CONNECTING:
					Toast.makeText(PostActivity.this, 
							getString(R.string.title_connecting), 
							Toast.LENGTH_SHORT).show();
					break;
				case ReceiveService.STATE_LISTEN:
				case ReceiveService.STATE_NONE:
					Toast.makeText(PostActivity.this, 
							getString(R.string.title_not_connected), 
							Toast.LENGTH_SHORT ).show();
					//mProgressbar.hide();
					break;
				}
				
				break;
			case ReceiveService.MESSAGE_WRITE:
				//byte[] writeBuf = (byte[]) msg.obj;

				//String writeMessage = new String(writeBuf);
				//chatArrayAdapter.add("Me:  " + writeMessage);
				break;
			case ReceiveService.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;

				String readMessage = new String(readBuf, 0, msg.arg1);
				String strToast = "";
				String[] array = readMessage.split("\\|", -1);
				mStrName = array[0];
				mStrAlert = array[1];
				mStrThreadhold = array[2];
				strToast += "Name: " + mStrName + "\n";
				strToast += "Alert value: " + mStrAlert + "\n";
				strToast += "Threadhold: " + mStrThreadhold + "\n";
				
				Toast.makeText(PostActivity.this, 
						strToast,
						Toast.LENGTH_SHORT ).show();
				
				//float[] sensorLogs = makeTestData();
				//analization(sensorLogs);
				float val = Float.parseFloat(mStrThreadhold);
				float[] sensorLogs = new float[1];
				sensorLogs[0] = val;
				analization(sensorLogs);
				
				break;
			case ReceiveService.MESSAGE_DEVICE_NAME:

				connectedDeviceName = msg.getData().getString(ReceiveService.DEVICE_NAME);
				Toast.makeText(getApplicationContext(), 
						"Connected to " + connectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case ReceiveService.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(ReceiveService.TOAST), 
						Toast.LENGTH_SHORT).show();
				break;
			}
			return false;
		}
	});
		
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
				mStrAlert = mAlert.getSelectedItem().toString();
				mStrName = mName.getText().toString();	
				mStrResult = mStrResult + "\nData post: name = " + mStrName + ";alert= " + mStrAlert;
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
				new Object[]{mStrName, mStrAlert == "true"});
		
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
	        if (status == 200 || status == 204 || status == 201){
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
}


