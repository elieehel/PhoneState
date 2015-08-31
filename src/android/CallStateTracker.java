package com.cellip.lyncapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.ConfigXmlParser;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CallStateTracker extends Service  {

	private Handler mHandler = new Handler();
	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
	private Context context;
	private ListenToPhoneState listener;
	private TelephonyManager tManager;
	private boolean init = false;
	private ConfigXmlParser parser;
	
	private String readFile() {
		File sdcard = Environment.getExternalStorageDirectory();
		System.out.println("Parser is " + parser);
		System.out.println("Parser.getPreferences() is " + parser.getPreferences());
		System.out.println("Parser.getString(appcompany) is " + parser.getPreferences().getString("app_company", "cellip"));
		File file = new File(sdcard, parser.getPreferences().getString("app_company", "cellip")+"/prefs");
		System.out.println("RUNNING THE RUNNER FOR THE SERVICE " + file.getAbsolutePath());

		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
			}
			br.close();
		}
		catch (IOException e) {
		}
		return text.toString();
	}

	private void writeFile(String text) throws IOException {
		File sdcard = Environment.getExternalStorageDirectory();
		
		File file = new File(sdcard, parser.getPreferences().getString("app_company", "cellip")+"/prefs");
		
		FileOutputStream stream = new FileOutputStream(file);
		try {
		    stream.write("text-to-write".getBytes());
		} finally {
		    stream.close();
		}
	}

	private boolean logIn(String uid, String pid, JSONObject jObject) {
		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost("https://www.cellip.com/sv/minasidor/json/lync_app/login_back.html?user="+uid+"&pid="+pid);
		// Depends on your web service
		httppost.setHeader("Content-type", "application/json");
		boolean ret = false;
		InputStream inputStream = null;
		String result = null;
		try {
			HttpResponse response = httpclient.execute(httppost);           
			HttpEntity entity = response.getEntity();

			inputStream = entity.getContent();
			// json is UTF-8 by default
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}
			result = sb.toString();
			
		} catch (Exception e) { 
			// Oops
		}
		finally {
			try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
		}

		try {
			JSONObject res = new JSONObject(result);
			if (res.getInt("error") == 0) {
				jObject.put("loggedObject", res.toString());
				writeFile(jObject.toString());
				ret = true;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Login returning this string: \n" + ret);
		return ret;
	}

	//will launch the activity
	private Runnable mLaunchTask = new Runnable() {
		public void run() {
			System.out.println("RUNNING THE RUNNER FOR THE SERVICE");
			try {
				String prefs = readFile();
				JSONObject jObject = new JSONObject(prefs);
				JSONObject login = jObject.getJSONObject("login");
				if (login == null)
					return;
				if (!logIn(login.getString("uid"), login.getString("pid"), jObject))
					return;
			} catch (Exception e) {
				e.printStackTrace(System.out);
				
				return;
			}
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
		ConfigXmlParser parser = new ConfigXmlParser();
        	parser.parse(context);
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
					mHandler.postDelayed(mLaunchTask, 2000);
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
