package com.app.dlna.dmc.gui;

import android.os.Bundle;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class UIWithPhonegapActivity extends UpnpListenerActivity {
	private static final String TAG = UIWithPhonegapActivity.class.getSimpleName();
	public static UpnpProcessor UPNP_PROCESSOR = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UPNP_PROCESSOR = new UpnpProcessorImpl(UIWithPhonegapActivity.this);
		UPNP_PROCESSOR.bindUpnpService();
		super.loadUrl("file:///android_asset/www/views/devices-view/devices-view.html");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		UPNP_PROCESSOR.unbindUpnpService();
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
	}

}
