package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.message.header.DeviceTypeHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
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
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService;

public class UpnpProcessorImpl implements UpnpProcessor, RegistryListener {
	private static String TAG = UpnpProcessorImpl.class.getName();

	private Activity m_activity;

	private CoreUpnpService.Binder m_upnpService;

	private ServiceConnection m_serviceConnection;

	private List<UpnpProcessorListener> m_listeners;

	private List<RemoteDevice> m_remoteDevices;

	public UpnpProcessorImpl(UpnpListenerActivity activity) {
		m_activity = activity;
		m_listeners = new ArrayList<UpnpProcessorListener>();
		m_listeners.add(activity);
		m_remoteDevices = new ArrayList<RemoteDevice>();
	}

	public void bindUpnpService() {

		m_serviceConnection = new ServiceConnection() {

			public void onServiceDisconnected(ComponentName name) {
				m_upnpService = null;
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				m_upnpService = (CoreUpnpService.Binder) service;
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
		Log.e(TAG, "Search invoke");
		m_upnpService.getRegistry().removeAllRemoteDevices();
		m_upnpService.getControlPoint().search();
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
		return m_upnpService.getControlPoint();
	}

	@Override
	public RemoteDevice getRemoteDevice(String UDN) {
		synchronized (m_remoteDevices) {
			for (RemoteDevice device : m_upnpService.getRegistry().getRemoteDevices()) {
				if (device.getIdentity().getUdn().toString().compareTo(UDN) == 0)
					return device;
			}
			return null;
		}
	}

	private void fireOnStartCompleteEvent() {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onStartComplete();
			}
		}
	}

	@Override
	public void searchDMS() {
		Log.e(TAG, "Search invoke");
		DeviceType type = new DeviceType("schemas-upnp-org", "MediaServer", 1);
		if (m_upnpService != null) {
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
		} else {
			Log.e(TAG, "UPnP Service is null");
		}

	}

	@Override
	public void searchDMR() {
		Log.e(TAG, "Search invoke");
		DeviceType type = new DeviceType("schemas-upnp-org", "MediaRenderer", 1);
		m_upnpService.getRegistry().removeAllRemoteDevices();
		m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
	}

	@Override
	public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
		fireRemoteDeviceAddedEvent(device);
	}

	@Override
	public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
		// TODO Auto-generated method stub
	}

	@Override
	public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
		fireRemoteDeviceRemovedEvent(device);
	}

	@Override
	public void localDeviceAdded(Registry registry, LocalDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void localDeviceRemoved(Registry registry, LocalDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeShutdown(Registry registry) {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterShutdown() {
		// TODO Auto-generated method stub

	}

	private void fireRemoteDeviceAddedEvent(RemoteDevice remoteDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onRemoteDeviceAdded(remoteDevice);
			}
		}
	}

	private void fireRemoteDeviceRemovedEvent(RemoteDevice remoteDevice) {
		synchronized (m_listeners) {
			for (UpnpProcessorListener listener : m_listeners) {
				listener.onRemoteDeviceRemoved(remoteDevice);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Device> getDMSList() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getDevices(new DeviceType("schemas-upnp-org", "MediaServer"));
		return new ArrayList<Device>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Device> getDMRList() {
		if (m_upnpService != null)
			return m_upnpService.getRegistry().getDevices(new DeviceType("schemas-upnp-org", "MediaRenderer"));
		return new ArrayList<Device>();
	}
}
