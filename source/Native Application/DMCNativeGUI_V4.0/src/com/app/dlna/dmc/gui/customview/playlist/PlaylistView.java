package com.app.dlna.dmc.gui.customview.playlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class PlaylistView extends LinearLayout {

	private ListView m_listView;
	private PlaylistItemArrayAdapter m_playlistItemAdapter;
	private PlaylistToolbar m_playlistToolbar;

	public PlaylistView(Context context) {
		super(context);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_playlist_allitem, this);
		m_listView = (ListView) findViewById(R.id.lv_playlist);
		m_playlistItemAdapter = new PlaylistItemArrayAdapter(getContext(), 0);
		m_listView.setAdapter(m_playlistItemAdapter);
		preparePlaylist();
		m_listView.setOnItemClickListener(m_playlistItemClick);
		
		m_playlistToolbar = (PlaylistToolbar) findViewById(R.id.botToolbar);
		m_playlistToolbar.setPlaylistView(this);
	}

	public void preparePlaylist() {
		m_playlistItemAdapter.clear();
		for (PlaylistItem item : MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems()) {
			m_playlistItemAdapter.add(item);
		}
	}

	private OnItemClickListener m_playlistItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int arg2, long arg3) {

		}
	};

}
