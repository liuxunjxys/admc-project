package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService.CoreUpnpServiceBinder;

public class UpnpProcessorImpl implements UpnpProcessor, RegistryListener {
	private static String TAG = UpnpProcessorImpl.class.getName();

	private Activity m_activity;

	private CoreUpnpServiceBinder m_upnpService;

	private ServiceConnection m_serviceConnection;

	private List<UpnpProcessorListener> m_listeners;

	public UpnpProcessorImpl(UpnpListenerActivity activity) {
		m_activity = activity;
		m_listeners = new ArrayList<UpnpProcessorListener>();
		m_listeners.add(activity);
	}

	public void bindUpnpService() {

		m_serviceConnection = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName name) {
				m_upnpService = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				m_upnpService = (CoreUpnpServiceBinder) service;
				m_upnpService.getRegistry().addListener(UpnpProcessorImpl.this);
				Log.i(TAG, "Upnp Service Ready");
				fireOnStartCompleteEvent();
				m_upnpService.getControlPoint().search();
			}
		};

		Intent intent = new Intent(m_activity, CoreUpnpService.class);
		m_activity.getApplicationContext().bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public void unbindUpnpService() {
		try {
			Log.e(TAG, "Unbind to service");
			if (m_upnpService != null) {
				try {
					m_activity.getApplicationContext().unbindService(m_serviceConnection);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void searchAll() {
		if (m_upnpService != null) {
			Log.e(TAG, "Search invoke");
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search();
		} else {
			Log.e(TAG, "Upnp Service = null");
		}
	}

	public void addListener(UpnpProcessorListener listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener)) {
				m_listeners.add(listener);
			}
		}
	}

	public void removeListener(UpnpProcessorListener listener) {
		synchronized (m_listeners) {
			if (m_listeners.contains(listener)) {
				m_listeners.remove(listener);
			}
		}
	}

	public ControlPoint getControlPoint() {
		return m_upnpService != null ? m_upnpService.getControlPoint() : null;
	}

	private void fireOnStartCompleteEvent() {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onStartComplete();
			}
		}
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		fireDeviceAddedEvent(device);
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		fireDeviceRemovedEvent(device);
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		Log.e(TAG, "Local Device Add:" + device.toString());
		fireDeviceAddedEvent(device);
	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device) {
		Log.e(TAG, "Local Device Removed:" + device.toString());
		fireDeviceRemovedEvent(device);
	}

	@Override
	public void beforeShutdown(Registry registry) {
	}

	@Override
	public void afterShutdown() {
	}

	@SuppressWarnings("rawtypes")
	private void fireDeviceAddedEvent(Device device) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onDeviceAdded(device);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void fireDeviceRemovedEvent(Device device) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onDeviceRemoved(device);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Device> getDMSList() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getDevices(new DeviceType("schemas-upnp-org", "MediaServer"));
		else {
			Log.e(TAG, "Upnp Service = null");
		}
		return new ArrayList<Device>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Device> getDMRList() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getDevices(new DeviceType("schemas-upnp-org", "MediaRenderer"));
		else {
			Log.e(TAG, "Upnp Service = null");
		}
		return new ArrayList<Device>();
	}

	@Override
	public void setCurrentDMS(UDN uDN) {
		if (m_upnpService != null)
			m_upnpService.setCurrentDMS(uDN);
		else {
			Log.e(TAG, "Upnp Service = null");
		}
	}

	@Override
	public void setCurrentDMR(UDN uDN) {
		if (m_upnpService != null)
			m_upnpService.setCurrentDMR(uDN);
		else {
			Log.e(TAG, "Upnp Service = null");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Device getCurrentDMS() {
		return m_upnpService != null ? m_upnpService.getCurrentDMS() : null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Device getCurrentDMR() {
		return m_upnpService != null ? m_upnpService.getCurrentDMR() : null;
	}

	@Override
	public PlaylistProcessor getPlaylistProcessor() {
		return m_upnpService != null ? m_upnpService.getPlaylistProcessor() : null;
	}

	@Override
	public DMSProcessor getDMSProcessor() {
		return m_upnpService != null ? m_upnpService.getDMSProcessor() : null;
	}

	@Override
	public DMRProcessor getDMRProcessor() {
		return m_upnpService != null ? m_upnpService.getDMRProcessor() : null;
	}
}
