package com.app.dlna.dmc.processor.interfaces;

import java.util.Collection;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;

public interface UpnpProcessor {
	void addListener(UpnpProcessorListener listener);

	void removeListener(UpnpProcessorListener listener);

	void bindUpnpService();

	void unbindUpnpService();

	void setCurrentDMS(UDN uDN);

	void setCurrentDMR(UDN uDN);
	
	Registry getRegistry();

	@SuppressWarnings("rawtypes")
	Device getCurrentDMS();

	@SuppressWarnings("rawtypes")
	Device getCurrentDMR();

	PlaylistProcessor getPlaylistProcessor();

	@SuppressWarnings("rawtypes")
	Collection<Device> getDMSList();

	@SuppressWarnings("rawtypes")
	Collection<Device> getDMRList();

	ControlPoint getControlPoint();

	DMSProcessor getDMSProcessor();

	DMRProcessor getDMRProcessor();

	DownloadProcessor getDownloadProcessor();

	public interface UpnpProcessorListener {

		@SuppressWarnings("rawtypes")
		void onDeviceAdded(Device device);

		@SuppressWarnings("rawtypes")
		void onDeviceRemoved(Device device);

		void onStartComplete();

		void onStartFailed();

		void onNetworkChanged();

		void onRouterError(String cause);

		void onRouterEnabledEvent();

		void onRouterDisabledEvent();

	}

}
