package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.List;

import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class PlaylistProcessorImpl implements PlaylistProcessor {
	private List<PlaylistItem> m_playlistItems;
	private int m_currentItemIdx;

	public PlaylistProcessorImpl() {
		m_playlistItems = new ArrayList<PlaylistItem>();
		m_currentItemIdx = -1;
	}

	@Override
	public void next() {
		m_currentItemIdx = (m_currentItemIdx + 1) % m_playlistItems.size();
	}

	@Override
	public void previous() {
		m_currentItemIdx = (m_currentItemIdx - 1) % m_playlistItems.size();
	}

	@Override
	public PlaylistItem getCurrentItem() {
		if (m_playlistItems.size() > 0 && m_currentItemIdx < m_playlistItems.size()) {
			return m_playlistItems.get(m_currentItemIdx);
		}
		return null;
	}

	@Override
	public void addItem(PlaylistItem item) {
		synchronized (m_playlistItems) {
			m_playlistItems.add(item);
			if (m_playlistItems.size() == 1) {
				m_currentItemIdx = 0;
			}
		}
	}

	@Override
	public void removeItem(PlaylistItem item) {
		synchronized (m_playlistItems) {
			if (m_playlistItems.contains(item)) {
				m_playlistItems.remove(item);
			}
		}
	}

	@Override
	public List<PlaylistItem> getAllItems() {
		return m_playlistItems;
	}

}
