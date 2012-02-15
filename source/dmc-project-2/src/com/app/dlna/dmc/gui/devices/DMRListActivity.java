package com.app.dlna.dmc.gui.devices;

import java.io.File;
import java.util.ArrayList;

import org.teleal.cling.model.meta.RemoteDevice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.gui.dmr.DMRControllerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.utility.Utility;

public class DMRListActivity extends UpnpListenerActivity {

	private static final String TAG = DMRListActivity.class.getName();
	private String m_url = null;
	private UpnpProcessor m_upnpProcessor = null;
	private ListView m_listView;
	private RemoteDMRArrayAdapter m_adapter;
	private String m_title;
	private Bundle m_extraInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmrlist_activitiy);
		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new RemoteDMRArrayAdapter(DMRListActivity.this, 0, 0, new ArrayList<RemoteDevice>());
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(onItemClickListener);
		m_upnpProcessor = new UpnpProcessorImpl(DMRListActivity.this);
		m_upnpProcessor.bindUpnpService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (m_upnpProcessor != null) {
			m_upnpProcessor.addListener(DMRListActivity.this);
			m_upnpProcessor.searchDMR();
		}
		// refresh();
	}

	@Override
	protected void onPause() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(DMRListActivity.this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.unbindUpnpService();
		}
		super.onDestroy();

	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			Intent intent = new Intent(DMRListActivity.this, DMRControllerActivity.class);
			intent.putExtra("URL", m_url);
			intent.putExtra("UDN", m_adapter.getItem(position).getIdentity().getUdn().toString());
			intent.putExtra("Title", m_title);
			intent.putExtra("ExtraInfo", m_extraInfo);
			startActivity(intent);
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh();
			return true;
		default:
			break;
		}
		return false;
	}

	private void refresh() {
		if (m_adapter != null && m_upnpProcessor != null) {
			m_adapter.clear();
			m_upnpProcessor.searchDMR();
		}
	}

	@Override
	public void onRemoteDeviceAdded(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_adapter != null) {
					Log.e(TAG, "Remote Device Removed");
					m_adapter.add(device);
				}
			}
		});
	}

	@Override
	public void onRemoteDeviceRemoved(final RemoteDevice device) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_adapter != null) {
					Log.e(TAG, "Remote Device Added");
					m_adapter.remove(device);
				}
			}
		});
	}

	@Override
	public void onStartComplete() {
		Toast.makeText(DMRListActivity.this, "Start upnp service complete", Toast.LENGTH_SHORT).show();
		Intent intent = getIntent();
		m_url = intent.getStringExtra("URL");
		m_title = intent.getStringExtra("Title");
		m_extraInfo = (Bundle) intent.getBundleExtra("ExtraInfo");
		if (m_url == null) {
			Bundle bundle = intent.getExtras();
			String fileuri = bundle.get(Intent.EXTRA_STREAM).toString();

			File file = new File(fileuri.substring(fileuri.indexOf("/mnt/sdcard/")));
			m_url = Utility.createLink(file);
			m_title = file.getName();
			Log.d(TAG, m_url);
		}
		if (m_url == null) {
			new AlertDialog.Builder(DMRListActivity.this).setTitle("Error").setMessage("URL is null")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DMRListActivity.this.finish();
						}
					}).show();
		}
		m_upnpProcessor.searchDMR();
	}
}
