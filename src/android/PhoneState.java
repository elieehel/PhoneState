package com.cellip.lyncapp;
import java.util.Collection;
import java.util.LinkedHashMap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginEntry;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.splashscreen.SplashScreen;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
* This class echoes a string called from JavaScript.
*/
public class PhoneState extends CordovaPlugin {

    private static final String TAG = "phonestate";
    
    private CallbackContext connectionCallbackContext;
    private boolean init = false;
	private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private CordovaInterface cordova;
    private CordovaWebView webView;
    private ListenToPhoneState listener;
    private TelephonyManager tManager;
    private Context context;
    //will care for all posts
    private Handler mHandler = new Handler();
    
    //will launch the activity
    private Runnable mLaunchTask = new Runnable() {
        public void run() {
            Intent it = new Intent("com.cellip.show.transfer");
            it.setComponent(new ComponentName(context.getPackageName(), MainActivity.class.getName()));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            it.setAction(Intent.ACTION_MAIN);
            it.addCategory(Intent.CATEGORY_LAUNCHER);
            //context.startActivity(it);
            //context.getApplicationContext().startActivity(it);
        }
     };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        this.connectionCallbackContext = callbackContext;
        this.connectionCallbackContext.sendPluginResult(result);
        
        if (action.equals("start")) {
            //Log.i(TAG, "Asked to start TelephonyManager, previous one is " + tManager);
	        //if (!init) {
	            init = true;
	            tManager = (TelephonyManager)cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
	            listener = new ListenToPhoneState();
	            tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	        //} else {
	            //tManager.listen(listener, PhoneStateListener.LISTEN_NONE);
	        //}
            
	        context = this.cordova.getActivity().getApplicationContext(); 
            //Log.i(TAG, "Also starting service from context " + context);
	        Intent intent = new Intent(context, CallStateTracker.class);
	        cordova.getActivity().startService(intent);
        } else if (action.equals("getnumber")) {
        	this.getNumber(callbackContext);
        } else if (action.equals("resetplugin")) {
        	/*Collection<PluginEntry> pluginEntries = webView.getPluginManager().getPluginEntries();
        	webView.getPluginManager().setPluginEntries(pluginEntries);*/
        	//webView.getPluginManager().addService("SplashScreen2"+Math.random(), "org.apache.cordova.splashscreen.SplashScreen");
        	//cp.initit();
        } else if (action.equals("hide")) {
            /*CordovaPlugin cp = webView.getPluginManager().getPlugin("SplashScreen");
            System.out.println("WE HAS PLUGIN " + cp);
            cp.onMessage("splashscreen", "hide");*/
        }

        
        /*if (action.equals("echo")) {
            String message = args.getString(0);
            this.echo(message, callbackContext);
            return true;
        }
        return false;*/
        return true;
    }

    private void echo(String message, CallbackContext callbackContext) {
        
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.connectionCallbackContext = null;
        this.cordova = cordova;
        
        this.webView = webView;
        
    }
    
    private void getNumber(CallbackContext callbackContext) {
    	if (callbackContext != null) {
    		TelephonyManager tm = (TelephonyManager)cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		
		boolean isGSM  = (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM);
		if (isGSM) {
			String phoneNumber = tm.getLine1Number();
			boolean hasSim = !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
    			callbackContext.success(hasSim + "|" + phoneNumber);
		} else {
			callbackContext.success("false|0");
		}
    	} else {
    		callbackContext.success("false|0");
    	}
    }
    
    private void sendUpdate(String type) {
        //Log.i(TAG, "asked to send update of type " + type + " and cbcontext is " + connectionCallbackContext);
        if (connectionCallbackContext != null) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, type);
            result.setKeepCallback(true);
            connectionCallbackContext.sendPluginResult(result);
        } else {
        }
        //webView.postMessage("watchingnetwork", "webview "+type);
    }
    
    private class ListenToPhoneState extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {

            //Log.i(TAG, "New state is " + state + ", last state was " + lastState + " for " + connectionCallbackContext);
            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                lastState = state;
                sendUpdate("idle");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //you will be here at **STEP 3**
                // you will be here when you cut call
                if (lastState == TelephonyManager.CALL_STATE_RINGING)
                	sendUpdate("busy");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                lastState = state;
                break;
            default:
                break;
            }
        }
    }
}
