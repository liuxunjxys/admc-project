package com.app.dlna.dmc.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import app.dlna.controller.R;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerTabActivity;
import com.app.dlna.dmc.gui.subactivity.MediaSourceActivity;
import com.app.dlna.dmc.gui.subactivity.NowPlayingActivity;
import com.app.dlna.dmc.gui.subactivity.PlaylistActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.localdevice.service.LocalContentDirectoryService;
import com.app.dlna.dmc.processor.receiver.SDCardReceiver;
import com.app.dlna.dmc.processor.systemservice.RestartService;

public class MainActivity extends UpnpListenerTabActivity {
	private static final String TAG = MainActivity.class.getName();
	private TabHost m_tabHost;
	public static UpnpProcessor UPNP_PROCESSOR = null;
	private static final int DEFAULT_TAB_INDEX = 0;
	private ProgressDialog m_routerProgressDialog;
	public static MainActivity INSTANCE;

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
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");

		registerReceiver(m_mountedReceiver, filter);
		INSTANCE = this;
	}

	private OnTabChangeListener changeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {

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
		Intent intent = null;

		// TabSpec mediaSource = m_tabHost.newTabSpec("Devices");
		// mediaSource.setIndicator("",
		// getResources().getDrawable(R.drawable.ic_tab_mediasource));
		// intent = new Intent(this, MediaSourceActivity.class);
		// mediaSource.setContent(intent);
		//
		// TabSpec playlist = m_tabHost.newTabSpec("Library");
		// playlist.setIndicator("",
		// getResources().getDrawable(R.drawable.ic_tab_playlist));
		// intent = new Intent(this, PlaylistActivity.class);
		// playlist.setContent(intent);
		//
		// TabSpec nowPlayling = m_tabHost.newTabSpec("Youtube");
		// nowPlayling.setIndicator("",
		// getResources().getDrawable(R.drawable.ic_tab_nowplaying));
		// intent = new Intent(this, NowPlayingActivity.class);
		// nowPlayling.setContent(intent);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TabSpec mediaSource = m_tabHost.newTabSpec(getString(R.string.media_source));
		TextView tvMediaSource = (TextView) inflater.inflate(R.layout.cv_tabwidget, null);
		tvMediaSource.setText(R.string.media_source);
		mediaSource.setIndicator(tvMediaSource);
		intent = new Intent(this, MediaSourceActivity.class);
		mediaSource.setContent(intent);

		TabSpec playlist = m_tabHost.newTabSpec(getString(R.string.playlist));
		TextView tvPlaylist = (TextView) inflater.inflate(R.layout.cv_tabwidget, null);
		tvPlaylist.setText(R.string.playlist);
		playlist.setIndicator(tvPlaylist);
		intent = new Intent(this, PlaylistActivity.class);
		playlist.setContent(intent);

		TabSpec nowPlayling = m_tabHost.newTabSpec(getString(R.string.now_playing));
		TextView tvNowPlaying = (TextView) inflater.inflate(R.layout.cv_tabwidget, null);
		tvNowPlaying.setText(R.string.now_playing);
		nowPlayling.setIndicator(tvNowPlaying);
		intent = new Intent(this, NowPlayingActivity.class);
		nowPlayling.setContent(intent);

		m_tabHost.addTab(mediaSource);
		m_tabHost.addTab(playlist);
		m_tabHost.addTab(nowPlayling);

		m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);

		m_tabHost.setOnTabChangedListener(changeListener);

		m_tabHost.refreshDrawableState();

	}

	@Override
	public void onStartFailed() {
		super.onStartFailed();
		new AlertDialog.Builder(MainActivity.this).setTitle("Start Fail")
				.setMessage("Cannot found a connection for UpnpService. Start service fail")
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
		if (m_tabHost.getCurrentTabTag().equals(getString(R.string.media_source))) {
			confirmExit();
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
				new AlertDialog.Builder(MainActivity.this).setTitle("Network changed")
						.setMessage("Network interface changed. Application must restart.").setCancelable(false)
						.setPositiveButton("OK", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								restartActivity();
							}
						}).create().show();

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
				new AlertDialog.Builder(MainActivity.this).setTitle("Network error").setMessage(cause)
						.setCancelable(false).setPositiveButton("OK", new OnClickListener() {

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
				m_routerProgressDialog = ProgressDialog.show(MainActivity.this, "Router disabled",
						"Router disabled, try to enabled router");
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
		new AlertDialog.Builder(this).setTitle("Restart Required")
				.setMessage("Network interface changed. Application must restart.")
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						MainActivity.this.startService(new Intent(MainActivity.this, RestartService.class));
						MainActivity.this.finish();
					}
				}).setCancelable(false).create().show();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.title:
			Toast.makeText(MainActivity.this, "Show about dialog", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_refresh:
			Toast.makeText(MainActivity.this, "Refresh", Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_settings:
			String[] items = new String[1];
			items[0] = "Rescan external storage";
			new AlertDialog.Builder(MainActivity.this).setTitle("Settings").setItems(items, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						if (!LocalContentDirectoryService.isScanning())
							LocalContentDirectoryService.scanMedia(MainActivity.this);
						break;
					default:
						break;
					}
				}
			}).create().show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		confirmExit();

	}

	private void confirmExit() {
		new AlertDialog.Builder(MainActivity.this).setTitle("Confirm exit").setMessage("Are you sure want to exit?")
				.setPositiveButton("OK", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).setNegativeButton("Cancel", null).create().show();
	}
}
