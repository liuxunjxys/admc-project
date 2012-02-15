package com.app.dlna.dmc.processor.interfaces;

import java.util.Collection;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;

public interface UpnpProcessor {
	void addListener(UpnpProcessorListener listener);

	void removeListener(UpnpProcessorListener listener);

	void bindUpnpService();

	void unbindUpnpService();

	void searchAll();

	void searchDMS();

	void searchDMR();

	@SuppressWarnings("rawtypes")
	Collection<Device> getDMSList();

	@SuppressWarnings("rawtypes")
	Collection<Device> getDMRList();

	ControlPoint getControlPoint();

	RemoteDevice getRemoteDevice(String UDN);

	public interface UpnpProcessorListener {

		void onRemoteDeviceAdded(RemoteDevice device);

		void onRemoteDeviceRemoved(RemoteDevice device);

		void onStartComplete();
	}
}
