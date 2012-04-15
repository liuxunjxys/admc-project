package com.app.dlna.dmc.gui.library;

import java.util.List;
import java.util.Map;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.app.dlna.controller.nativegui.R;
import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSProcessorListner;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class LibraryActivity extends UpnpListenerActivity {
	private static final String TAG = LibraryActivity.class.getName();
	private DMSProcessor m_dmsProcessor;
	private DIDLObjectArrayAdapter m_adapter;
	private ListView m_listView;
	private ProgressDialog m_progressDlg;
	private String m_currentDMSUDN;
	private PlaylistProcessor m_playlistProcessor;
	private EditText m_filterText;
	private boolean m_loadMore;
	protected boolean m_isRoot;

	private DMSProcessorListner m_listener = new DMSProcessorListner() {

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
		public void onBrowseComplete(final String objectID, final boolean haveNext, boolean havePrev,
				final Map<String, List<? extends DIDLObject>> result) {
			Log.i(TAG, "browse complete: object id = " + objectID + " haveNext = " + haveNext + "; havePrev =" + havePrev
					+ "; result size = " + result.size());
			m_isRoot = objectID.equals("0");
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (m_progressDlg.isShowing()) {
						if (m_loadMore) {
							m_adapter.remove(m_adapter.getItem(m_adapter.getCount() - 1));
						} else
							m_adapter.clear();
						for (DIDLObject container : result.get("Containers"))
							m_adapter.add(container);

						for (DIDLObject item : result.get("Items"))
							m_adapter.add(item);

						if (haveNext) {
							Item item = new Item();
							item.setTitle("Load more result");
							item.setId("-1");
							m_adapter.add(item);
						}

						m_progressDlg.dismiss();
						m_adapter.notifyDataSetChanged();
					}
				}
			});

		}
	};

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
		m_listView.setOnItemLongClickListener(itemLongClickListener);

		m_filterText = (EditText) findViewById(R.id.search_box);
		m_filterText.addTextChangedListener(filterTextWatcher);
		m_progressDlg = new ProgressDialog(LibraryActivity.this);
		m_progressDlg.setTitle("Loading");
		m_progressDlg.setMessage("Waiting for loading items");
		m_progressDlg.setCancelable(true);

		m_loadMore = false;
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
		if (MainActivity.UPNP_PROCESSOR != null && MainActivity.UPNP_PROCESSOR.getCurrentDMS() != null) {
			m_playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			m_adapter.setPlaylistProcessor(m_playlistProcessor);
			browseRootContainer();
		}
	}

	private void browseRootContainer() {
		String newUDN = MainActivity.UPNP_PROCESSOR.getCurrentDMS().getIdentity().getUdn().toString();
		if (m_currentDMSUDN == null || !m_currentDMSUDN.equals(newUDN)) {
			m_currentDMSUDN = newUDN;
			m_dmsProcessor = MainActivity.UPNP_PROCESSOR.getDMSProcessor();
			browse("0", 0);
		} else {
			m_adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Library onPause");
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(m_filterText.getWindowToken(), 0);
		if (MainActivity.UPNP_PROCESSOR != null) {
			MainActivity.UPNP_PROCESSOR.removeListener(LibraryActivity.this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Library onDestroy");
		m_filterText.removeTextChangedListener(filterTextWatcher);
		super.onDestroy();
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			final DIDLObject object = m_adapter.getItem(position);
			if (object instanceof Container) {
				browse(object.getId(), 0);
			} else if (object instanceof Item) {
				if (object.getId().equals("-1")) {
					// load more items
					m_loadMore = true;
					m_progressDlg.show();
					m_dmsProcessor.nextPage(m_listener);
				} else {
					addToPlaylist(object);
				}
			}
		}
	};

	private OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg3) {
			if (MainActivity.UPNP_PROCESSOR.getCurrentDMS() != null
					&& MainActivity.UPNP_PROCESSOR.getCurrentDMS() instanceof RemoteDevice)
				downloadItem(m_adapter.getItem(position));
			return true;
		}
	};

	private void downloadItem(final DIDLObject item) {
		if (item instanceof Item) {
			new AlertDialog.Builder(LibraryActivity.this).setTitle("Download content")
					.setMessage("Do you want to download this content?").setPositiveButton("Yes", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.UPNP_PROCESSOR.getDownloadProcessor().startDownload(item);
						}
					}).setNegativeButton("No", null).create().show();
		}
	}

	private void browse(String id, int pageIndex) {
		m_filterText.setText("");
		Log.e(TAG, "Browse id = " + id);
		m_progressDlg.show();
		m_loadMore = false;
		m_dmsProcessor.browse(id, pageIndex, m_listener);
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
	public void onBackPressed() {
		upOneLevel();
	}

	private void upOneLevel() {
		if (m_isRoot)
			Toast.makeText(LibraryActivity.this, "You are in root of this data source", Toast.LENGTH_SHORT).show();
		else if (m_dmsProcessor != null) {
			m_progressDlg.show();
			m_loadMore = false;
			m_dmsProcessor.back(m_listener);
		}
	}

	public void onButtonBackClick(View view) {
		upOneLevel();
	}

	public void onRefreshButtonClick(View view) {

	}

	public void onSelectAllButtonClick(View view) {
		if (m_playlistProcessor == null) {
			Toast.makeText(LibraryActivity.this, "Cannot get playlist processor", Toast.LENGTH_SHORT).show();
			return;
		}
		int count = m_adapter.getCount();
		for (int i = 0; i < count; ++i) {
			DIDLObject object = m_adapter.getItem(i);
			if (object.getId().equals("-1"))
				continue;
			if (object instanceof Item) {
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
				m_playlistProcessor.addItem(item);
				m_adapter.notifyDataSetChanged();
			}
		}
	}

	public void onDeselectAllButtonClick(View view) {
		if (m_playlistProcessor == null) {
			Toast.makeText(LibraryActivity.this, "Cannot get playlist processor", Toast.LENGTH_SHORT).show();
			return;
		}
		int count = m_adapter.getCount();
		for (int i = 0; i < count; ++i) {
			DIDLObject object = m_adapter.getItem(i);
			if (object.getId().equals("-1"))
				continue;
			if (object instanceof Item) {
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
				m_playlistProcessor.removeItem(item);
				m_adapter.notifyDataSetChanged();
			}
		}
	}
}
