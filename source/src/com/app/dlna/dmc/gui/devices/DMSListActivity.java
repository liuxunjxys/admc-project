package com.app.dlna.dmc.gui.devices;

import org.teleal.cling.model.meta.RemoteDevice;

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
import com.app.dlna.dmc.gui.dms.DMSBrowserActivity;
import com.app.dlna.dmc.gui.localcontent.BrowseLocalActivity;
import com.app.dlna.dmc.gui.ytcontent.YoutubeContentActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class DMSListActivity extends UpnpListenerActivity {

	protected static final String TAG = DMSListActivity.class.getName();
	private ListView m_lvDevices;
	private UpnpProcessor m_upnpProcessor;
	private RemoteDMSArrayAdapter m_adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmslist_activity);
		m_adapter = new RemoteDMSArrayAdapter(this, 0, 0);

		m_lvDevices = (ListView) findViewById(R.id.lv_ListRenderer);
		m_lvDevices.setAdapter(m_adapter);
		m_lvDevices.setOnItemClickListener(itemClickListener);
		m_upnpProcessor = new UpnpProcessorImpl(DMSListActivity.this);
		m_upnpProcessor.bindUpnpService();
	}

	@Override
	protected void onResume() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.addListener(DMSListActivity.this);
		}
		super.onResume();
	}

	protected void onPause() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(DMSListActivity.this);
		}
		super.onPause();
	};

	@Override
	protected void onDestroy() {
		if (m_upnpProcessor != null)
			m_upnpProcessor.unbindUpnpService();
		super.onDestroy();
	}

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
		case R.id.browselocal:
			switchToLocal();
			return true;
		case R.id.youtube:
			switchToYoutube();
			return true;
		default:
			break;
		}
		return false;
	}

	private void switchToYoutube() {
		Intent intent = new Intent(DMSListActivity.this, YoutubeContentActivity.class);
		DMSListActivity.this.startActivity(intent);
	}

	private void refresh() {
		m_adapter.clear();
		m_upnpProcessor.searchDMS();
	}

	private void switchToLocal() {
		Intent intent = new Intent(DMSListActivity.this, BrowseLocalActivity.class);
		DMSListActivity.this.startActivity(intent);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
			RemoteDevice device = m_adapter.getItem(position);
			Intent intent = new Intent(DMSListActivity.this, DMSBrowserActivity.class);
			intent.putExtra("RemoteServerUDN", device.getIdentity().getUdn().toString());
			DMSListActivity.this.startActivity(intent);
		}
	};

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
		Toast.makeText(DMSListActivity.this, "Start upnp service complete", Toast.LENGTH_SHORT).show();
		refresh();
	}
}