package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public interface PlaylistProcessor {
	void next();

	void previous();

	PlaylistItem getCurrentItem();

	boolean addItem(PlaylistItem item);

	boolean removeItem(PlaylistItem item);

	boolean containsUrl(String url);
	
	int getMaxSize();

	boolean isFull();

	List<PlaylistItem> getAllItems();

	int setCurrentItem(int idx);

	int setCurrentItem(PlaylistItem item);
}
