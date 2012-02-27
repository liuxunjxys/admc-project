package com.app.dlna.dmc.gui.devices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.UDN;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
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
	private LinearLayout m_ll_dms;
	private LinearLayout m_ll_dmr;

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

		m_ll_dms = (LinearLayout) findViewById(R.id.ll_dms);
		m_ll_dmr = (LinearLayout) findViewById(R.id.ll_dmr);

		m_upnpProcessor = new UpnpProcessorImpl(DevicesActivity.this);
		m_upnpProcessor.bindUpnpService();

		// restoreState();

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
		// saveState();
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

	public void onDMSButtonClick(View view) {
		if (m_ll_dms.getVisibility() == View.VISIBLE)
			return;
		AlphaAnimation dmsAnimation = new AlphaAnimation(0f, 1f);
		dmsAnimation.setDuration(500);
		dmsAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_ll_dms.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_ll_dms.setVisibility(View.VISIBLE);
			}
		});
		m_ll_dms.startAnimation(dmsAnimation);

		AlphaAnimation dmrAnimation = new AlphaAnimation(1f, 0f);
		dmrAnimation.setDuration(500);
		dmrAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_ll_dmr.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_ll_dmr.setVisibility(View.GONE);
			}
		});
		m_ll_dmr.startAnimation(dmrAnimation);

	}

	public void onDMRButtonClick(View view) {
		if (m_ll_dmr.getVisibility() == View.VISIBLE)
			return;
		AlphaAnimation dmsAnimation = new AlphaAnimation(1f, 0f);
		dmsAnimation.setDuration(500);
		dmsAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_ll_dms.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_ll_dms.setVisibility(View.GONE);
			}
		});
		m_ll_dms.startAnimation(dmsAnimation);

		AlphaAnimation dmrAnimation = new AlphaAnimation(0f, 1f);
		dmrAnimation.setDuration(500);
		dmrAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_ll_dmr.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_ll_dmr.setVisibility(View.VISIBLE);
			}
		});
		m_ll_dmr.startAnimation(dmrAnimation);
	}

	// @SuppressWarnings("rawtypes")
	// private void saveState() {
	// ObjectOutputStream outputStream = null;
	//
	// try {
	// outputStream = new ObjectOutputStream(openFileOutput("devices_cache", Context.MODE_PRIVATE));
	// if (m_dmsAdapter != null) {
	// synchronized (m_dmsAdapter) {
	// int dmsCount = m_dmsAdapter.getCount();
	// for (int i = 0; i < dmsCount; ++i) {
	// Device device = m_dmsAdapter.getItem(i);
	// if (device instanceof RemoteDevice) {
	// RemoteDevice remote = (RemoteDevice) device;
	// outputStream.writeObject(remote);
	// }
	// }
	// }
	// }
	//
	// if (m_dmrAdapter != null) {
	// synchronized (m_dmsAdapter) {
	// int dmrCount = m_dmrAdapter.getCount();
	// for (int i = 0; i < dmrCount; ++i) {
	// Device device = m_dmrAdapter.getItem(i);
	// if (device instanceof RemoteDevice) {
	// RemoteDevice remote = (RemoteDevice) device;
	// outputStream.writeObject((RemoteDevice) remote);
	// }
	// }
	// }
	// }
	//
	// } catch (FileNotFoundException ex) {
	// ex.printStackTrace();
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// } finally {
	// try {
	// if (outputStream != null) {
	// outputStream.flush();
	// outputStream.close();
	// }
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	//
	// }

	// @SuppressWarnings("rawtypes")
	// private void restoreState() {
	// ObjectInputStream inputStream = null;
	// m_dmsAdapter.clear();
	// m_dmrAdapter.clear();
	// try {
	// inputStream = new ObjectInputStream(openFileInput("devices_cache"));
	//
	// while (true) {
	// Object object = inputStream.readObject();
	// if (object instanceof RemoteDevice) {
	// RemoteDevice device = (RemoteDevice) object;
	// if (device.getType().getNamespace().equals("schemas-upnp-org")) {
	// if (device.getType().getType().equals("MediaServer")) {
	// addDMS(device);
	// } else if (device.getType().getType().equals("MediaRenderer")) {
	// addDMR(device);
	// }
	// }
	// }
	// }
	// } catch (FileNotFoundException ex) {
	// ex.printStackTrace();
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (inputStream != null) {
	// inputStream.close();
	// }
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }
	// }
}
