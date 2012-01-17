package com.app.dlna.dmc.gui.devices;

import java.util.ArrayList;

import org.teleal.cling.model.meta.RemoteDevice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.dmr.DMRControllerActivity;
import com.app.dlna.dmc.processor.ProcessorFactory;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor.DevicesProcessorListener;

public class DMRListActivity extends Activity implements DevicesProcessorListener {

	private static final String TAG = DMRListActivity.class.getName();
	private String m_url = null;
	private IDevicesProcessor m_devicesProcessor = null;
	private ListView m_listView;
	private RemoteDMRArrayAdapter m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmrlist_activitiy);

		m_devicesProcessor = ProcessorFactory.getProcessorInstance(DMRListActivity.this);
		m_devicesProcessor.searchDMR();

		Intent intent = getIntent();
		m_url = intent.getStringExtra("URL");

		if (m_url == null) {
			new AlertDialog.Builder(DMRListActivity.this).setTitle("Error").setMessage("URL is null")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DMRListActivity.this.finish();
						}
					}).show();
		} else {
			m_listView = (ListView) findViewById(R.id.listView);
			m_adapter = new RemoteDMRArrayAdapter(DMRListActivity.this, 0, 0, new ArrayList<RemoteDevice>());
			m_listView.setAdapter(m_adapter);
			m_listView.setOnItemClickListener(onItemClickListener);
		}
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			Intent intent = new Intent(DMRListActivity.this, DMRControllerActivity.class);
			intent.putExtra("URL", m_url);
			intent.putExtra("UDN", m_adapter.getItem(position).getIdentity().getUdn().toString());
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		m_devicesProcessor.addListener(DMRListActivity.this);
		super.onResume();
	}

	public void onRefreshClick(View view) {
		Log.e(TAG, "Refresh");
		refresh();
	}

	private void refresh() {
		m_devicesProcessor.searchDMR();
	}

	@Override
	public void onRemoteDeviceAdded(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.e(TAG, "Remote Device Removed");
				m_adapter.add(device);
			}
		});
	}

	@Override
	public void onRemoteDeviceRemoved(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Log.e(TAG, "Remote Device Added");
				m_adapter.remove(device);
			}
		});
	}

	@Override
	protected void onPause() {
		m_devicesProcessor.removeListener(DMRListActivity.this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStartComplete() {
		m_devicesProcessor.searchDMR();
	}
}
