/*
 * Released under MIT License http://opensource.org/licenses/MIT
 * Copyright (c) 2013 Plasty Grove
 * Refer to file LICENSE or URL above for full text 
 */

package com.blueserial;

import java.io.*;
import java.util.*;

import com.blueserial.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
//import android.os.AsyncTask;
//import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "BlueTest5-MainActivity";
	private int mMaxChars = 50000;//Default
	private UUID mDeviceUUID;
	private BluetoothSocket mBTSocket;
	private ReadInput mReadThread = null;

	private boolean mIsUserInitiatedDisconnect = false;

	// All controls here
	private TextView mTxtReceive;
	private EditText mEditSend;
	private Button mBtnDisconnect;
	private Button mBtnSend;
	private Button mBtnClear;
	private Button mBtnClearInput;
	private ScrollView scrollView;
	private CheckBox chkScroll;
	private CheckBox chkReceiveText;

	private boolean mIsBluetoothConnected = false;

	private BluetoothDevice mDevice;

	private ProgressDialog progressDialog;
	
	private String curString;
	private String tempString;
	private int accel_X;
	private int accel_Y;
	private int accel_Z;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActivityHelper.initialize(this);

		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		mDevice = b.getParcelable(Homescreen.DEVICE_EXTRA);
		mDeviceUUID = UUID.fromString(b.getString(Homescreen.DEVICE_UUID));
		mMaxChars = b.getInt(Homescreen.BUFFER_SIZE);
			
		Log.d("TAG", "Ready");
		
		TextLog_Mov.mAppendTime = true;
		TextLog_Mov.mReverseReport = true;
		TextLog_Mov.init(this);

		mBtnDisconnect = (Button) findViewById(R.id.btnDisconnect);
		mBtnSend = (Button) findViewById(R.id.btnSend);
		mBtnClear = (Button) findViewById(R.id.btnClear);
		mTxtReceive = (TextView) findViewById(R.id.txtReceive);
		mEditSend = (EditText) findViewById(R.id.editSend);
		scrollView = (ScrollView) findViewById(R.id.viewScroll);
		chkScroll = (CheckBox) findViewById(R.id.chkScroll);
		chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
		mBtnClearInput = (Button) findViewById(R.id.btnClearInput);

		mTxtReceive.setMovementMethod(new ScrollingMovementMethod());
		

		mBtnDisconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsUserInitiatedDisconnect = true;

				Log.d("TAG", "Home button start here?");
				
				lg_Mov.o("Disconnect start 1");
				new DisConnectBT().execute();
			}
		});

		mBtnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					String command = "#";
					//mBTSocket.getOutputStream().write(mEditSend.getText().toString().getBytes());	
					mBTSocket.getOutputStream().write(command.getBytes());		
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		mBtnClear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mEditSend.setText("");
			}
		});
		
		mBtnClearInput.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mTxtReceive.setText("");
			}
		});

	}

	private class ReadInput implements Runnable {

		private boolean bStop = false;
		private boolean bStarted = false;
		private Thread t;
		
		private long prevTick;
		private long currTick;
		
		public ReadInput() {
			t = new Thread(this, "Input Thread");
			t.start();
		}

		public boolean isRunning() {
			return t.isAlive();
		}

		@Override
		public void run() {
			InputStream inputStream;

			try {
				inputStream = mBTSocket.getInputStream();
				while (!bStop) {
					byte[] buffer = new byte[4096]; // 2014.04.25 Shin increase buffer size, if buffer size is too small likes 256 then it stops after 1 min.
					if (inputStream.available() > 0) {
						inputStream.read(buffer);
						bStarted = true;
						int i = 0;
						int line_cnt = 0;
						int data_cnt = 0;
						/*
						 * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
						 */
						for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
						}
						final String strInput = new String(buffer, 0, i);
						String inputStr = new String(buffer, 0, i);
						
						//Log.d("TAG", strInput);
						//lg_Mov.o("Test: " + strInput);
						
						Log.d("TAG", "input String: " + inputStr);
						
						
						if(tempString!=null)
						{
							inputStr = tempString + inputStr;
						}
						tempString = null;
						
						String t1 = "total string: " + inputStr;
						Log.d("TAG", t1);
						
						String[] line_parser = inputStr.split("\r\n");
						
						Log.d("TAG", "line_parser length: " + line_parser.length);
						
						for(line_cnt =0; line_cnt< line_parser.length; line_cnt++)
						{
							if(line_parser[line_cnt].matches(".*a.*")) // if this line completes
							{
								Log.d("TAG", "a is coming");						
								//start second parsing process
								line_parser[line_cnt] = line_parser[line_cnt].replace("a", "");
								line_parser[line_cnt].trim();
								String[] data_parser = line_parser[line_cnt].split(",");
								for(data_cnt = 0; data_cnt<data_parser.length; data_cnt++)
								{
									switch(data_cnt)
									{
									case 0:
										Log.d("TAG", "Y: " + data_parser[data_cnt]);
										break;
									case 1:
										Log.d("TAG", "P: " + data_parser[data_cnt]);
										break;									
									case 2:										
										Log.d("TAG", "R: " + data_parser[data_cnt]);
										break;									
									}
								}
								lg_Mov.o("YPR: " + line_parser[line_cnt]);
							}
							else
							{
								Log.d("TAG", "no a incomplete line");	
								line_parser[line_cnt].trim();
								tempString = line_parser[line_cnt];
								
							}
						}
						Log.d("TAG", "temp String: " + tempString);
						
						


						/*
						 * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
						 */

						if (chkReceiveText.isChecked()) {
							mTxtReceive.post(new Runnable() {
								@Override
								public void run() {									
									mTxtReceive.append(strInput);
									//Uncomment below for testing
									//mTxtReceive.append("\n");
									//mTxtReceive.append("Chars: " + strInput.length() + " Lines: " + mTxtReceive.getLineCount() + "\n");
									
									
									int txtLength = mTxtReceive.getEditableText().length();  
									if(txtLength > mMaxChars){
										mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
									}

									if (chkScroll.isChecked()) { // Scroll only if this is checked
										scrollView.post(new Runnable() { // Snippet from http://stackoverflow.com/a/4612082/1287554
													@Override
													public void run() {
														scrollView.fullScroll(View.FOCUS_DOWN);
													}
												});
									}
								}
							});
						}
						prevTick = System.currentTimeMillis();

					}
					else
					{
						Log.d("TAG", "No stream input");
						if(bStarted == true)
						{
							
							Log.d("TAG", "good catch");
							currTick = System.currentTimeMillis();
							Log.d("TAG", "current Tick " + currTick);
							Log.d("TAG", "prevTick Tick " + prevTick);
							
							if(currTick - prevTick > 15000)
							{
								lg_Mov.o("force out");
								Log.d("TAG", "force out");
								mIsUserInitiatedDisconnect = true;
								//Toast.makeText(getApplicationContext(), "Connection exception occurs!!", Toast.LENGTH_LONG).show();
								lg_Mov.o("Disconnect start 2");
								new DisConnectBT().execute();								
							}
						}						
					}
					Thread.sleep(100); // 2014.04.25 Shin, shorten sleep time to react fast
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public void stop() {
			bStop = true;
		}

	}

	private class DisConnectBT extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			Log.d("TAG", "Disconnect Start");
			lg_Mov.o("Disconnect Start");
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (mReadThread != null) {
				mReadThread.stop();
				while (mReadThread.isRunning())
					; // Wait until it stops
				mReadThread = null;

			}

			try {
				mBTSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mIsBluetoothConnected = false;
			int tone_cnt = 0;
			ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 20);
			for(tone_cnt=0; tone_cnt<1; tone_cnt++)
			{
				toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400);
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{				
					;						
				}
			}			
			if (mIsUserInitiatedDisconnect) {
				lg_Mov.o("Disconnect End");
				finish();
			}
		}

	}

	private void msg(String s) {
		Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPause() {
		lg_Mov.o("onPause");
		if (mBTSocket != null && mIsBluetoothConnected) {
			lg_Mov.o("onPause1");
			//new DisConnectBT().execute();
		}
		lg_Mov.o("Paused");
		super.onPause();
	}

	@Override
	protected void onResume() {
		lg_Mov.o("onResume");
		if (mBTSocket == null || !mIsBluetoothConnected) {
			lg_Mov.o("onResume1");
			new ConnectBT().execute();
		}
		lg_Mov.o("onResume");
		super.onResume();
	}

	@Override
	protected void onStop() {
		lg_Mov.o("onStop");
		lg_Mov.o("onStop");
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		lg_Mov.o("onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}	

	private class ConnectBT extends AsyncTask<Void, Void, Void> {
		private boolean mConnectSuccessful = true;

		@Override
		protected void onPreExecute() {
			Log.d("TAG", "ConnectBT Start");
			progressDialog = ProgressDialog.show(MainActivity.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
		}

		@Override
		protected Void doInBackground(Void... devices) {

			try {
				if (mBTSocket == null || !mIsBluetoothConnected) {
					mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
					mBTSocket.connect();
				}
			} catch (IOException e) {
				// Unable to connect to device
				e.printStackTrace();
				mConnectSuccessful = false;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (!mConnectSuccessful) {
				Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
				finish();
			} else {
				msg("Connected to device");
				mIsBluetoothConnected = true;
				mReadThread = new ReadInput(); // Kick off input reader
			}

			progressDialog.dismiss();
		}

	}

}
