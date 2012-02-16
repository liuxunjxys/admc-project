package com.app.dlna.dmc.gui.youtube;

import android.os.Bundle;
import android.util.Log;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;

public class YoutubeActivity extends UpnpListenerActivity {
	private static final String TAG = YoutubeActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Youtube onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "Youtube onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "Youtube onPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Youtube onDestroy");
	}
}
