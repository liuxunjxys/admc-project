package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.VideoItem;

import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class PlaylistProcessorImpl implements PlaylistProcessor {
	private List<PlaylistItem> m_playlistItems;
	private int m_currentItemIdx;
	private int m_maxSize;
	private List<String> m_listURI;
	private String m_playlistName;

	public PlaylistProcessorImpl(String name, int maxSize) {
		m_playlistItems = new ArrayList<PlaylistItem>();
		m_currentItemIdx = -1;
		m_maxSize = maxSize;
		m_listURI = new ArrayList<String>();
		m_playlistName = name;
	}

	@Override
	public String getPlaylistName() {
		return m_playlistName;
	}

	@Override
	public int getMaxSize() {
		return m_maxSize;
	}

	@Override
	public boolean isFull() {
		return m_playlistItems.size() >= m_maxSize ? true : false;
	}

	@Override
	public void next() {
		if (m_playlistItems.size() == 0)
			return;
		m_currentItemIdx = (m_currentItemIdx + 1) % m_playlistItems.size();
		if (m_currentItemIdx >= m_playlistItems.size()) {
			m_currentItemIdx = 0;
		}
	}

	@Override
	public void previous() {
		if (m_playlistItems.size() == 0)
			return;
		m_currentItemIdx = (m_currentItemIdx - 1) % m_playlistItems.size();
		if (m_currentItemIdx < 0) {
			m_currentItemIdx = m_playlistItems.size() - 1;
		}
	}

	@Override
	public PlaylistItem getCurrentItem() {
		if (m_playlistItems.size() > 0 && m_currentItemIdx < m_playlistItems.size()) {
			return m_playlistItems.get(m_currentItemIdx);
		}
		return null;
	}

	@Override
	public int setCurrentItem(int idx) {
		if (0 <= idx && idx < m_playlistItems.size()) {
			m_currentItemIdx = idx;
			return m_currentItemIdx;
		}
		return -1;
	}

	@Override
	public int setCurrentItem(PlaylistItem item) {
		synchronized (m_playlistItems) {
			if (m_playlistItems.contains(item)) {
				m_currentItemIdx = m_playlistItems.indexOf(item);
				return m_currentItemIdx;
			}
			return -1;
		}
	}

	@Override
	public PlaylistItem addItem(PlaylistItem item) {
		synchronized (m_playlistItems) {
			if (m_playlistItems.contains(item) || m_playlistItems.size() >= m_maxSize)
				return null;
			m_playlistItems.add(item);
			m_listURI.add(item.getUri());
			if (m_playlistItems.size() == 1) {
				m_currentItemIdx = 0;
			}
			return item;
		}
	}

	@Override
	public PlaylistItem removeItem(PlaylistItem item) {
		synchronized (m_playlistItems) {
			if (m_playlistItems.contains(item)) {
				m_playlistItems.remove(item);
				m_listURI.remove(item.getUri());
				return item;
			}
			return null;
		}
	}

	@Override
	public List<PlaylistItem> getAllItems() {
		return m_playlistItems;
	}

	@Override
	public boolean containsUrl(String url) {
		return m_listURI.contains(url);
	}

	@Override
	public PlaylistItem addDIDLObject(DIDLObject object) {
		return addItem(createPlaylistItem(object));
	}

	@Override
	public PlaylistItem removeDIDLObject(DIDLObject object) {
		return removeItem(createPlaylistItem(object));
	}

	private PlaylistItem createPlaylistItem(DIDLObject object) {
		PlaylistItem item = new PlaylistItem();
		item.setTitle(object.getTitle());
		item.setUrl(object.getResources().get(0).getValue());
		if (object instanceof AudioItem) {
			item.setType(Type.AUDIO);
		} else if (object instanceof VideoItem) {
			item.setType(Type.VIDEO);
		} else {
			item.setType(Type.IMAGE);
		}
		return item;
	}
}
