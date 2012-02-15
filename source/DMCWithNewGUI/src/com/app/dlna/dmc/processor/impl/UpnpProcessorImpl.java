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
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerTabActivity;
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
	
	public UpnpProcessorImpl(UpnpListenerTabActivity activity) {
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
	public void searchDMS() {
		if (m_upnpService != null) {
			Log.e(TAG, "Search invoke");
			DeviceType type = new DeviceType("schemas-upnp-org", "MediaServer", 1);
			if (m_upnpService != null) {
				m_upnpService.getRegistry().removeAllRemoteDevices();
				m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
			} else {
				Log.e(TAG, "UPnP Service is null");
			}
		} else {
			Log.e(TAG, "Upnp Service = null");
		}
	}

	@Override
	public void searchDMR() {
		if (m_upnpService != null) {
			Log.e(TAG, "Search invoke");
			DeviceType type = new DeviceType("schemas-upnp-org", "MediaRenderer", 1);
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
		} else {
			Log.e(TAG, "Upnp Service = null");
		}
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

	@Override
	public RemoteDevice getCurrentDMS() {
		return m_upnpService != null ? m_upnpService.getCurrentDMS() : null;
	}

	@Override
	public RemoteDevice getCurrentDMR() {
		return m_upnpService != null ? m_upnpService.getCurrentDMR() : null;
	}
}
