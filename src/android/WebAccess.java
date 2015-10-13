package com.cellip.lyncapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import android.os.AsyncTask;


public class WebAccess extends AsyncTask<String, Void, Void> {

	private WeakReference<Cb> mRef;
	private String result = null;

	public WebAccess(Cb activity) {
		mRef = new WeakReference<Cb>(activity);
	}

	@Override
	protected Void doInBackground(String... params) {
		DefaultHttpClient   httpclient = new DefaultHttpClient(new BasicHttpParams());
		HttpPost httppost = new HttpPost(params[0]);
		// Depends on your web service
		httppost.setHeader("Content-type", "application/json");
		boolean ret = false;
		InputStream inputStream = null;
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
			e.printStackTrace(System.out);
		}
		finally {
			try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void aVoid) {
		Cb activity = mRef.get();
		if (activity == null) {
			// the activity reference was cleared, 
			// lets forget about it
		}
		else {
			// lets update the activity with the results
			// of the task
			activity.callback(this.result);
		}
	}
}

