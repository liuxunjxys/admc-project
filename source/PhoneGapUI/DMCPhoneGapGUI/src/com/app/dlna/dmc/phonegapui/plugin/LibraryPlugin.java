package com.app.dlna.dmc.phonegapui.plugin;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.app.dlna.dmc.phonegapui.MainActivity;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class LibraryPlugin extends Plugin {
	private static final String TAG = LibraryPlugin.class.getName();
	private static final String ACTION_BROWSE = "browse";
	private static final String ACTION_BACK = "back";
	private static final String ACTION_FILTER = "filter";

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		if (ACTION_BROWSE.equals(action)) {
			if (MainActivity.UPNP_PROCESSOR == null) {
				return new PluginResult(Status.ERROR, "Cannot get UPNP Processor");
			} else {
				try {
					DMSProcessor dmsProcessor = MainActivity.UPNP_PROCESSOR.getDMSProcessor();
					String objectID = data.getString(0);
					Log.i(TAG, "Object id");
				} catch (JSONException e) {
					return new PluginResult(Status.JSON_EXCEPTION);
				}
			}
		} else if (ACTION_FILTER.equals(action)) {

		}

		return null;
	}

}
