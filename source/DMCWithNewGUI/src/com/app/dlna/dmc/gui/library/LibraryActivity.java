package com.app.dlna.dmc.gui.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.DMSProcessorImpl;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSProcessorListner;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class LibraryActivity extends UpnpListenerActivity implements DMSProcessorListner {
	private static final String TAG = LibraryActivity.class.getName();
	private UpnpProcessor m_upnpProcessor;
	private DMSProcessor m_dmsProcessor;
	private DIDLObjectArrayAdapter m_adapter;
	private ListView m_listView;
	private ProgressDialog m_progressDlg;
	private List<String> m_traceID;
	private String m_currentDMSUDN;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmsbrowser_activity);
		m_adapter = new DIDLObjectArrayAdapter(LibraryActivity.this, 0);
		m_listView = (ListView) findViewById(R.id.lv_ServerContent);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(itemClickListener);

		m_upnpProcessor = new UpnpProcessorImpl(LibraryActivity.this);
		m_upnpProcessor.bindUpnpService();
		m_traceID = new ArrayList<String>();
		m_traceID.add("-1");
	}

	@Override
	protected void onResume() {
		Log.d(TAG, m_upnpProcessor != null ? "Upnp Processor != null" : "Upnp Processor == null");
		super.onResume();
		if (m_upnpProcessor != null && m_upnpProcessor.getCurrentDMS() != null) {
			String newUDN = m_upnpProcessor.getCurrentDMS().getIdentity().getUdn().toString();
			if (m_currentDMSUDN == null || !m_currentDMSUDN.equals(newUDN)) {
				m_currentDMSUDN = newUDN;
				m_traceID = new ArrayList<String>();
				m_traceID.add("-1");
				m_dmsProcessor = new DMSProcessorImpl(m_upnpProcessor);
				m_dmsProcessor.addListener(LibraryActivity.this);
				browse("0");
			}
		}
	}

	@Override
	protected void onPause() {
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(LibraryActivity.this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (m_upnpProcessor != null)
			m_upnpProcessor.unbindUpnpService();
		super.onDestroy();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			final DIDLObject object = m_adapter.getItem(position);
			if (object instanceof Container) {
				browse(object.getId());
			} else if (object instanceof Item) {
				addToPlaylist();
			}
		}
	};

	private void browse(String id) {
		Log.e(TAG, "Browse id = " + id);
		m_traceID.add(id);
		m_progressDlg = ProgressDialog.show(LibraryActivity.this, "Loading", "Loading...");
		m_dmsProcessor.browse(id);
		for (String _id : m_traceID) {
			Log.e(TAG, _id);
		}
	}

	protected void addToPlaylist() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBrowseComplete(final Map<String, List<? extends DIDLObject>> result) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_adapter.clear();

				for (DIDLObject container : result.get("Containers"))
					m_adapter.add(container);

				for (DIDLObject item : result.get("Items"))
					m_adapter.add(item);

				m_progressDlg.dismiss();
			}
		});

	}

	@Override
	public void onBrowseFail(final String message) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_progressDlg.dismiss();
				new AlertDialog.Builder(LibraryActivity.this).setTitle("Error occurs").setMessage(message)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								LibraryActivity.this.finish();
							}
						}).show();

			}
		});
	}

	@Override
	public void onBackPressed() {
		int traceSize = m_traceID.size();
		if (traceSize > 2) {
			String parentID = m_traceID.get(traceSize - 2);
			browse(parentID);
			m_traceID.remove(m_traceID.size() - 1);
			m_traceID.remove(m_traceID.size() - 1);
		} else {
			Toast.makeText(LibraryActivity.this,
					"You are in the root of this MediaServer. Press Back again to chose other MediaServer", Toast.LENGTH_SHORT)
					.show();
			final TabHost tabHost = ((TabActivity) getParent()).getTabHost();
			tabHost.setCurrentTab(3);
		}
	}

}
