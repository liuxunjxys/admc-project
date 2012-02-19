package com.app.dlna.dmc.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerTabActivity;
import com.app.dlna.dmc.gui.devices.DevicesActivity;
import com.app.dlna.dmc.gui.library.LibraryActivity;
import com.app.dlna.dmc.gui.playlist.PlaylistActivity;
import com.app.dlna.dmc.gui.youtube.YoutubeActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class MainActivity extends UpnpListenerTabActivity {
	private static final String TAG = MainActivity.class.getName();
	private TabHost m_tabHost;
	private UpnpProcessor m_processor = null;
	private ProgressDialog m_progressDialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "MainActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		m_processor = new UpnpProcessorImpl(MainActivity.this);
		m_processor.bindUpnpService();
		m_progressDialog = ProgressDialog.show(MainActivity.this, "Starting Service", "");
		m_progressDialog.setCancelable(true);
		m_tabHost = getTabHost();
		m_tabHost.setup();
		Intent intent = null;

		TabSpec playlistTabSpec = m_tabHost.newTabSpec("Playlist");
		playlistTabSpec.setIndicator("Playlist", getResources().getDrawable(R.drawable.ic_tab_play_list));
		intent = new Intent(this, PlaylistActivity.class);
		playlistTabSpec.setContent(intent);

		TabSpec libraryTabSpec = m_tabHost.newTabSpec("Library");
		libraryTabSpec.setIndicator("Library", getResources().getDrawable(R.drawable.ic_tab_browse));
		intent = new Intent(this, LibraryActivity.class);
		libraryTabSpec.setContent(intent);

		TabSpec devicesTabSpec = m_tabHost.newTabSpec("Devices");
		devicesTabSpec.setIndicator("Devices", getResources().getDrawable(R.drawable.ic_tab_devices));
		intent = new Intent(this, DevicesActivity.class);
		devicesTabSpec.setContent(intent);

		TabSpec youtubeTabSpec = m_tabHost.newTabSpec("Youtube");
		youtubeTabSpec.setIndicator("Youtube", getResources().getDrawable(R.drawable.ic_tab_youtube));
		intent = new Intent(this, YoutubeActivity.class);
		youtubeTabSpec.setContent(intent);

		m_tabHost.addTab(playlistTabSpec);
		m_tabHost.addTab(libraryTabSpec);
		m_tabHost.addTab(devicesTabSpec);
		m_tabHost.addTab(youtubeTabSpec);

		m_tabHost.setCurrentTab(2);

	}

	private OnTabChangeListener changeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			Log.d(TAG, "Select tab = " + tabId);
			if (tabId.equals("Library") && m_processor.getCurrentDMS() == null) {
				Toast.makeText(MainActivity.this, "Please select a MediaServer to browse", Toast.LENGTH_SHORT).show();
				m_tabHost.setCurrentTab(3);
				return;
			}
			if (tabId.equals("NowPlaying") && m_processor.getCurrentDMR() == null) {
				Toast.makeText(MainActivity.this, "Please select a MediaRenderer to control", Toast.LENGTH_SHORT).show();
				m_tabHost.setCurrentTab(3);
				return;
			}

		}
	};

	protected void onResume() {
		super.onResume();
		Log.i(TAG, "MainActivity onResume");
	};

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "MainActivity onPause");
	}

	protected void onDestroy() {
		Log.i(TAG, "MainActivity onDestroy");
		m_processor.unbindUpnpService();
		super.onDestroy();
	};

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		m_tabHost.setOnTabChangedListener(changeListener);
		m_progressDialog.setTitle("Scanning device");
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(7000);
					m_progressDialog.dismiss();
				} catch (Exception ex) {
					m_progressDialog.dismiss();
				}

			}
		}).start();
	}

	@Override
	public void finishFromChild(Activity child) {
		Log.e(TAG, "Finish from child " + m_tabHost.getCurrentTabTag());
		if (m_tabHost.getCurrentTabTag().equals("Devices")) {
			finish();
		} else {
			m_tabHost.setCurrentTab(3);
		}
	}
}