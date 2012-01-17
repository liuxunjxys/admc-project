package com.app.dlna.dmc.processor.interfaces;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.RemoteDevice;

import android.app.Activity;

public interface IDevicesProcessor {
	void addListener(DevicesProcessorListener listener);

	void removeListener(DevicesProcessorListener listener);

	void startUpnpService();

	void stopUpnpService();

	boolean isUpnpServiceReady();

	IDevicesProcessor setActivity(Activity activity);

	void searchAll();

	void searchDMS();

	void searchDMR();

	ControlPoint getControlPoint();

	void remoteDeviceAdded(RemoteDevice device);

	void remoteDeviceRemoved(RemoteDevice device);

	RemoteDevice getRemoteDevice(String UDN);

	public interface DevicesProcessorListener {
		void onRemoteDeviceAdded(RemoteDevice device);

		void onRemoteDeviceRemoved(RemoteDevice device);

		void onStartComplete();
	}
}
