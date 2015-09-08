package com.cellip.lyncapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class CellipCallInterceptor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Prefs p = Prefs.getInstance(context);
		if (p.getPrefsObject(true).getBoolean("allow_call_intercept")) {
			final String oldNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			this.setResultData("0123456789");
			final String newNumber = this.getResultData();
			String msg = "Intercepted outgoing call. Old number " + oldNumber + ", new number " + newNumber;
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}

}
