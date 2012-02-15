package com.app.dlna.dmc.gui.playlist;

import android.os.Bundle;
import android.widget.ListView;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class PlaylistActivity extends UpnpListenerActivity {

	private UpnpProcessor m_upnpProcessor;
	private ListView m_listView;
	private PlaylistItemArrayAdapter m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
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
		super.onResume();
		if (m_upnpProcessor != null && m_upnpProcessor.getPlaylistProcessor() != null)
			for (PlaylistItem item : m_upnpProcessor.getPlaylistProcessor().getAllItems()) {
				m_adapter.add(item);
			}
	}

	@Override
	protected void onPause() {
		m_adapter.clear();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		m_upnpProcessor.unbindUpnpService();
		super.onDestroy();
	}
}
