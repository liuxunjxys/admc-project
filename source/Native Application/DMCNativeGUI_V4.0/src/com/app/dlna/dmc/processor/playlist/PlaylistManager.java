package com.app.dlna.dmc.processor.playlist;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.impl.PlaylistProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;
import com.app.dlna.dmc.processor.provider.PlaylistProvider;
import com.app.dlna.dmc.processor.provider.PlaylistSQLiteHelper;

public class PlaylistManager {
	private static final int MAX_ITEM = 64;

	public static List<Playlist> getAllPlaylist() {
		try {
			List<Playlist> result = new ArrayList<Playlist>();
			Cursor cursor = MainActivity.INSTANCE.getContentResolver().query(PlaylistProvider.PLAYLIST_URI,
					PlaylistSQLiteHelper.PLAYLIST_ALLCOLUMNS, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					long id = cursor.getLong(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_ID));
					String name = cursor.getString(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_NAME));
					Playlist playlist = new Playlist();
					playlist.setId(id);
					playlist.setName(name);
					result.add(playlist);

				} while (cursor.moveToNext());
			}
			cursor.close();
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<Playlist>();
		}
	}

	public static List<PlaylistItem> getAllPlaylistItem(long id) {
		try {
			List<PlaylistItem> result = new ArrayList<PlaylistItem>();
			Cursor itemsCursor = MainActivity.INSTANCE.getContentResolver().query(PlaylistProvider.PLAYLIST_ITEM_URI,
					PlaylistSQLiteHelper.PLALYLISTITEM_ALLCOLUMNS, PlaylistSQLiteHelper.COL_PLAYLIST_ID + " = ? ",
					new String[] { String.valueOf(id) }, null);
			if (itemsCursor != null && itemsCursor.moveToFirst()) {
				do {
					result.add(cursorToPlaylistItem(itemsCursor));
				} while (itemsCursor.moveToNext());
			}
			itemsCursor.close();
			return result;
		} catch (Exception ex) {
			return null;
		}
	}

	public static PlaylistItem cursorToPlaylistItem(Cursor cursor) {
		try {
			long id = cursor.getLong(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_ID));
			String title = cursor.getString(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_TITLE));
			String url = cursor.getString(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_URL));
			String type = cursor.getString(cursor.getColumnIndex(PlaylistSQLiteHelper.COL_TYPE));
			PlaylistItem res = new PlaylistItem();
			res.setId(id);
			res.setTitle(title);
			res.setType(Type.valueOf(type));
			res.setUrl(url);
			return res;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static boolean createPlaylistItem(PlaylistItem playlistItem, long playlist_id) {
		try {
			ContentValues values = new ContentValues();
			values.put(PlaylistSQLiteHelper.COL_TITLE, playlistItem.getTitle());
			values.put(PlaylistSQLiteHelper.COL_URL, playlistItem.getUrl());
			values.put(PlaylistSQLiteHelper.COL_TYPE, playlistItem.getType().toString());
			values.put(PlaylistSQLiteHelper.COL_PLAYLIST_ID, playlist_id);
			return null != MainActivity.INSTANCE.getContentResolver()
					.insert(PlaylistProvider.PLAYLIST_ITEM_URI, values);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean createPlaylist(Playlist playlist) {
		try {
			ContentResolver resolver = MainActivity.INSTANCE.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(PlaylistSQLiteHelper.COL_NAME, playlist.getName());
			Uri uri = resolver.insert(PlaylistProvider.PLAYLIST_URI, values);
			if (uri != null) {
				String newId = uri.getQueryParameter("newid");
				ContentValues updateValues = new ContentValues();
				updateValues.put(PlaylistSQLiteHelper.COL_PLAYLIST_ID, newId);
				resolver.update(PlaylistProvider.PLAYLIST_ITEM_URI, updateValues, PlaylistSQLiteHelper.COL_PLAYLIST_ID
						+ " = ?", new String[] { "1" });
				return true;
			}
			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean deletePlaylist(PlaylistProcessor playlistProcessor) {
		long playlistId = playlistProcessor.getData().getId();
		MainActivity.INSTANCE.getContentResolver().delete(PlaylistProvider.PLAYLIST_ITEM_URI,
				PlaylistSQLiteHelper.COL_PLAYLIST_ID + " = ?", new String[] { String.valueOf(playlistId) });
		int playlistCount = MainActivity.INSTANCE.getContentResolver().delete(PlaylistProvider.PLAYLIST_URI,
				PlaylistSQLiteHelper.COL_ID + " = ?", new String[] { String.valueOf(playlistId) });
		return playlistCount == 1;
	}

	public static PlaylistProcessor getPlaylistProcessor(Playlist playlist) {
		PlaylistProcessor processor = new PlaylistProcessorImpl(playlist, MAX_ITEM);

		for (PlaylistItem item : getAllPlaylistItem(playlist.getId())) {
			processor.getAllItems().add(item);
		}

		return processor;
	}

	public static boolean deletePlaylistItem(long id) {
		return 1 == MainActivity.INSTANCE.getContentResolver().delete(PlaylistProvider.PLAYLIST_ITEM_URI,
				PlaylistSQLiteHelper.COL_ID + " = ?", new String[] { String.valueOf(id) });
	}

	public static void clearPlaylist(long id) {
		MainActivity.INSTANCE.getContentResolver().delete(PlaylistProvider.PLAYLIST_ITEM_URI,
				PlaylistSQLiteHelper.COL_PLAYLIST_ID + " = ?", new String[] { String.valueOf(id) });
	}

}
