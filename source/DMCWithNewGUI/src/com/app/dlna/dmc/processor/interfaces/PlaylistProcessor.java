package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public interface PlaylistProcessor {
	void next();

	void previous();

	PlaylistItem getCurrentItem();

	void addItem(PlaylistItem item);

	void removeItem(PlaylistItem item);

	List<PlaylistItem> getAllItems();
}
