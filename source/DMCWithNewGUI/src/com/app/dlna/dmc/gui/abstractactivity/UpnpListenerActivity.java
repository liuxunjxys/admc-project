package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.RemoteDevice;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

import android.app.Activity;

public abstract class UpnpListenerActivity extends Activity implements UpnpProcessor.UpnpProcessorListener {

	@Override
	public void onRemoteDeviceAdded(RemoteDevice device) {

	}

	@Override
	public void onRemoteDeviceRemoved(RemoteDevice device) {

	}

	@Override
	public void onStartComplete() {

	}

}
