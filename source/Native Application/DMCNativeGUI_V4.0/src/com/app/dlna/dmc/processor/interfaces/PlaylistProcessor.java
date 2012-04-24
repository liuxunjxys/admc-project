package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

import org.teleal.cling.support.model.DIDLObject;

import com.app.dlna.dmc.processor.playlist.Playlist;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public interface PlaylistProcessor {
	void next();

	void previous();

	PlaylistItem getCurrentItem();

	PlaylistItem addItem(PlaylistItem item);

	PlaylistItem addDIDLObject(DIDLObject object);

	PlaylistItem removeItem(PlaylistItem item);

	PlaylistItem removeDIDLObject(DIDLObject object);

	PlaylistItem getItemAt(int idx);

	boolean containsUrl(String url);

	int getMaxSize();

	boolean isFull();

	List<PlaylistItem> getAllItems();

	int setCurrentItem(int idx);

	int setCurrentItem(PlaylistItem item);
	
	Playlist getData();
	
	void setData(Playlist data);
	
	void insertNew(String name);
	
	void updateCurrent();
	
	void deleteCurrent();
	
}
