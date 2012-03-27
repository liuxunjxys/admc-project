package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.Device;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;
import com.phonegap.DroidGap;

public class UpnpListenerDroidGapActivity extends DroidGap implements UpnpProcessorListener {

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

	@Override
	public void onNetworkChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouterError(String cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouterEnabledEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRouterDisabledEvent() {
		// TODO Auto-generated method stub

	}

}
