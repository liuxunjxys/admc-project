package com.app.dlna.dmc.gui.abstractactivity;

import org.teleal.cling.model.meta.Device;

import android.app.Activity;

import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class UpnpListenerActivity extends Activity implements UpnpProcessor.UpnpProcessorListener {

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
