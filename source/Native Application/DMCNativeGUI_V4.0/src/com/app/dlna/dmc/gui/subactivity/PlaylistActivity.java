package com.app.dlna.dmc.gui.subactivity;

import android.app.Activity;
import android.os.Bundle;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.customview.playlist.PlaylistAllItemView;

public class PlaylistActivity extends Activity {

	private PlaylistAllItemView m_playlistAllItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_activity);
		m_playlistAllItem = (PlaylistAllItemView) findViewById(R.id.playlist_AllItem);
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_playlistAllItem.preparePlaylist();
	}

}
