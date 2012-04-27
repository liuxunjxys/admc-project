package com.app.dlna.dmc.gui.customview.nowplaying;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.gui.customview.adapter.CustomArrayAdapter;
import com.app.dlna.dmc.gui.subactivity.NowPlayingActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.Playlist;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistManager;

public class TopToolbarView extends LinearLayout {

	protected static final String TAG = TopToolbarView.class.getName();

	private Spinner m_spinner_playlist;
	private Spinner m_spinner_playlistItem;
	private CustomArrayAdapter m_playlistAdapter;
	private CustomArrayAdapter m_playlistItemAdapter;
	private TextView m_tv_currentPlaylistName;
	private ImageView m_btn_fakeDropdown;

	public TopToolbarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_nowplayling_top, this);

		m_tv_currentPlaylistName = (TextView) findViewById(R.id.tv_playlistName);

		m_spinner_playlist = (Spinner) findViewById(R.id.spinner_playlist);
		m_playlistAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlist.setAdapter(m_playlistAdapter);
		m_spinner_playlist.setOnItemSelectedListener(m_itemSelected);

		m_spinner_playlistItem = (Spinner) findViewById(R.id.playlistItem);
		m_playlistItemAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlistItem.setAdapter(m_playlistItemAdapter);
		m_spinner_playlistItem.setOnItemSelectedListener(m_playlistItemSelected);

		m_btn_fakeDropdown = (ImageView) findViewById(R.id.btn_fakeDropdown);
		m_btn_fakeDropdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_spinner_playlistItem.performClick();
			}
		});

		updateToolbar();
	}

	OnItemSelectedListener m_playlistItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			if (playlistProcessor == null)
				return;
			playlistProcessor.setCurrentItem(position);
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor != null) {
				dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem().getUrl());
			}
			NowPlayingActivity activity = (NowPlayingActivity) getContext();
			activity.updateItemInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener m_itemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			Log.e(TAG, "Select item at " + position);
			MainActivity.UPNP_PROCESSOR.setPlaylistProcessor(PlaylistManager
					.getPlaylistProcessor((Playlist) m_playlistAdapter.getItem(position).getData()));
			m_tv_currentPlaylistName.setText(MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getData().getName());
			updatePlaylistItemSpinner();
			NowPlayingActivity activity = (NowPlayingActivity) getContext();
			activity.updatePlaylist();
			activity.updateItemInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};

	public void updateToolbar() {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			m_playlistAdapter.clear();
			for (Playlist playlist : PlaylistManager.getAllPlaylist())
				m_playlistAdapter.add(new AdapterItem(playlist));
			m_tv_currentPlaylistName.setText(playlistProcessor.getData().getName());
			updatePlaylistItemSpinner();
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor != null)
				dmrProcessor.setPlaylistProcessor(playlistProcessor);
		}
	}

	public void updatePlaylistItemSpinner() {
		m_playlistItemAdapter.clear();
		for (PlaylistItem item : MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems())
			m_playlistItemAdapter.add(new AdapterItem(item));
	}

}
