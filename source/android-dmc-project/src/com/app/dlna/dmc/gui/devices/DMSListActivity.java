package com.app.dlna.dmc.gui.devices;

import org.teleal.cling.model.meta.RemoteDevice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.dms.DMSBrowserActivity;
import com.app.dlna.dmc.processor.ProcessorFactory;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor.DevicesProcessorListener;

public class DMSListActivity extends Activity implements DevicesProcessorListener {

	protected static final String TAG = DMSListActivity.class.getName();
	private ListView m_lvDevices;
	private IDevicesProcessor m_processor;
	private RemoteDMSArrayAdapter m_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmslist_activity);

		m_processor = ProcessorFactory.getProcessorInstance(DMSListActivity.this);

		m_processor.startUpnpService();

		m_adapter = new RemoteDMSArrayAdapter(this, 0, 0);

		m_lvDevices = (ListView) findViewById(R.id.lv_ListRenderer);
		m_lvDevices.setAdapter(m_adapter);
		m_lvDevices.setOnItemClickListener(itemClickListener);
	}

	@Override
	protected void onResume() {
		m_processor.setActivity(DMSListActivity.this);
		m_processor.addListener(this);
		super.onResume();
	}

	public void onRefreshClick(View view) {
		m_adapter.clear();
		m_processor.searchDMS();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
			RemoteDevice device = m_adapter.getItem(position);
			Intent intent = new Intent(DMSListActivity.this, DMSBrowserActivity.class);
			intent.putExtra("RemoteServerUDN", device.getIdentity().getUdn().toString());
			DMSListActivity.this.startActivity(intent);
		}
	};

	protected void onPause() {
		m_processor.removeListener(this);
		super.onPause();
	};

	@Override
	protected void onDestroy() {
		m_processor.stopUpnpService();
		m_processor = null;
		super.onDestroy();
	}

	public void onRemoteDeviceAdded(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			public void run() {
				Log.e(TAG, "Remote device added");
				m_adapter.add(device);
			}
		});
	}

	@Override
	public void onRemoteDeviceRemoved(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			public void run() {
				Log.e(TAG, "Remote device removed");
				m_adapter.remove(device);
			}
		});
	}

	@Override
	public void onStartComplete() {
		m_processor.searchDMS();
	}
}