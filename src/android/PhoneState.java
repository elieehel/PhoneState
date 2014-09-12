package com.cellip.lyncapp;

import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.IntentFilter;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.content.BroadcastReceiver;

/**
* This class echoes a string called from JavaScript.
*/
public class PhoneState extends CordovaPlugin {
	
	private CallbackContext connectionCallbackContext;
	private boolean init = false;
	private CordovaInterface cordova;
	private CordovaWebView webView;
	private ListenToPhoneState listener;
	private TelephonyManager tManager;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    	
    	PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        this.connectionCallbackContext = callbackContext;
        this.connectionCallbackContext.sendPluginResult(result);
        
        if (!init) {
        	init = true;
        	tManager = (TelephonyManager)cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        	listener = new ListenToPhoneState();
        	tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        } else {
        	tManager.listen(listener, PhoneStateListener.LISTEN_NONE);
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
    
    private void sendUpdate(String type) {
    	if (connectionCallbackContext != null) {
    		PluginResult result = new PluginResult(PluginResult.Status.OK, type);
    		result.setKeepCallback(true);
    		connectionCallbackContext.sendPluginResult(result);
    	}
    	//webView.postMessage("watchingnetwork", "webview "+type);
    }
    
    private class ListenToPhoneState extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
            	sendUpdate("idle");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                    //you will be here at **STEP 3**
                 // you will be here when you cut call
                sendUpdate("busy");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                break;
            default:
                break;
            }
        }

    }
}
