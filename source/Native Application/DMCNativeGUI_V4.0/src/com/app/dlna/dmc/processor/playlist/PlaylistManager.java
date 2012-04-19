package com.app.dlna.dmc.processor.playlist;

import java.util.ArrayList;
import java.util.List;

import com.app.dlna.dmc.processor.impl.PlaylistProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class PlaylistManager {
	private static final int MAX_ITEM = 1000;

	public static PlaylistProcessor UNSAVED_LIST = new PlaylistProcessorImpl("Unsaved Playlist", MAX_ITEM);
	private static List<PlaylistProcessor> PLAYLISTES = new ArrayList<PlaylistProcessor>();
	static {

		// create dummy 1
		PlaylistProcessor dummy1 = new PlaylistProcessorImpl("Dummy 1", MAX_ITEM);
		for (int i = 0; i < 10; ++i) {
			PlaylistItem item = new PlaylistItem();
			item.setTitle("Dummy item 1 - " + i);
			item.setType(Type.AUDIO);
			item.setUrl("www.google.com.vn");
			dummy1.addItem(item);
		}

		// craete dummy 2
		PlaylistProcessor dummy2 = new PlaylistProcessorImpl("Dummy 2", MAX_ITEM);
		for (int i = 0; i < 10; ++i) {
			PlaylistItem item = new PlaylistItem();
			item.setTitle("Dummy item 2 - " + i);
			item.setType(Type.VIDEO);
			item.setUrl("www.google.com.vn");
			dummy2.addItem(item);
		}

		// craete dummy 3
		PlaylistProcessor dummy3 = new PlaylistProcessorImpl("Dummy 3", MAX_ITEM);
		for (int i = 0; i < 10; ++i) {
			PlaylistItem item = new PlaylistItem();
			item.setTitle("Dummy item 3 - " + i);
			item.setType(Type.IMAGE);
			item.setUrl("www.google.com.vn");
			dummy3.addItem(item);
		}

		PLAYLISTES.add(dummy1);
		PLAYLISTES.add(dummy2);
		PLAYLISTES.add(dummy3);

		
	}

	public static List<PlaylistProcessor> getAllPlaylist() {
		return PLAYLISTES;
	}
}
