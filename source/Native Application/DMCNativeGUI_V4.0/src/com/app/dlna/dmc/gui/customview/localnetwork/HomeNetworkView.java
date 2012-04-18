package com.app.dlna.dmc.gui.customview.localnetwork;

import java.util.List;
import java.util.Map;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSProcessorListner;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class HomeNetworkView extends LinearLayout {
	private static final String TAG = HomeNetworkView.class.getName();
	private ListView m_listView;
	private LayoutInflater m_inflater;
	private HomeNetworkArrayAdapter m_adapter;
	private ProgressDialog m_progressDlg;
	private boolean m_loadMore;
	private boolean m_isRoot;
	private boolean m_isBrowsing;

	private HomeNetworkToolbar m_toolbar;

	@SuppressWarnings("rawtypes")
	public HomeNetworkView(Context context) {
		super(context);
		m_isBrowsing = false;
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_inflater.inflate(R.layout.cv_homenetwork, this);
		m_listView = (ListView) findViewById(R.id.lv_mediasource_browsing);
		m_adapter = new HomeNetworkArrayAdapter(context, 0);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(m_itemClick);
		m_listView.setOnScrollListener(m_scrollListener);
		m_progressDlg = new ProgressDialog(MainActivity.INSTANCE);
		m_progressDlg.setTitle("Loading");
		m_progressDlg.setMessage("Waiting for loading items");
		m_progressDlg.setCancelable(true);
		MainActivity.UPNP_PROCESSOR.addListener(m_upnpListener);

		m_toolbar = (HomeNetworkToolbar) findViewById(R.id.botToolbar);
		m_toolbar.setLocalNetworkView(this);

		for (Device device : MainActivity.UPNP_PROCESSOR.getDMSList()) {
			m_adapter.add(new AdapterItem(device));
		}
	}

	private OnScrollListener m_scrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			try {
				if (MainActivity.UPNP_PROCESSOR.getDMSProcessor() != null
						&& m_progressDlg != null
						&& !m_progressDlg.isShowing()
						&& firstVisibleItem + visibleItemCount == totalItemCount
						&& m_adapter.getItem(firstVisibleItem + visibleItemCount - 1).getData() instanceof DIDLObject
						&& ((DIDLObject) m_adapter.getItem(firstVisibleItem + visibleItemCount - 1).getData()).getId().equals(
								"-1")) {
					doLoadMoreItems();
				}
			} catch (Exception ex) {

			}
		}
	};

	private OnItemClickListener m_itemClick = new OnItemClickListener() {

		@SuppressWarnings("rawtypes")
		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			AdapterItem item = m_adapter.getItem(position);
			if (item.getData() instanceof Device) {
				UDN udn = ((Device) item.getData()).getIdentity().getUdn();
				MainActivity.UPNP_PROCESSOR.setCurrentDMS(udn);
				m_progressDlg.show();
				browse("0", 0);
				m_isBrowsing = true;
				m_toolbar.setBackButtonEnabled(true);
			} else {
				final DIDLObject object = (DIDLObject) item.getData();
				if (object instanceof Container) {
					if (((Container) object).getChildCount() != null && ((Container) object).getChildCount() == 0)
						Toast.makeText(getContext(), "Folder is empty", Toast.LENGTH_SHORT).show();
					else
						browse(object.getId(), 0);
				} else if (object instanceof Item) {
					if (object.getId().equals("-1")) {
						// load more items
						doLoadMoreItems();
					} else {
						addToPlaylist(object);
					}
				}
			}
		}

	};

	private void doLoadMoreItems() {
		m_loadMore = true;
		m_progressDlg.show();
		MainActivity.UPNP_PROCESSOR.getDMSProcessor().nextPage(m_browseListener);
	}

	private void browse(String id, int pageIndex) {
		Log.e(TAG, "Browse id = " + id);
		m_progressDlg.show();
		m_loadMore = false;
		MainActivity.UPNP_PROCESSOR.getDMSProcessor().browse(id, pageIndex, m_browseListener);
	}

	private void addToPlaylist(DIDLObject object) {
		if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor() == null) {
			Toast.makeText(MainActivity.INSTANCE, "Cannot get playlist processor", Toast.LENGTH_SHORT).show();
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
		if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().addItem(item)) {
			m_adapter.notifyDataSetChanged();
		} else {
			if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().isFull()) {
				Toast.makeText(MainActivity.INSTANCE, "Current playlist is full", Toast.LENGTH_SHORT).show();
			} else {
				MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().removeItem(item);
				m_adapter.notifyDataSetChanged();
			}
		}
	}

	private DMSProcessorListner m_browseListener = new DMSProcessorListner() {

		@Override
		public void onBrowseFail(final String message) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_progressDlg.dismiss();
					new AlertDialog.Builder(MainActivity.INSTANCE).setTitle("Error occurs").setMessage(message)
							.setPositiveButton("OK", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									m_isBrowsing = false;
									m_isRoot = false;
									m_toolbar.backToDeviceList();
								}
							}).show();

				}
			});
			Log.e(TAG, "On browse fail, message = " + message);
		}

		@Override
		public void onBrowseComplete(final String objectID, final boolean haveNext, boolean havePrev,
				final Map<String, List<? extends DIDLObject>> result) {
			Log.i(TAG, "browse complete: object id = " + objectID + " haveNext = " + haveNext + "; havePrev =" + havePrev
					+ "; result size = " + result.size());
			m_isRoot = objectID.equals("0");
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (m_progressDlg.isShowing()) {
						if (m_loadMore) {
							m_adapter.remove(m_adapter.getItem(m_adapter.getCount() - 1));
						} else
							m_adapter.clear();
						for (DIDLObject container : result.get("Containers"))
							m_adapter.add(new AdapterItem(container));

						for (DIDLObject item : result.get("Items"))
							m_adapter.add(new AdapterItem(item));

						prepareImageCache(result);

						if (haveNext) {
							Item item = new Item();
							item.setTitle("Load more result");
							item.setId("-1");
							m_adapter.add(new AdapterItem(item));
						}

						m_progressDlg.dismiss();
						if (!m_loadMore)
							m_listView.smoothScrollToPosition(0);
						m_adapter.notifyDataSetChanged();
					}
				}
			});

		}
	};

	private void prepareImageCache(final Map<String, List<? extends DIDLObject>> result) {
		m_adapter.cancelPrepareImageCache();
		m_adapter.prepareImageItemCache(result.get("Items"));
	}

	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (m_isRoot) {
				m_adapter.clear();
			}
			return true;
		}
		return false;
	};

	private UpnpProcessorListener m_upnpListener = new UpnpProcessorListener() {

		@Override
		public void onStartFailed() {

		}

		@Override
		public void onStartComplete() {

		}

		@Override
		public void onRouterError(String cause) {
		}

		@Override
		public void onRouterEnabledEvent() {

		}

		@Override
		public void onRouterDisabledEvent() {

		}

		@Override
		public void onNetworkChanged() {

		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onDeviceAdded(Device device) {
			if (device.getType().getNamespace().equals("schemas-upnp-org")) {
				if (device.getType().getType().equals("MediaServer")) {
					addDMS(device);
				}
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onDeviceRemoved(Device device) {
			if (device.getType().getNamespace().equals("schemas-upnp-org")) {
				if (device.getType().getType().equals("MediaServer")) {
					removeDMS(device);
				}
			}
		}
	};

	@SuppressWarnings("rawtypes")
	private void addDMS(final Device device) {
		MainActivity.INSTANCE.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_adapter) {
					if (m_isBrowsing)
						return;
					if (device instanceof LocalDevice)
						m_adapter.insert(new AdapterItem(device), 0);
					else
						m_adapter.add(new AdapterItem(device));
				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	private void removeDMS(final Device device) {
		MainActivity.INSTANCE.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (m_adapter) {
					if (m_isBrowsing)
						return;
					m_adapter.remove(new AdapterItem(device));
				}
			}
		});
	}

	public boolean isRoot() {
		return m_isRoot;
	}

	public boolean isBrowsing() {
		return m_isBrowsing;
	}

	public HomeNetworkArrayAdapter getListAdapter() {
		return m_adapter;
	}

	public void setBrowsing(boolean browsing) {
		m_isBrowsing = browsing;
	}

	public ProgressDialog getProgressDlg() {
		return m_progressDlg;
	}

	public void setLoadMore(boolean loadMore) {
		m_loadMore = loadMore;
	}

	public DMSProcessorListner getBrowseListener() {
		return m_browseListener;
	}

	public void updateListView() {
		m_adapter.notifyDataSetChanged();
	}

}
