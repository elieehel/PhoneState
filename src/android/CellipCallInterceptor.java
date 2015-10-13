package com.cellip.lyncapp;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.util.Log;

public class CellipCallInterceptor extends BroadcastReceiver {
	

	private JSONObject json;
	private Prefs p;
	private WeakReference<CellipCallInterceptor> mRef;
	private WeakReference<Cb> mRef2;
	private static final String TAG = "lyncapp";
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		Log.v(TAG, "HAS CALL");
		p = Prefs.getInstance(context);
		try {
			if (p.getPrefsObject(true).getInt("isProxied") > 0 && p.getPrefsObject(true).getBoolean("allow_call_intercept")) {
				
				JSONObject login = json.getJSONObject("login");
				if (login == null || !json.getString("numReg").matches("^\\d+$"))
					return;
				Log.v(TAG, "HAS LOGIN DETAILS");
				mRef = new WeakReference<CellipCallInterceptor>(this);
				Cb cb = new Cb() {
					public void callback(String result) {
						CellipCallInterceptor self = mRef.get();
						final String oldNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
						self.setResultData("+46812451570");
						final String newNumber = self.getResultData();
						String msg = "Intercepted outgoing call. Old number " + oldNumber + ", new number " + newNumber;
						Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
					}
				};
				
				doLogIn(login.getString("uid"), login.getString("pid"), cb);
				
				
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);

			return;
		}
	}
	
	private void doLogIn(String uid, String pid, final Cb success) {
		Log.v(TAG, "DO THE LOGIN");
		mRef2 = new WeakReference<Cb>(success);
		Cb cb = new Cb() {
			public void callback(String result) {
				if (parseLogIn(result)) {
					Cb a = mRef2.get();
					a.callback("");
				}
			}
		};
		new WebAccess(cb).execute("https://www.cellip.com/sv/minasidor/json/lync_app/login.html?user="+uid+"&pass="+pid);
	}
	
	private boolean parseLogIn(String result) {
		boolean ret = false;
		try {
			JSONObject res = new JSONObject(result);
			if (res.getInt("error") == 0) {
				ret = true;
			}
		} catch (JSONException e) {
		} 
		return ret;
	}

}
