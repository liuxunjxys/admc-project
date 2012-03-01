package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.Device;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.phonegap.DroidGap;

public class UpnpListenerActivity extends DroidGap implements UpnpProcessor.UpnpProcessorListener {

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onStartComplete() {

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceAdded(Device device) {

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceRemoved(Device device) {

	}

}
