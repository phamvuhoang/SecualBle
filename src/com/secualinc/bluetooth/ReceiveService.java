package com.secualinc.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ReceiveService {
	
	private static final String NAME_SECURE = "BluetoothChatSecure";
	private static final String NAME_INSECURE = "BluetoothChatInsecure";
	
	// Unique UUID for this application
	private static final UUID MY_UUID_SECURE = UUID
			.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

	// Member fields
	private final BluetoothAdapter bluetoothAdapter;
	private final Handler handler;
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private int state;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0;
	public static final int STATE_LISTEN = 1;
	public static final int STATE_CONNECTING = 2; // initiate outgoing connection
	public static final int STATE_CONNECTED = 3; // connected to remote device
	
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	public static final String DEVICE_ADDRESS = "deviceAddress";
	
	public ReceiveService(Context context, Handler handler) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		state = STATE_NONE;

		this.handler = handler;
	}

	// Set the current state of the chat connection
	private synchronized void setState(int state) {
		this.state = state;

		handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	// get current connection state
	public synchronized int getState() {
		return state;
	}

	// start service
	public synchronized void start() {
		// Cancel any thread
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel any running thread
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}

	// initiate connection to remote device
	public synchronized void connect(BluetoothDevice device, boolean secure) {
		// Cancel any thread
		if (state == STATE_CONNECTING) {
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}

		// Cancel running thread
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Start the thread to connect with the given device
		connectThread = new ConnectThread(device, secure);
		connectThread.start();
		setState(STATE_CONNECTING);
	}

	// manage Bluetooth connection
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device, final String socketType) {
		// Cancel the thread
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		// Cancel running thread
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(socket, socketType);
		connectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = handler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		handler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	// stop all threads
	public synchronized void stop() {
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}

		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}

		setState(STATE_NONE);
	}

	public void write(byte[] out) {
		ConnectedThread r;
		synchronized (this) {
			if (state != STATE_CONNECTED)
				return;
			r = connectedThread;
		}
		r.write(out);
	}

	private void connectionFailed() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Unable to connect device");
		msg.setData(bundle);
		handler.sendMessage(msg);

		// Start the service over to restart listening mode
		ReceiveService.this.start();
	}

	private void connectionLost() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Device connection was lost");
		msg.setData(bundle);
		handler.sendMessage(msg);

		// Start the service over to restart listening mode
		ReceiveService.this.start();
	}

	// runs while attempting to make an outgoing connection
	private class ConnectThread extends Thread {
		private final BluetoothSocket socket;
		private final BluetoothDevice device;
		private String socketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			this.device = device;
			BluetoothSocket tmp = null;
			socketType = secure ? "Secure" : "Insecure";

			try {
				if (secure) {
					tmp = device
							.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
				} else {
					tmp = device
							.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (IOException e) {
			}
			socket = tmp;
		}

		public void run() {
			setName("ConnectThread" + socketType);

			// Always cancel discovery because it will slow down a connection
			bluetoothAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				socket.connect();
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e2) {
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (ReceiveService.this) {
				connectThread = null;
			}

			// Start the connected thread
			connected(socket, device, socketType);
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	// runs during a connection with a remote device
	private class ConnectedThread extends Thread {
		private final BluetoothSocket bluetoothSocket;
		private final InputStream inputStream;
		private final OutputStream outputStream;

		public ConnectedThread(BluetoothSocket socket, String socketType) {
			this.bluetoothSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			inputStream = tmpIn;
			outputStream = tmpOut;
		}

		public void run() {
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream
			while (true) {
				try {
					// Read from the InputStream
					bytes = inputStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					handler.obtainMessage(MESSAGE_READ, bytes, -1,
							buffer).sendToTarget();
				} catch (IOException e) {
					connectionLost();
					// Start the service over to restart listening mode
					ReceiveService.this.start();
					break;
				}
			}
		}

		// write to OutputStream
		public void write(byte[] buffer) {
			try {
				outputStream.write(buffer);
				handler.obtainMessage(MESSAGE_WRITE, -1, -1,
						buffer).sendToTarget();
			} catch (IOException e) {
			}
		}

		public void cancel() {
			try {
				bluetoothSocket.close();
			} catch (IOException e) {
			}
		}
	}

}
