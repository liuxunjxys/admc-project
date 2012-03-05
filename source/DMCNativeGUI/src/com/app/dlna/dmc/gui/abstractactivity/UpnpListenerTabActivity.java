package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.Device;

import android.app.TabActivity;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;

public class UpnpListenerTabActivity extends TabActivity implements UpnpProcessorListener {

	@Override
	public void onStartComplete() {

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceAdded(Device device) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceRemoved(Device device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartFailed() {

	}

}
