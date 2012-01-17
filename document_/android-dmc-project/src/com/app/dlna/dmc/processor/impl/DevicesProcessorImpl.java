package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.message.header.DeviceTypeHeader;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.registry.RegistryListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;
import com.app.dlna.dmc.processor.upnp.CoreUpnpListener;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService;

public class DevicesProcessorImpl implements IDevicesProcessor {
	private static String TAG = DevicesProcessorImpl.class.getName();

	private Activity m_activity;

	private AndroidUpnpService m_upnpService;

	private RegistryListener m_upnplistener;

	private ServiceConnection m_serviceConnection;

	private boolean m_isServiceReady;

	private List<DevicesProcessorListener> m_listeners;

	private List<RemoteDevice> m_remoteDevices;

	public DevicesProcessorImpl() {
		m_isServiceReady = false;
		m_listeners = new ArrayList<DevicesProcessorListener>();
		m_upnplistener = new CoreUpnpListener(this);
		m_remoteDevices = new ArrayList<RemoteDevice>();
	}

	public IDevicesProcessor setActivity(Activity activity) {
		m_activity = activity;
		return this;
	}

	public void startUpnpService() {
		if (!m_isServiceReady) {
			m_serviceConnection = new ServiceConnection() {

				public void onServiceDisconnected(ComponentName name) {
					m_upnpService = null;
					m_isServiceReady = false;
				}

				public void onServiceConnected(ComponentName name, IBinder service) {
					m_upnpService = (AndroidUpnpService) service;
					m_upnpService.getRegistry().addListener(m_upnplistener);
					Log.i(TAG, "Upnp Service Ready");
					m_isServiceReady = true;
					fireOnStartCompleteEvent();
				}
			};

			Intent intent = new Intent(m_activity, CoreUpnpService.class);
			m_activity.bindService(intent, m_serviceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	public void stopUpnpService() {
		try {
			if (m_upnpService != null) {
				Log.e(TAG, "Stop upnp service");
				m_upnpService.getRegistry().removeListener(m_upnplistener);
				m_upnpService.getRegistry().shutdown();
				m_activity.unbindService(m_serviceConnection);
				m_serviceConnection = null;
				m_upnpService = null;
			}
		} catch (Exception ex) {

		}
	}

	public void searchAll() {
		if (m_isServiceReady == true) {
			Log.e(TAG, "Search invoke");
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search();
		}
	}

	public boolean isUpnpServiceReady() {
		return m_isServiceReady;
	}

	public void addListener(DevicesProcessorListener listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener)) {
				m_listeners.add(listener);
			}
		}
	}

	public void removeListener(DevicesProcessorListener listener) {
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
	public void remoteDeviceAdded(RemoteDevice device) {
		fireOnRemoteDeviceAdded(device);
		synchronized (m_remoteDevices) {
			m_remoteDevices.add(device);
		}
	}

	@Override
	public void remoteDeviceRemoved(RemoteDevice device) {
		fireOnRemoteDeviceRemoved(device);
		synchronized (m_remoteDevices) {
			m_remoteDevices.remove(device);
		}
	}

	@Override
	public RemoteDevice getRemoteDevice(String UDN) {
		synchronized (m_remoteDevices) {
			for (RemoteDevice device : m_remoteDevices) {
				if (device.getIdentity().getUdn().toString().compareTo(UDN) == 0)
					return device;
			}
			return null;
		}
	}

	private void fireOnRemoteDeviceAdded(RemoteDevice device) {
		synchronized (m_listeners) {
			for (DevicesProcessorListener listener : m_listeners) {
				listener.onRemoteDeviceAdded(device);
			}
		}
	}

	private void fireOnRemoteDeviceRemoved(RemoteDevice device) {
		synchronized (m_listeners) {
			for (DevicesProcessorListener listener : m_listeners) {
				listener.onRemoteDeviceRemoved(device);
			}
		}
	}

	private void fireOnStartCompleteEvent() {
		synchronized (m_listeners) {
			for (DevicesProcessorListener listener : m_listeners) {
				listener.onStartComplete();
			}
		}
	}

	@Override
	public void searchDMS() {
		if (m_isServiceReady == true) {
			Log.e(TAG, "Search invoke");
			DeviceType type = new DeviceType("schemas-upnp-org", "MediaServer", 1);
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
		}

	}

	@Override
	public void searchDMR() {
		if (m_isServiceReady == true) {
			Log.e(TAG, "Search invoke");
			DeviceType type = new DeviceType("schemas-upnp-org", "MediaRenderer", 1);
			m_upnpService.getRegistry().removeAllRemoteDevices();
			m_upnpService.getControlPoint().search(new DeviceTypeHeader(type));
		}

	}

}
