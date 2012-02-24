package com.app.dlna.dmc.gui.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSProcessorListner;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class LibraryActivity extends UpnpListenerActivity implements DMSProcessorListner {
	private static final String TAG = LibraryActivity.class.getName();
	private UpnpProcessor m_upnpProcessor;
	private DMSProcessor m_dmsProcessor;
	private DIDLObjectArrayAdapter m_adapter;
	private ListView m_listView;
	private ProgressDialog m_progressDlg;
	private List<String> m_traceID;
	private String m_currentDMSUDN;
	private PlaylistProcessor m_playlistProcessor;
	private EditText m_filterText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Library onCreate");
		setContentView(R.layout.library_activity);
		m_adapter = new DIDLObjectArrayAdapter(LibraryActivity.this, 0);
		m_listView = (ListView) findViewById(R.id.lv_ServerContent);
		m_listView.setTextFilterEnabled(true);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(itemClickListener);

		m_upnpProcessor = new UpnpProcessorImpl(LibraryActivity.this);
		m_upnpProcessor.bindUpnpService();
		m_traceID = new ArrayList<String>();
		m_traceID.add("-1");

		m_filterText = (EditText) findViewById(R.id.search_box);
		m_filterText.addTextChangedListener(filterTextWatcher);

	}

	private TextWatcher filterTextWatcher = new TextWatcher() {

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			m_adapter.getFilter().filter(s);
		}

	};

	@Override
	protected void onResume() {
		Log.i(TAG, "Library onResume");
		super.onResume();
		if (m_upnpProcessor != null && m_upnpProcessor.getCurrentDMS() != null) {
			m_playlistProcessor = m_upnpProcessor.getPlaylistProcessor();
			m_adapter.setPlaylistProcessor(m_playlistProcessor);
			browseRootContainer();
		}
	}

	private void browseRootContainer() {
		String newUDN = m_upnpProcessor.getCurrentDMS().getIdentity().getUdn().toString();
		if (m_currentDMSUDN == null || !m_currentDMSUDN.equals(newUDN)) {
			m_currentDMSUDN = newUDN;
			m_traceID = new ArrayList<String>();
			m_traceID.add("-1");
			m_dmsProcessor = m_upnpProcessor.getDMSProcessor();
			m_dmsProcessor.addListener(LibraryActivity.this);
			browse("0");
		} else {
			m_adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Library onPause");
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(m_filterText.getWindowToken(), 0);
		if (m_upnpProcessor != null) {
			m_upnpProcessor.removeListener(LibraryActivity.this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Library onDestroy");
		m_upnpProcessor.unbindUpnpService();
		m_filterText.removeTextChangedListener(filterTextWatcher);

		super.onDestroy();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			final DIDLObject object = m_adapter.getItem(position);
			if (object instanceof Container) {
				browse(object.getId());
			} else if (object instanceof Item) {
				addToPlaylist(object);
			}
		}
	};

	private void browse(String id) {
		m_filterText.setText("");
		Log.e(TAG, "Browse id = " + id);
		m_traceID.add(id);
		m_progressDlg = ProgressDialog.show(LibraryActivity.this, "Loading", "Loading...");
		m_progressDlg.setCancelable(true);
		m_dmsProcessor.browse(id);
		for (String _id : m_traceID) {
			Log.e(TAG, _id);
		}
	}

	protected void addToPlaylist(DIDLObject object) {
		if (m_playlistProcessor == null) {
			Toast.makeText(LibraryActivity.this, "Cannot get playlist processor", Toast.LENGTH_SHORT).show();
			return;
		}
		PlaylistItem item = new PlaylistItem();
		item.setTitle(object.getTitle());
		item.setUrl(object.getResources().get(0).getValue());
		if (object instanceof AudioItem) {
			item.setType(Type.AUDIO);
		} else if (object instanceof VideoItem) {
			item.setType(Type.VIDEO);
		} else {
			item.setType(Type.IMAGE);
		}
		if (m_playlistProcessor.addItem(item)) {
			m_adapter.notifyDataSetChanged();
		} else {
			if (m_playlistProcessor.isFull()) {
				Toast.makeText(LibraryActivity.this, "Current playlist is full", Toast.LENGTH_SHORT).show();
			} else {
				m_playlistProcessor.removeItem(item);
				m_adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onBrowseComplete(final Map<String, List<? extends DIDLObject>> result) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_progressDlg.isShowing()) {
					m_adapter.clear();

					for (DIDLObject container : result.get("Containers"))
						m_adapter.add(container);

					for (DIDLObject item : result.get("Items"))
						m_adapter.add(item);

					m_progressDlg.dismiss();
				} else {
					m_traceID.remove(m_traceID.size() - 1);
				}
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
		upOneLevel();
	}

	private void upOneLevel() {
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
			finish();
		}
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		if (m_upnpProcessor != null && m_upnpProcessor.getCurrentDMS() != null) {
			m_playlistProcessor = m_upnpProcessor.getPlaylistProcessor();
			m_adapter.setPlaylistProcessor(m_playlistProcessor);
			browseRootContainer();
		}
	}

	public void onButtonBackClick(View view) {
		upOneLevel();
	}
}
