package com.app.dlna.dmc.gui.customview.localnetwork;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import app.dlna.controller.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class HomeNetworkToolbar extends LinearLayout {
	private HomeNetworkView m_localNetworkView;
	private ImageView m_btn_back;
	private HomeNetworkArrayAdapter m_homeNetworkAdapter;

	public HomeNetworkToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_homenetwork_toolbar,
				this);
		m_btn_back = ((ImageView) findViewById(R.id.btn_back));
		m_btn_back.setOnClickListener(onBackClick);
		m_btn_back.setOnLongClickListener(onBackLongclick);
		m_btn_back.setEnabled(false);

		((ImageView) findViewById(R.id.btn_selectAll)).setOnClickListener(onSelectAll);
		((ImageView) findViewById(R.id.btn_deselectAll)).setOnClickListener(onDeselectAll);
	}

	public void setLocalNetworkView(HomeNetworkView localNetworkView) {
		m_localNetworkView = localNetworkView;
		m_homeNetworkAdapter = localNetworkView.getListAdapter();
	}

	private OnClickListener onBackClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (m_localNetworkView.isBrowsing()) {
				if (m_localNetworkView.isRoot()) {
					// return to device list
					backToDeviceList();
				} else {
					upOneLevel();
				}
			}
		}
	};

	private OnLongClickListener onBackLongclick = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			backToDeviceList();
			return true;
		}
	};

	@SuppressWarnings("rawtypes")
	public void backToDeviceList() {
		m_homeNetworkAdapter.clear();
		for (Device dms : MainActivity.UPNP_PROCESSOR.getDMSList()) {
			if (dms instanceof LocalDevice)
				m_homeNetworkAdapter.insert(new AdapterItem(dms), 0);
			else
				m_homeNetworkAdapter.add(new AdapterItem(dms));
		}
		m_localNetworkView.setBrowsing(false);
		m_homeNetworkAdapter.cancelPrepareImageCache();
		m_btn_back.setEnabled(false);
	}

	private void upOneLevel() {
		if (m_localNetworkView.isRoot())
			Toast.makeText(getContext(), "You are in root of this data source", Toast.LENGTH_SHORT).show();
		else if (MainActivity.UPNP_PROCESSOR.getDMSProcessor() != null) {
			m_localNetworkView.getProgressDlg().show();
			m_localNetworkView.setLoadMore(false);
			MainActivity.UPNP_PROCESSOR.getDMSProcessor().back(m_localNetworkView.getBrowseListener());
		}
	}

	public void setBackButtonEnabled(boolean enabled) {
		m_btn_back.setEnabled(enabled);
	}

	private void modifyPlaylist(String type) {
		final PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor == null) {
			Toast.makeText(MainActivity.INSTANCE, "Cannot get playlist processor", Toast.LENGTH_SHORT).show();
			return;
		}
		final int count = m_homeNetworkAdapter.getCount();
		for (int i = 0; i < count; ++i) {
			if (m_homeNetworkAdapter.getItem(i).getData() instanceof Item) {
				final Item didlItem = (Item) m_homeNetworkAdapter.getItem(i).getData();
				if (didlItem.getResources() == null || didlItem.getResources().size() == 0)
					continue;
				final PlaylistItem playlistItem = new PlaylistItem();
				playlistItem.setTitle(didlItem.getTitle());
				playlistItem.setUrl(didlItem.getResources().get(0).getValue());
				if (didlItem instanceof AudioItem) {
					playlistItem.setType(Type.AUDIO);
				} else if (didlItem instanceof VideoItem) {
					playlistItem.setType(Type.VIDEO);
				} else {
					playlistItem.setType(Type.IMAGE);
				}
				if (type.equals("selectAll"))
					playlistProcessor.addItem(playlistItem);
				else if (type.equals("deselectAll"))
					playlistProcessor.removeItem(playlistItem);
				m_homeNetworkAdapter.notifyDataSetChanged();
			}
		}
	}

	private OnClickListener onSelectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist("selectAll");
		}
	};

	private OnClickListener onDeselectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist("deselectAll");
		}
	};
}
