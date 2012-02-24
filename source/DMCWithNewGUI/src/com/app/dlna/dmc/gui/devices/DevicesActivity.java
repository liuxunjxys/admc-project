package com.app.dlna.dmc.gui.devices;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.types.UDN;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class DevicesActivity extends UpnpListenerActivity {
	private static final String TAG = DevicesActivity.class.getName();
	private UpnpProcessor m_upnpProcessor = null;

	private ListView m_dmrList;
	private ListView m_dmsList;
	private DeviceArrayAdapter m_dmrAdapter;
	private DeviceArrayAdapter m_dmsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Devices onCreate");
		setContentView(R.layout.devices_activity);

		m_dmrList = (ListView) findViewById(R.id.dmrList);
		m_dmsList = (ListView) findViewById(R.id.dmsList);

		m_dmrAdapter = new DeviceArrayAdapter(DevicesActivity.this, 0);
		m_dmsAdapter = new DeviceArrayAdapter(DevicesActivity.this, 0);

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
				UDN udn = m_dmrAdapter.getItem(position).getIdentity().getUdn();
				m_upnpProcessor.setCurrentDMR(udn);
				synchronized (m_dmrAdapter) {
					m_dmrAdapter.setCurrentDMSUDN(udn.getIdentifierString());
					m_dmrAdapter.notifyDataSetChanged();
				}
			}
		}

	};

	private OnItemClickListener onDMSClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			synchronized (m_dmsAdapter) {
				UDN udn = m_dmsAdapter.getItem(position).getIdentity().getUdn();
				m_upnpProcessor.setCurrentDMS(udn);
				synchronized (m_dmsAdapter) {
					m_dmsAdapter.setCurrentDMSUDN(udn.getIdentifierString());
					m_dmsAdapter.notifyDataSetChanged();
				}
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

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceAdded(Device device) {
		super.onDeviceAdded(device);
		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			if (device.getType().getType().equals("MediaServer")) {
				addDMS(device);
			} else if (device.getType().getType().equals("MediaRenderer")) {
				addDMR(device);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceRemoved(Device device) {
		super.onDeviceRemoved(device);
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
			if (m_upnpProcessor.getCurrentDMS() != null) {
				m_dmsAdapter.setCurrentDMSUDN(m_upnpProcessor.getCurrentDMS().getIdentity().getUdn().getIdentifierString());
			} else {
				m_dmsAdapter.setCurrentDMSUDN("");
			}
		}

		synchronized (m_dmrAdapter) {
			m_dmrAdapter.clear();
			if (m_upnpProcessor.getCurrentDMR() != null) {
				m_dmrAdapter.setCurrentDMSUDN(m_upnpProcessor.getCurrentDMR().getIdentity().getUdn().getIdentifierString());
			} else {
				m_dmrAdapter.setCurrentDMSUDN("");
			}
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
					// int count = m_dmrAdapter.getCount();
					// for (int i = 0; i < count; ++i) {
					// Device oldDevice = m_dmrAdapter.getItem(i);
					// if (oldDevice.getIdentity().equals(device.getIdentity()))
					// {
					// m_dmrAdapter.remove(oldDevice);
					// m_dmrAdapter.insert(oldDevice, i);
					// return;
					// }
					// }
					if (device instanceof LocalDevice)
						m_dmrAdapter.insert(device, 0);
					else
						m_dmrAdapter.add(device);
				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	private void removeDMR(final Device device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_dmrAdapter) {
					m_dmrAdapter.remove(device);
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
					// int count = m_dmsAdapter.getCount();
					// for (int i = 0; i < count; ++i) {
					// Device oldDevice = m_dmsAdapter.getItem(i);
					// if (oldDevice.getIdentity().equals(device.getIdentity()))
					// {
					// m_dmsAdapter.remove(oldDevice);
					// m_dmsAdapter.insert(oldDevice, i);
					// return;
					// }
					// }
					if (device instanceof LocalDevice)
						m_dmsAdapter.insert(device, 0);
					else
						m_dmsAdapter.add(device);
				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	private void removeDMS(final Device device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_dmsAdapter) {
					m_dmsAdapter.remove(device);
				}
			}
		});
	}

}
