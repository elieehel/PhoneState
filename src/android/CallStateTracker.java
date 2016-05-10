package com.cellip.lyncapp;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallStateTracker extends Service  {

	private static final String TAG = "phonestate";

	private Handler mHandler = new Handler();
	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
	private Context context;
	private ListenToPhoneState listener;
	private TelephonyManager tManager;
	private boolean init = false;
	private JSONObject json;
	private Prefs pU;
	


	private void doLogIn(String uid, String pid) {
		Cb cb = new Cb() {
			public void callback(String result) {
				Log.i(TAG, "login_back returned, parsing...");
				if (parseLogIn(result)) {
					Log.i(TAG, "Result properly parsed, trying to fire intent");
					Intent it = new Intent("com.cellip.show.transfer");
					it.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
					it.setAction(Intent.ACTION_MAIN);
					it.addCategory(Intent.CATEGORY_LAUNCHER);
					context.startActivity(it);
					context.getApplicationContext().startActivity(it);
					Log.i(TAG, "Activity should start now, context is " + context + " and appcontext is " + context.getApplicationContext());
				}
			}
		};
		new WebAccess(cb).execute("https://www.cellip.com/sv/minasidor/json/lync_app/login_back.html?user="+uid+"&pass="+pid);
	}
	
	private boolean parseLogIn(String result) {
		boolean ret = false;
		try {
			JSONObject res = new JSONObject(result);
			if (res.getInt("error") == 0) {
				json.put("loggedObject", res);
				pU.writeFile(json.toString());
				ret = true;
			}
		} catch (JSONException e) {
		} catch (IOException e) {
		}
		return ret;
	}

	//will launch the activity
	private Runnable mLaunchTask = new Runnable() {
		public void run() {
			try {
				String prefs = pU.readFile();
				json = new JSONObject(prefs);
				JSONObject login = json.getJSONObject("login");
				if (login == null || !json.getBoolean("allow_popup") || ((json.optInt("isProxied") == 0 && !json.optBoolean("isProxied")) && (!json.getString("linkedNumber").matches("^\\d+$") || !json.getString("linkedNumber").replaceAll("^46", "0").equals(json.getString("numReg").replaceAll("^46", "0"))))) {
					System.out.println("phonestate not logging in! ");
					System.out.println("Is null? " + login == null);
					System.out.println("Allowing popup? " + json.getBoolean("allow_popup"));
					System.out.println("Proxied? " + (json.optInt("isProxied", 0) == 0 && !json.optBoolean("isProxied")));
					return;
				}
				Log.i(TAG, "Finished reading prefs file, will try to login");
				doLogIn(login.getString("uid"), login.getString("pid"));
			} catch (Exception e) {
				e.printStackTrace(System.out);

				return;
			}
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
		pU = Prefs.getInstance(context);
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
				if (lastState == TelephonyManager.CALL_STATE_RINGING)
					mHandler.postDelayed(mLaunchTask, 3500);
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				break;
			default:
				break;
			}
			lastState = state;
		}

	}

}
