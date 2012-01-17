package com.app.dlna.dmc.processor.upnp;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.DefaultRegistryListener;
import org.teleal.cling.registry.Registry;

import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;

public class CoreUpnpListener extends DefaultRegistryListener {

	static String TAG = CoreUpnpListener.class.getName();

	private IDevicesProcessor m_processor;

	public CoreUpnpListener(IDevicesProcessor devicesProcessor) {
		m_processor = devicesProcessor;
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		super.remoteDeviceAdded(registry, device);
		Log.e(TAG, "Remote device added ; Device Name = " + device.getDetails().getFriendlyName() + "; Device Serial Number = "
				+ device.getDetails().getSerialNumber());
		m_processor.remoteDeviceAdded(device);

	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		super.remoteDeviceRemoved(registry, device);
		Log.e(TAG, "Remote device removed ; Device Name = " + device.getDetails().getFriendlyName() + "; Device Serial Number = "
				+ device.getDetails().getSerialNumber());
		m_processor.remoteDeviceRemoved(device);
	}

	// @Override
	// public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
	// Log.e(TAG, "Remote Device Updated");
	// super.remoteDeviceUpdated(registry, device);
	// synchronized (m_processor.getRemoteDevices()) {
	// m_processor.getRemoteDevices().add(device);
	// ((DevicesProcessorImpl) m_processor).fireDevicesListChangedEvent(device);
	// int position = m_processor.getRemoteDevices().indexOf(device);
	// m_processor.getRemoteDevices().remove(device);
	// m_processor.getRemoteDevices().add(position, device);
	// }
	// }

}
