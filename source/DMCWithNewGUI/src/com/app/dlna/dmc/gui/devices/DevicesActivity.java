package com.app.dlna.dmc.gui.devices;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class DevicesActivity extends UpnpListenerActivity {
	private static final String TAG = DevicesActivity.class.getName();
	private UpnpProcessor m_upnpProcessor = null;

	private ListView m_dmrList;
	private ListView m_dmsList;
	private DMRArrayAdapter m_dmrAdapter;
	private DMSArrayAdapter m_dmsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Devices onCreate");
		setContentView(R.layout.devices_activity);

		m_dmrList = (ListView) findViewById(R.id.dmrList);
		m_dmsList = (ListView) findViewById(R.id.dmsList);

		m_dmrAdapter = new DMRArrayAdapter(DevicesActivity.this, 0);
		m_dmsAdapter = new DMSArrayAdapter(DevicesActivity.this, 0);

		m_dmrList.setOnItemClickListener(onDMRClick);
		m_dmsList.setOnItemClickListener(onDMSClick);

		m_dmrList.setAdapter(m_dmrAdapter);
		m_dmsList.setAdapter(m_dmsAdapter);

		m_upnpProcessor = new UpnpProcessorImpl(DevicesActivity.this);
		m_upnpProcessor.bindUpnpService();
	}

	private OnItemClickListener onDMRClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			synchronized (m_dmrAdapter) {
				m_upnpProcessor.setCurrentDMR(m_dmrAdapter.getItem(position).getIdentity().getUdn());
			}
		}

	};

	private OnItemClickListener onDMSClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			synchronized (m_dmsAdapter) {
				m_upnpProcessor.setCurrentDMS(m_dmsAdapter.getItem(position).getIdentity().getUdn());
			}
		}

	};

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "Devices onResume");
		m_upnpProcessor.addListener(DevicesActivity.this);
		refresh();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "Devices onPause");
		m_upnpProcessor.removeListener(DevicesActivity.this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Devices onDestroy");
		m_upnpProcessor.unbindUpnpService();
	}

	@Override
	public void onRemoteDeviceAdded(RemoteDevice device) {
		super.onRemoteDeviceAdded(device);
		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			if (device.getType().getType().equals("MediaServer")) {
				addDMS(device);
			} else if (device.getType().getType().equals("MediaRenderer")) {
				addDMR(device);
			}
		}
	}

	@Override
	public void onRemoteDeviceRemoved(RemoteDevice device) {
		super.onRemoteDeviceRemoved(device);
		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			if (device.getType().getType().equals("MediaServer")) {
				removeDMS(device);
			} else if (device.getType().getType().equals("MediaRenderer")) {
				removeDMR(device);
			}
		}
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		refresh();
	}

	@SuppressWarnings("rawtypes")
	private void refresh() {
		synchronized (m_dmsAdapter) {
			m_dmsAdapter.clear();
		}

		synchronized (m_dmrAdapter) {
			m_dmrAdapter.clear();
		}

		for (Device device : m_upnpProcessor.getDMSList()) {
			addDMS(device);
		}

		for (Device device : m_upnpProcessor.getDMRList()) {
			addDMR(device);
		}
	}

	@SuppressWarnings("rawtypes")
	private void addDMR(final Device device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_dmrAdapter) {
					if (device instanceof RemoteDevice) {
						int count = m_dmrAdapter.getCount();
						for (int i = 0; i < count; ++i) {
							RemoteDevice remoteDevice = m_dmrAdapter.getItem(i);
							if (remoteDevice.getIdentity().equals(device.getIdentity())) {
								m_dmrAdapter.remove(remoteDevice);
								m_dmrAdapter.insert(remoteDevice, i);
								return;
							}
						}
						m_dmrAdapter.add((RemoteDevice) device);
					}

				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	private void removeDMR(final Device device) {
		if (device instanceof RemoteDevice)
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					synchronized (m_dmrAdapter) {
						m_dmrAdapter.remove((RemoteDevice) device);
					}
				}
			});
	}

	@SuppressWarnings("rawtypes")
	private void addDMS(final Device device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_dmsAdapter) {
					if (device instanceof RemoteDevice) {
						int count = m_dmsAdapter.getCount();
						for (int i = 0; i < count; ++i) {
							RemoteDevice remoteDevice = m_dmsAdapter.getItem(i);
							if (remoteDevice.getIdentity().equals(device.getIdentity())) {
								m_dmsAdapter.remove(remoteDevice);
								m_dmsAdapter.insert(remoteDevice, i);
								return;
							}
						}
						m_dmsAdapter.add((RemoteDevice) device);
					}
				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	private void removeDMS(final Device device) {
		if (device instanceof RemoteDevice)
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					synchronized (m_dmsAdapter) {
						m_dmsAdapter.remove((RemoteDevice) device);
					}
				}
			});
	}

}
