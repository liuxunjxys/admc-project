package com.app.dlna.dmc.processor.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PlaylistSQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_PLAYLIST_ITEMS = "PlaylistItems";
	public static final String COL_ID = "_id";
	public static final String COL_TITLE = "title";
	public static final String COL_URL = "url";
	public static final String COL_TYPE = "type";
	public static final String COL_PLAYLIST_ID = "playlist_id";

	public static final String TABLE_PLAYLISTS = "playlists";
	public static final String COL_NAME = "name";

	private static final String DATABASE_NAME = "playlists.db";
	private static final int DATABASE_VERSION = 7;

	private static final String UNSAVED = "Unsaved Playlist";

	private static final String DATABASE_CREATE_PLAYLIST = "create table " + TABLE_PLAYLISTS + "( " + COL_ID
			+ " integer primary key autoincrement, " + COL_NAME + " text not null);";

	private static final String DATABASE_CREATE_PLAYLIST_ITEM = "create table " + TABLE_PLAYLIST_ITEMS + "( " + COL_ID
			+ " integer primary key autoincrement, " + COL_TITLE + " text not null, " + COL_URL + " text not null, "
			+ COL_TYPE + " text not null, " + COL_PLAYLIST_ID + " text not null);";

	private static final String DATABASE_CREATE_UNSAVEDLIST = "insert into " + TABLE_PLAYLISTS + " (" + COL_NAME
			+ ") values ('" + UNSAVED + "');";

	public static String[] PLAYLIST_ALLCOLUMNS = { PlaylistSQLiteHelper.COL_ID, PlaylistSQLiteHelper.COL_NAME };
	public static String[] PLALYLISTITEM_ALLCOLUMNS = { PlaylistSQLiteHelper.COL_ID, PlaylistSQLiteHelper.COL_TITLE,
			PlaylistSQLiteHelper.COL_URL, PlaylistSQLiteHelper.COL_TYPE, PlaylistSQLiteHelper.COL_PLAYLIST_ID };

	private static final String TAG = PlaylistSQLiteHelper.class.getName();

	public PlaylistSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Create database");
		db.execSQL(DATABASE_CREATE_PLAYLIST);
		db.execSQL(DATABASE_CREATE_UNSAVEDLIST);
		db.execSQL(DATABASE_CREATE_PLAYLIST_ITEM);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrade database");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_ITEMS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
		onCreate(db);
	}

}
