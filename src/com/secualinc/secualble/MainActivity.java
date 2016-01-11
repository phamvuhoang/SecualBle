package com.secualinc.secualble;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {


    private BluetoothAdapter mBluetoothAdapter;
    private boolean mDemoRuning;
    private boolean mScanning;
    private boolean mConnect;
    private Handler mHandler;
    private Handler mTimeHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 15000;	//スキャンを始めてからSCAN_PERIOD mS後にスキャンを自動停止
    private static final long SCAN_INTERVAL = 3000; //スキャンの間隔
    ArrayAdapter<String> listAdapter;

    /** 異常通知時のサービスUUID */
    private static final String SENSOR_SERVICE_UUID = "029258C6-9403-476C-B06F-61E56C2ACCFF";
    /** 異常通知時のキャラクタリスティックUUID */
    private static final String SENSOR_CHARACTERISTIC_UUID = "029258C6-9403-476C-B06F-61E56C2ACC31";
    
    private BluetoothGatt bluetoothGatt;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().setTitle("Secual GW Emulator");
        mHandler = new Handler();
        mTimeHandler = new Handler();
        
        ListView listView = (ListView)findViewById(R.id.listView);

        
        listAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);

        listView.setAdapter(listAdapter);
		
		// 対象デバイスがBLEをサポートしているかのチェック。
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "このデバイスではBLEはサポートされておりません。", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Bluetooth adapter 初期化.  （API 18以上が必要)
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // 対象デバイスがBluetoothをサポートしているかのチェック.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetoothがサポートされておりません。", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        mConnect = false;
	}


	//　メニュー展開（画面上部）
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mDemoRuning) {
			menu.findItem(R.id.demo_stop).setVisible(false);
			menu.findItem(R.id.demo_start).setVisible(true);
			
		} else {
			menu.findItem(R.id.demo_stop).setVisible(true);
			menu.findItem(R.id.demo_start).setVisible(false);
		}
		menu.findItem(R.id.post_server).setVisible(true);
		menu.findItem(R.id.back_main).setVisible(false);
		return true;
	}
	
	//　メニュー選択時
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		//　デモの開始
			case R.id.demo_start:
				mDemoRuning = true;
				scanLeDevice(true);
				Log.d("log","start");
				
				break;
		//　デモの停止
			case R.id.demo_stop:
				if(mConnect){
					bluetoothGatt.disconnect();
					Log.d("Debug","Disconnect");
				}
    			
				scanLeDevice(false);
				Log.d("log","stop");
				mDemoRuning = false;
				break;
		// Rederect to post activity
			case R.id.post_server:
				Intent intent = new Intent(this, PostActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				finish();
				
				break;
		}
		invalidateOptionsMenu();
		return true;
	}

	//　画面が出る時
    @Override
    protected void onResume() {
        super.onResume();

        // Bluetooth機能が有効になっているかのチェック。無効の場合はダイアログを表示して有効をうながす。(intentにて)
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        if(mDemoRuning){
        	scanLeDevice(true);
        }
    }

    //　intent戻り時
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //　画面が消える時
    @Override
    protected void onPause() {
        super.onPause();
        if(mDemoRuning){
        	scanLeDevice(false);
        }
    }


    //　スキャンメソッド
    private void scanLeDevice(final boolean enable) {
        if (mDemoRuning && enable) {
            // 一定時間後にスキャンを停止（をセット）（３）
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    senceInteval();
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            //　スキャン開始（２）
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    
    // センサー検索間隔タイマー
    private void senceInteval() {
    	if(mDemoRuning && !mScanning){
    		mTimeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                	scanLeDevice(true);
                }
            }, SCAN_INTERVAL);
    	}
    }

    // スキャンのコールバック
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
    	//　デバイスが発見された時
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                	if(device.getName().equals("30") || device.getName().equals("31") || device.getName().equals("32") || device.getName().equals("33")){
                		Log.d("Debug","DeviceName: "+device.getName());
                		Log.d("Debug","Address: "+device.getAddress());
                		listAdapter.add("MacAddress: "+device.getAddress()+" Status: "+device.getName());
                		listAdapter.notifyDataSetChanged();
//                		if(device.getName().equals("31")){
//                			Log.d("Debug","charenge connect");
//                			bluetoothGatt = device.connectGatt(getApplicationContext(), false, mGattCallBack);
//                		}

                	}
                	
                }
            });
        }
        
    };
    
    
    private final BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
    	@Override
    	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
    		Log.d("Debug", "onConnectionStateChange: " + status + " -> " + newState);
    		if (newState == BluetoothProfile.STATE_CONNECTED) {
    			// GATTへ接続成功
    			// サービスを検索する
    			Log.d("Debug","gatt connect OK");
    			bluetoothGatt = gatt;
    			mConnect = true;
    			gatt.discoverServices();
    		} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
    			// GATT通信から切断された
    			Log.d("Debug","gatt disconnect");
    			mConnect = false;
    			bluetoothGatt.close();
    			bluetoothGatt = null;
    		}
    	}
    	
    	@Override
    	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
    	    Log.d("DebugGatt", "onServicesDiscovered received: " + status);
    	    if (status == BluetoothGatt.GATT_SUCCESS) {
    	        BluetoothGattService service = gatt.getService(UUID.fromString(SENSOR_SERVICE_UUID));
    	        if (service == null) {
    	            // サービスが見つからなかった
    	        	Log.d("Debug","Not Service");
    	        } else {
    	            // サービスを見つけた
    	            BluetoothGattCharacteristic characteristic =
    	                    service.getCharacteristic(UUID.fromString(SENSOR_CHARACTERISTIC_UUID));
    	            if (characteristic == null) {
    	                Log.d("Debug","Not CharacterRistic");
    	            } else {
    	                // キャラクタリスティックを見つけた
//    	            	Log.d("Debug","CharacterRistic: "+ characteristic);
    	            	Log.d("Debug","CharacterRistic uuid: "+ characteristic.getUuid());
    	            	Log.d("Debug","CharacterRistic tostring: "+ characteristic.toString());
    	            	
    	                gatt.readCharacteristic(characteristic);
    	            	
    	            }
    	        }
    	    }
    	}
    	
    	@Override
        public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic,int status) {
    		Log.d("Debug","characteristic getValue: "+characteristic.getValue().toString());
    		Log.d("Debug","characteristic0: "+characteristic.getValue()[0]);
    		Log.d("Debug","characteristic0Hex: "+Integer.toHexString(characteristic.getValue()[0] & 0xff));
    		Log.d("Debug","characteristic1: "+characteristic.getValue()[1]);
    		Log.d("Debug","characteristic1Hex: "+Integer.toHexString(characteristic.getValue()[1] & 0xff));
//    		Log.d("Debug","characteristic2: "+characteristic.getValue()[2]);
    		Log.d("Debug","characteristic Test ");
    		Log.d("Debug","characteristic3: "+characteristic.getValue()[3]);
    		Log.d("Debug","characteristic4: "+characteristic.getValue()[4]);
    		Log.d("Debug","characteristic5: "+characteristic.getValue()[5]);
        }
    };
    	

    
}
