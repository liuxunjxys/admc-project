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

public class PlaylistAllItemView extends LinearLayout {

	private ListView m_listView;
	private PlaylistItemArrayAdapter m_adapter;

	public PlaylistAllItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_playlist_allitem, this);
		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new PlaylistItemArrayAdapter(getContext(), 0);
		m_listView.setAdapter(m_adapter);
		preparePlaylist();
		m_listView.setOnItemClickListener(m_playlistItemClick);
	}

	public void preparePlaylist() {
		m_adapter.clear();
		for (PlaylistItem item : MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems()) {
			m_adapter.add(item);
		}
	}

	private OnItemClickListener m_playlistItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int arg2, long arg3) {

		}
	};

}
