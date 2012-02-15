package com.app.dlna.dmc.gui;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.app.dlna.dmc.gui.devices.DevicesActivity;
import com.app.dlna.dmc.gui.library.LibraryActivity;
import com.app.dlna.dmc.gui.nowplaying.NowPlayingActivity;
import com.app.dlna.dmc.gui.playlist.PlaylistActivity;
import com.app.dlna.dmc.gui.youtube.YoutubeActivity;

public class MainActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		TabHost tabHost = getTabHost();
		tabHost.setup();

		TabSpec nowplayingTabSpec = tabHost.newTabSpec("NowPlaying");
		nowplayingTabSpec.setIndicator("Now Playing", getResources().getDrawable(R.drawable.ic_tab_now_playing));
		Intent nowplayingIntent = new Intent(this, NowPlayingActivity.class);
		nowplayingTabSpec.setContent(nowplayingIntent);

		TabSpec playlistTabSpec = tabHost.newTabSpec("Playlist");
		playlistTabSpec.setIndicator("Playlist", getResources().getDrawable(R.drawable.ic_tab_play_list));
		Intent playlistIntent = new Intent(this, PlaylistActivity.class);
		playlistTabSpec.setContent(playlistIntent);

		TabSpec libraryTabSpec = tabHost.newTabSpec("Library");
		libraryTabSpec.setIndicator("Library", getResources().getDrawable(R.drawable.ic_tab_browse));
		Intent libraryIntent = new Intent(this, LibraryActivity.class);
		libraryTabSpec.setContent(libraryIntent);

		TabSpec devicesTabSpec = tabHost.newTabSpec("Devices");
		devicesTabSpec.setIndicator("Devices", getResources().getDrawable(R.drawable.ic_tab_devices));
		Intent devicesIntent = new Intent(this, DevicesActivity.class);
		devicesTabSpec.setContent(devicesIntent);

		TabSpec youtubeTabSpec = tabHost.newTabSpec("Youtube");
		youtubeTabSpec.setIndicator("Youtube", getResources().getDrawable(R.drawable.ic_tab_youtube));
		Intent youtubeIntent = new Intent(this, YoutubeActivity.class);
		youtubeTabSpec.setContent(youtubeIntent);

		tabHost.addTab(nowplayingTabSpec);
		tabHost.addTab(playlistTabSpec);
		tabHost.addTab(libraryTabSpec);
		tabHost.addTab(devicesTabSpec);
		tabHost.addTab(youtubeTabSpec);

		tabHost.setCurrentTab(3);
	}
}