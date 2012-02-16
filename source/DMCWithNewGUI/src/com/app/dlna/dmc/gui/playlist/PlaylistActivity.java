package com.app.dlna.dmc.gui.playlist;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class PlaylistActivity extends UpnpListenerActivity {

	private static final String TAG = PlaylistActivity.class.getName();
	private UpnpProcessor m_upnpProcessor;
	private ListView m_listView;
	private PlaylistItemArrayAdapter m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "Playlist onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playlist_activity);
		m_upnpProcessor = new UpnpProcessorImpl(PlaylistActivity.this);
		m_upnpProcessor.bindUpnpService();
		m_listView = (ListView) findViewById(R.id.playList);
		m_adapter = new PlaylistItemArrayAdapter(PlaylistActivity.this, 0);
		m_listView.setAdapter(m_adapter);
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "Playlist onResume");
		super.onResume();
		if (m_upnpProcessor != null && m_upnpProcessor.getPlaylistProcessor() != null)
			refreshPlaylist();
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "Playlist onPause");
		m_adapter.clear();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Playlist onDestroy");
		m_upnpProcessor.unbindUpnpService();
		super.onDestroy();
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		refreshPlaylist();
		m_listView.setOnItemClickListener(onPlaylistItemClick);
	}

	private OnItemClickListener onPlaylistItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adaper, View view, int position, long arg3) {
			PlaylistItem item = m_adapter.getItem(position);
			String url = item.getUrl();
			DMRProcessor dmrProcessor = m_upnpProcessor.getDMRProcessor();
			if (dmrProcessor == null) {
				Toast.makeText(PlaylistActivity.this, "Cannot get DMRProcessor", Toast.LENGTH_SHORT).show();
			} else {
				dmrProcessor.setURI(url);
			}
		}
	};

	private void refreshPlaylist() {
		synchronized (m_adapter) {
			m_adapter.clear();
			for (PlaylistItem item : m_upnpProcessor.getPlaylistProcessor().getAllItems()) {
				m_adapter.add(item);
			}
		}
	}
}
