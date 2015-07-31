package com.cellip.lyncapp;

import com.cellip.lyncapp.PhoneState.ListenToPhoneState;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallStateTracker extends Service  {

	private Handler mHandler = new Handler();
	private Context context;
	private ListenToPhoneState listener;
	private TelephonyManager tManager;
	private boolean init = false;

	//will launch the activity
	private Runnable mLaunchTask = new Runnable() {
		public void run() {
			Intent it = new Intent("intent.my.action");
			it.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
			it.setAction(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_LAUNCHER);
			context.startActivity(it);
			context.getApplicationContext().startActivity(it);
		}
	};

	public CallStateTracker() {
		super();
	}

	@Override
	public void onCreate() {
		super.onCreate(); // if you override onCreate(), make sure to call super().
		// If a Context object is needed, call getApplicationContext() here.
		context = getApplicationContext(); 
	}
	
	@Override
	public void onDestroy() {
		if (tManager != null)
			tManager.listen(listener, PhoneStateListener.LISTEN_NONE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!init) {
			init = true;
			tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			listener = new ListenToPhoneState();
			tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		} 
		return START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private class ListenToPhoneState extends PhoneStateListener {

		public void onCallStateChanged(int state, String incomingNumber) {

			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				mHandler.postDelayed(mLaunchTask, 2000);
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			default:
				break;
			}
		}

	}

}
