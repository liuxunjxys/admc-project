package com.app.dlna.dmc.gui.customview.localnetwork;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalDevice;

import android.app.ProgressDialog;
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
import com.app.dlna.dmc.gui.subactivity.MediaSourceActivity;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor.DMSAddRemoveContainerListener;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;

public class HomeNetworkToolbar extends LinearLayout {
	private static final String ACTION_DESELECT_ALL = "Deselect All";
	private static final String ACTION_SELECT_ALL = "Select All";
	private static final String ACTION_SELECT_ALL_CONTAINER = "Select All Container";
	private static final String ACTION_DESELECT_ALL_CONTAINER = "Deselect All Container";

	private HomeNetworkView m_localNetworkView;
	private ImageView m_btn_back;
	private HomeNetworkArrayAdapter m_homeNetworkAdapter;

	public HomeNetworkToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_homenetwork_toolbar, this);
		m_btn_back = ((ImageView) findViewById(R.id.btn_back));
		m_btn_back.setOnClickListener(onBackClick);
		m_btn_back.setOnLongClickListener(onBackLongclick);
		m_btn_back.setEnabled(false);

		((ImageView) findViewById(R.id.btn_selectAll)).setOnClickListener(onSelectAll);
		((ImageView) findViewById(R.id.btn_deselectAll)).setOnClickListener(onDeselectAll);
		((ImageView) findViewById(R.id.btn_containerSelectAll)).setOnClickListener(onContainerSeselectAll);
		((ImageView) findViewById(R.id.btn_containerDeselectAll)).setOnClickListener(onContainerDeselectAll);
		((ImageView) findViewById(R.id.btn_showhide)).setOnClickListener(onShowHideClick);
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
			Toast.makeText(MainActivity.INSTANCE, "Cannot process playlist", Toast.LENGTH_SHORT).show();
		}
		final DMSProcessor dmsProcessor = MainActivity.UPNP_PROCESSOR.getDMSProcessor();
		if (dmsProcessor == null) {
			Toast.makeText(MainActivity.INSTANCE, "Cannot contact to dms server", Toast.LENGTH_SHORT).show();
		}

		if (type.equals(ACTION_SELECT_ALL)) {
			dmsProcessor.addCurrentItemsToPlaylist(playlistProcessor, m_playlistModifyListener);
		} else if (type.equals(ACTION_DESELECT_ALL)) {
			dmsProcessor.removeCurrentItemsFromPlaylist(playlistProcessor, m_playlistModifyListener);
		} else if (type.equals(ACTION_SELECT_ALL_CONTAINER)) {
			dmsProcessor.addAllToPlaylist(playlistProcessor, m_playlistModifyListener);
		} else if (type.equals(ACTION_DESELECT_ALL_CONTAINER)) {
			dmsProcessor.removeAllFromPlaylist(playlistProcessor, m_playlistModifyListener);
		}

	}

	private DMSAddRemoveContainerListener m_playlistModifyListener = new DMSAddRemoveContainerListener() {
		ProgressDialog m_progreProgressDialog;

		@Override
		public void onActionStart() {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_progreProgressDialog = ProgressDialog
							.show(getContext(), "Processing", "Waiting to add all items");
					m_progreProgressDialog.setCancelable(false);
				}
			});

		}

		@Override
		public void onActionFail(final Exception ex) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_progreProgressDialog.dismiss();
					Toast.makeText(getContext(), "Error occur: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
					m_homeNetworkAdapter.notifyDataSetChanged();
				}
			});

		}

		@Override
		public void onActionComplete() {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_progreProgressDialog.dismiss();
					Toast.makeText(getContext(), "Add all items complete", Toast.LENGTH_SHORT).show();
					m_homeNetworkAdapter.notifyDataSetChanged();
				}
			});

		}
	};

	private OnClickListener onSelectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist(ACTION_SELECT_ALL);
		}
	};

	private OnClickListener onDeselectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist(ACTION_DESELECT_ALL);
		}
	};

	private OnClickListener onContainerSeselectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist(ACTION_SELECT_ALL_CONTAINER);
		}
	};
	private OnClickListener onContainerDeselectAll = new OnClickListener() {

		@Override
		public void onClick(View v) {
			modifyPlaylist(ACTION_DESELECT_ALL_CONTAINER);
		}
	};
	private OnClickListener onShowHideClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getContext() instanceof MediaSourceActivity) {
				MediaSourceActivity activity = (MediaSourceActivity) v.getContext();
				if (activity.isCompactRendererShowing()) {
					activity.hideRendererCompactView();
					((ImageView) v).setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.ic_btn_navigate_up));
				} else {
					activity.showRendererCompactView();
					((ImageView) v).setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.ic_btn_navigate_down));
				}
			}
		}
	};
}
