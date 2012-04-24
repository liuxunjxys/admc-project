package com.app.dlna.dmc.gui.customview.playlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.gui.customview.adapter.CustomArrayAdapter;
import com.app.dlna.dmc.gui.customview.listener.DMRListenerView;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.Playlist;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistManager;

public class PlaylistView extends DMRListenerView {

	private PlaylistToolbar m_playlistToolbar;
	private static final int VM_LIST = 0;
	private static final int VM_DETAILS = 1;
	private int m_viewMode = -1;

	public PlaylistView(Context context) {
		super(context);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_playlist_allitem, this);
		m_listView = (ListView) findViewById(R.id.lv_playlist);
		m_adapter = new CustomArrayAdapter(getContext(), 0);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(m_playlistItemClick);

		m_playlistToolbar = (PlaylistToolbar) findViewById(R.id.botToolbar);
		m_playlistToolbar.setPlaylistView(this);
		m_viewMode = VM_LIST;
		m_playlistToolbar.enableBack();
		preparePlaylist();
		updateDMRListener();
	}

	public void preparePlaylist() {
		m_adapter.clear();
		switch (m_viewMode) {
		case VM_DETAILS:
			for (PlaylistItem item : MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems()) {
				m_adapter.add(new AdapterItem(item));
			}
			m_playlistToolbar.setVisibility(View.VISIBLE);
			m_playlistToolbar.enableBack();
			break;
		case VM_LIST:
			for (Playlist playlist : PlaylistManager.getAllPlaylist()) {
				m_adapter.add(new AdapterItem(playlist));
			}
			m_playlistToolbar.disableBack();
			m_playlistToolbar.setVisibility(View.GONE);
		default:
			break;
		}

	}

	private OnItemClickListener m_playlistItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
			Object object = m_adapter.getItem(position).getData();
			if (object instanceof Playlist) {
				PlaylistProcessor playlistProcessor = PlaylistManager.getPlaylistProcessor((Playlist) object);
				MainActivity.UPNP_PROCESSOR.setPlaylistProcessor(playlistProcessor);
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setPlaylistProcessor(playlistProcessor);
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setSeftAutoNext(true);
				m_viewMode = VM_DETAILS;
				preparePlaylist();
			} else if (object instanceof PlaylistItem) {
				PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setPlaylistProcessor(playlistProcessor);
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setSeftAutoNext(true);
				if (playlistProcessor == null) {
					Toast.makeText(getContext(), "Cannot get playlist", Toast.LENGTH_SHORT).show();
					return;
				}

				DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
				if (dmrProcessor == null) {
					Toast.makeText(getContext(), "Cannot connect to renderer", Toast.LENGTH_SHORT).show();
					return;
				}

				playlistProcessor.setCurrentItem((PlaylistItem) object);
				dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem().getUri());
				// TODO: high light current item
			}

		}
	};

	public void backToListPlaylist() {
		m_viewMode = VM_LIST;
		preparePlaylist();
	}
}
