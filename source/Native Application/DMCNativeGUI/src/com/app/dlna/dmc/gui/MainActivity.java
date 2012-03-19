package com.app.dlna.dmc.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.app.dlna.dmc.nativeui.R;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.receiver.SDCardReceiver;
import com.app.dlna.dmc.processor.systemservice.RestartService;

public class MainActivity extends UpnpListenerTabActivity {
	private static final String TAG = MainActivity.class.getName();
	private TabHost m_tabHost;
	public static UpnpProcessor UPNP_PROCESSOR = null;
	// private ProgressDialog m_progressDialog = null;
	private static final int DEFAULT_TAB_INDEX = 0;
	private ProgressDialog m_routerProgressDialog;

	BroadcastReceiver m_mountedReceiver = new SDCardReceiver();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "MainActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		m_tabHost = getTabHost();
		m_tabHost.setup();

		UPNP_PROCESSOR = new UpnpProcessorImpl(MainActivity.this);
		UPNP_PROCESSOR.bindUpnpService();
		// m_progressDialog = ProgressDialog.show(MainActivity.this, "Starting Service", "");
		// m_progressDialog.setCancelable(true);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");

		registerReceiver(m_mountedReceiver, filter);
	}

	private OnTabChangeListener changeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			Log.d(TAG, "Select tab = " + tabId);
			if (tabId.equals("Library") && UPNP_PROCESSOR.getCurrentDMS() == null) {
				Toast.makeText(MainActivity.this, "Please select a MediaServer to browse", Toast.LENGTH_SHORT).show();
				m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);
				return;
			}
			if (tabId.equals("NowPlaying") && UPNP_PROCESSOR.getCurrentDMR() == null) {
				Toast.makeText(MainActivity.this, "Please select a MediaRenderer to control", Toast.LENGTH_SHORT).show();
				m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);
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
		super.onDestroy();
		Log.i(TAG, "MainActivity onDestroy");
		UPNP_PROCESSOR.unbindUpnpService();
		unregisterReceiver(m_mountedReceiver);
	};

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		createTabs();
	}

	private void createTabs() {
		TabSpec devicesTabSpec = m_tabHost.newTabSpec("Devices");

		Intent intent = null;
		devicesTabSpec.setIndicator("Devices", getResources().getDrawable(R.drawable.ic_tab_devices));
		intent = new Intent(this, DevicesActivity.class);
		devicesTabSpec.setContent(intent);

		TabSpec libraryTabSpec = m_tabHost.newTabSpec("Library");
		libraryTabSpec.setIndicator("Library", getResources().getDrawable(R.drawable.ic_tab_browse));
		intent = new Intent(this, LibraryActivity.class);
		libraryTabSpec.setContent(intent);

		TabSpec youtubeTabSpec = m_tabHost.newTabSpec("Youtube");
		youtubeTabSpec.setIndicator("Youtube", getResources().getDrawable(R.drawable.ic_tab_youtube));
		intent = new Intent(this, YoutubeActivity.class);
		youtubeTabSpec.setContent(intent);

		TabSpec playlistTabSpec = m_tabHost.newTabSpec("Playlist");
		playlistTabSpec.setIndicator("Playlist", getResources().getDrawable(R.drawable.ic_tab_play_list));
		intent = new Intent(this, PlaylistActivity.class);
		playlistTabSpec.setContent(intent);

		m_tabHost.addTab(devicesTabSpec);
		m_tabHost.addTab(libraryTabSpec);
		m_tabHost.addTab(youtubeTabSpec);
		m_tabHost.addTab(playlistTabSpec);

		m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);

		m_tabHost.setOnTabChangedListener(changeListener);

		m_tabHost.refreshDrawableState();

	}

	@Override
	public void onStartFailed() {
		super.onStartFailed();
		new AlertDialog.Builder(MainActivity.this).setTitle("Start Fail").setMessage("Cannot found a connection for UpnpService. Start service fail")
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.finish();
					}
				}).setCancelable(false).create().show();
	}

	@Override
	public void finishFromChild(Activity child) {
		Log.e(TAG, "Finish from child " + m_tabHost.getCurrentTabTag());
		if (m_tabHost.getCurrentTabTag().equals("Devices")) {
			finish();
		} else {
			m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);
		}
	}

	@Override
	public void onNetworkChanged() {
		super.onNetworkChanged();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Network interface changed", Toast.LENGTH_SHORT).show();
				restartActivity();
			}
		});
	}

	@Override
	public void onRouterError(final String cause) {
		super.onRouterError(cause);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_routerProgressDialog != null)
					m_routerProgressDialog.dismiss();
				new AlertDialog.Builder(MainActivity.this).setTitle("Network error").setMessage(cause).setCancelable(false)
						.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								MainActivity.this.finish();
							}
						}).create().show();
			}
		});

	}

	@Override
	public void onRouterDisabledEvent() {
		super.onRouterDisabledEvent();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_routerProgressDialog != null)
					m_routerProgressDialog.dismiss();
				m_routerProgressDialog = ProgressDialog.show(MainActivity.this, "Router disabled", "Router disabled, try to enabled router");
			}
		});

	}

	@Override
	public void onRouterEnabledEvent() {
		super.onRouterEnabledEvent();
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_routerProgressDialog != null)
					m_routerProgressDialog.dismiss();
			}
		});
	}

	private void restartActivity() {
		new AlertDialog.Builder(this).setTitle("Restart Required").setMessage("Network interface changed. Application must restart.")
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.startService(new Intent(MainActivity.this, RestartService.class));
						MainActivity.this.finish();
					}
				}).setCancelable(false).create().show();

	}
}