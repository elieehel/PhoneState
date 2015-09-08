package com.cellip.lyncapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.os.Environment;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.ConfigXmlParser;

public class Prefs {

  private static Prefs instance = null;

  public static Prefs getInstance(Context context) {
    if (Prefs.instance == null)
      Prefs.instance = new Prefs(context);
    return Prefs.instance;
  }
  
  private Context context;
  private ConfigXmlParser parser;
  private JSONObject prefsObj;
  
  private Prefs(Context context) {
		context = getApplicationContext(); 
		parser = new ConfigXmlParser();
		parser.parse(context);
  }
  
  public JSONObject getPrefsObject(boolean reload) {
    if (reload)
      this.reloadPreferences();
    return this.prefsObj;
  }
  
  public void reloadPreferences() {
    this.prefsObj = new JSONObject(this.readFile());
  }
  
  private String readFile() {
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard, parser.getPreferences().getString("app_company", "cellip")+"/prefs");
		
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
			stream.write(text.getBytes());
		} finally {
			stream.close();
		}
	}

  

}
