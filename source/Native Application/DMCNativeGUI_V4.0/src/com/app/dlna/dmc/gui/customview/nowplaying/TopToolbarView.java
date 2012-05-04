package com.app.dlna.dmc.gui.customview.nowplaying;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
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
	private ImageView m_btn_playlistItemDropdown;
	private boolean m_flagPlaylistItem = true;
	private boolean m_flagPlaylist = true;

	public TopToolbarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_nowplayling_top, this);

		m_tv_currentPlaylistName = (TextView) findViewById(R.id.tv_playlistName);

		m_spinner_playlist = (Spinner) findViewById(R.id.spinner_playlist);
		m_playlistAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlist.setAdapter(m_playlistAdapter);
		m_spinner_playlist.setOnItemSelectedListener(m_playlistSelected);

		m_spinner_playlistItem = (Spinner) findViewById(R.id.playlistItem);
		m_playlistItemAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlistItem.setAdapter(m_playlistItemAdapter);
		m_spinner_playlistItem.setOnItemSelectedListener(m_playlistItemSelected);

		m_btn_playlistItemDropdown = (ImageView) findViewById(R.id.btn_fakeDropdown);
		m_btn_playlistItemDropdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_spinner_playlistItem.performClick();
			}
		});

		m_tv_currentPlaylistName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_spinner_playlist.performClick();
			}
		});

		updateToolbar();
	}

	private OnItemSelectedListener m_playlistItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			if (m_flagPlaylistItem) {
				m_flagPlaylistItem = !m_flagPlaylistItem;
				return;
			}
			PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			if (playlistProcessor == null)
				return;
			playlistProcessor.setCurrentItem(position);
			// DMRProcessor dmrProcessor =
			// MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			// if (dmrProcessor != null) {
			// dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem());
			// }
			NowPlayingActivity activity = (NowPlayingActivity) getContext();
			activity.updateItemInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener m_playlistSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			if (m_flagPlaylist) {
				m_flagPlaylist = !m_flagPlaylist;
				return;
			}
			PlaylistProcessor playlistProcessor = PlaylistManager.getPlaylistProcessor((Playlist) m_playlistAdapter
					.getItem(position).getData());
			if (playlistProcessor.getAllItems().size() == 0) {
				Toast.makeText(getContext(), "Playlist is empty", Toast.LENGTH_SHORT).show();
				return;
			}

			MainActivity.UPNP_PROCESSOR.setPlaylistProcessor(playlistProcessor);
			MainActivity.UPNP_PROCESSOR.getDMRProcessor().setPlaylistProcessor(playlistProcessor);
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
		m_playlistAdapter.clear();
		for (Playlist playlist : PlaylistManager.getAllPlaylist())
			m_playlistAdapter.add(new AdapterItem(playlist));
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			m_spinner_playlist
					.setSelection(m_playlistAdapter.getPosition(new AdapterItem(playlistProcessor.getData())));
			m_tv_currentPlaylistName.setText(playlistProcessor.getData().getName());
			updatePlaylistItemSpinner();
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor != null)
				dmrProcessor.setPlaylistProcessor(playlistProcessor);
		}
	}

	public void updatePlaylistItemSpinner() {
		m_playlistItemAdapter.clear();
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			for (PlaylistItem item : playlistProcessor.getAllItems())
				m_playlistItemAdapter.add(new AdapterItem(item));
			setCurrentSpinnerSelected(playlistProcessor.getCurrentItem());
		}

	}

	public void setCurrentSpinnerSelected(PlaylistItem item) {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor.getAllItems().size() > 0)
			m_spinner_playlistItem.setSelection(m_playlistItemAdapter.getPosition(new AdapterItem(playlistProcessor
					.getCurrentItem())));
	}
}
