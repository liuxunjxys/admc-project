package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.RemoteDevice;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;

import android.app.TabActivity;

public class UpnpListenerTabActivity extends TabActivity implements UpnpProcessorListener {

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
