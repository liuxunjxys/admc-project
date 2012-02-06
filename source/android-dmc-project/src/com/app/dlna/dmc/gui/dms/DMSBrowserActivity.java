package com.app.dlna.dmc.gui.dms;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.DIDLObject.Property.UPNP.ICON;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.devices.DMRListActivity;
import com.app.dlna.dmc.processor.ProcessorFactory;
import com.app.dlna.dmc.processor.interfaces.IDMSProcessor;
import com.app.dlna.dmc.processor.interfaces.IDMSProcessor.DMSProcessorListner;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;

public class DMSBrowserActivity extends Activity implements DMSProcessorListner {
	private static final String TAG = DMSBrowserActivity.class.getName();
	private IDevicesProcessor m_processor;
	private IDMSProcessor m_dmsProcessor;
	private RemoteDevice m_remoteDevice;
	private DIDLObjectArrayAdapter m_adapter;
	private ListView m_listView;
	private ProgressDialog m_progressDlg;
	private List<String> m_traceID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dmsbrowser_activity);
		m_traceID = new ArrayList<String>();
		m_traceID.add("-1");
		m_processor = ProcessorFactory.getProcessorInstance(null);
		String UDN = getIntent().getStringExtra("RemoteServerUDN");
		m_remoteDevice = m_processor.getRemoteDevice(UDN);

		if (m_remoteDevice == null) {
			Toast.makeText(this, "Cannot get server info", Toast.LENGTH_SHORT).show();
			this.finish();
		} else {
			m_dmsProcessor = ProcessorFactory.getDMSProcessorInstance(m_remoteDevice);
			if (m_dmsProcessor == null) {
				this.finish();
			} else {
				m_dmsProcessor.addListener(DMSBrowserActivity.this);
				browse("0");
				m_adapter = new DIDLObjectArrayAdapter(DMSBrowserActivity.this, 0, 0, new ArrayList<DIDLObject>());
				m_listView = (ListView) findViewById(R.id.lv_ServerContent);
				m_listView.setAdapter(m_adapter);
				m_listView.setOnItemClickListener(itemClickListener);
			}
		}
	}

	private void browse(String id) {
		Log.e(TAG, "Browse id = " + id);
		m_traceID.add(id);
		m_progressDlg = ProgressDialog.show(DMSBrowserActivity.this, "Loading", "Loading...");
		m_dmsProcessor.browse(id);
		for (String _id : m_traceID) {
			Log.e(TAG, _id);
		}
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			final DIDLObject object = m_adapter.getItem(position);
			if (object instanceof Container) {
				browse(object.getId());
			} else if (object instanceof Item) {
				String[] selectMode = { "Play local", "Play on remote device" };

				new AlertDialog.Builder(DMSBrowserActivity.this).setTitle("Select Play Mode").setItems(selectMode, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							playOnLocalDevice(object);
							break;
						case 1:
							playOnRemoteDevice(object);
							break;
						default:
							break;
						}
					}

				}).create().show();
			}
		}
	};

	private void playOnLocalDevice(DIDLObject object) {
		try {
			String url = object.getResources().get(0).getValue();
			String extension = MimeTypeMap.getFileExtensionFromUrl(url);
			String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
			mediaIntent.setDataAndType(Uri.parse(url), mimeType);
			startActivity(mediaIntent);
		} catch (Exception ex) {
			new AlertDialog.Builder(DMSBrowserActivity.this).setTitle("Error").setMessage("Cannot file any program to play this content")
					.setPositiveButton("OK", null).create().show();
		}
	}

	private void playOnRemoteDevice(DIDLObject object) {
		String url = null;
		if (object.getResources() != null && object.getResources().get(0) != null) {
			url = object.getResources().get(0).getValue();
		}
		Intent intent = new Intent(DMSBrowserActivity.this, DMRListActivity.class);
		intent.putExtra("URL", url);
		intent.putExtra("Title", object.getTitle());
		Bundle extraInfo = new Bundle();

		try {
			if (object.getFirstProperty(ICON.class) != null)
				extraInfo.putString("IconURL", object.getFirstProperty(ICON.class).getValue().toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		intent.putExtra("ExtraInfo", extraInfo);

		Log.e(TAG, "Item title = " + object.getTitle());
		DMSBrowserActivity.this.startActivity(intent);
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
				new AlertDialog.Builder(DMSBrowserActivity.this).setTitle("Error occurs").setMessage(message)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								DMSBrowserActivity.this.finish();
							}
						}).show();

			}
		});
	}

	@Override
	protected void onDestroy() {
		if (m_dmsProcessor != null)
			m_dmsProcessor.removeListener(DMSBrowserActivity.this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		int traceSize = m_traceID.size();
		if (traceSize > 2) {
			String parentID = m_traceID.get(traceSize - 2);
			browse(parentID);
			m_traceID.remove(traceSize - 1);
			m_traceID.remove(traceSize - 1);
		} else {
			super.onBackPressed();
		}
	}
}
