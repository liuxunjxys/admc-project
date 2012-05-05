package com.app.dlna.dmc.gui;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.types.UDN;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerTabActivity;
import com.app.dlna.dmc.gui.customview.renderer.RendererCompactView;
import com.app.dlna.dmc.gui.customview.renderer.RendererCompactView.OnDMRChangeListener;
import com.app.dlna.dmc.gui.subactivity.LibraryActivity;
import com.app.dlna.dmc.gui.subactivity.NowPlayingActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.localdevice.service.LocalContentDirectoryService;
import com.app.dlna.dmc.processor.nfc.NFCUtils;
import com.app.dlna.dmc.processor.receiver.SDCardReceiver;
import com.app.dlna.dmc.processor.systemservice.RestartService;
import com.app.dlna.dmc.processor.upnp.CoreUpnpService;

public class MainActivity extends UpnpListenerTabActivity {
	private static final String TAG = MainActivity.class.getName();
	private TabHost m_tabHost;
	public static UpnpProcessor UPNP_PROCESSOR = null;
	private static final int DEFAULT_TAB_INDEX = 0;
	private ProgressDialog m_routerProgressDialog;
	private ProgressDialog m_nfcProgressDialog;
	public static MainActivity INSTANCE;
	private LinearLayout m_ll_menu;
	private BroadcastReceiver m_mountedReceiver = new SDCardReceiver();
	private NfcAdapter m_nfcAdapter;
	private PendingIntent m_pendingIntent;
	private IntentFilter[] m_filters;
	private String[][] m_techLists;
	private boolean m_waitToWriteTAG = false;
	private RendererCompactView m_rendererCompactView;
	private ImageView btn_toggleRendererView;
	
	private static final int SIZE = 2;
	public ThreadPoolExecutor EXEC = new ThreadPoolExecutor(SIZE, SIZE, 8, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
			new RejectedExecutionHandler() {

				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
					
				}
			});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "MainActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

		m_ll_menu = (LinearLayout) findViewById(R.id.ll_floatMenu);

		m_nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		m_pendingIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		// for NfcTAG that have data
		IntentFilter ndefDiscovered = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefDiscovered.addDataType("text/plain");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		// for Empty NfcTAG
		IntentFilter techDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

		m_filters = new IntentFilter[] { ndefDiscovered, techDiscovered };
		m_techLists = new String[][] { new String[] { Ndef.class.getName() } };

		m_nfcProgressDialog = new ProgressDialog(MainActivity.this);
		m_nfcProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		m_nfcProgressDialog.setMessage("Tap for a NFC TAG to write");
		m_nfcProgressDialog.setCancelable(true);
		m_nfcProgressDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				m_waitToWriteTAG = false;
			}
		});

	}

	private OnTabChangeListener changeListener = new OnTabChangeListener() {

		@Override
		public void onTabChanged(String tabId) {
			setTabTextColor();
			// if (m_rendererCompactView.isShown())
			// hideRendererCompactView();
		}
	};

	protected void onResume() {
		super.onResume();
		if (m_nfcAdapter != null)
			m_nfcAdapter.enableForegroundDispatch(this, m_pendingIntent, m_filters, m_techLists);
	};

	@Override
	protected void onPause() {
		super.onPause();
		if (m_nfcAdapter != null)
			m_nfcAdapter.disableForegroundDispatch(MainActivity.this);
	}

	protected void onDestroy() {
		super.onDestroy();
		UPNP_PROCESSOR.unbindUpnpService();
		unregisterReceiver(m_mountedReceiver);
	};

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		createTabs();
		m_rendererCompactView = (RendererCompactView) findViewById(R.id.cv_compact_dmr);
		m_rendererCompactView.initComponent();
		m_rendererCompactView.setOnDMRChangeListener(m_onDMRChanged);
		btn_toggleRendererView = (ImageView) findViewById(R.id.btn_toggleShowHide);
	}

	private OnDMRChangeListener m_onDMRChanged = new OnDMRChangeListener() {

		@Override
		public void onDMRChange() {
			String tabTag = getTabHost().getCurrentTabTag();
			Activity activity = getLocalActivityManager().getActivity(tabTag);
			if (activity instanceof NowPlayingActivity) {
				((NowPlayingActivity) activity).updateDMRControlView();
				((NowPlayingActivity) activity).updateItemInfo();
			}
			MainActivity.UPNP_PROCESSOR.getDMRProcessor()
					.setPlaylistProcessor(MainActivity.UPNP_PROCESSOR.getPlaylistProcessor());
		}

		@Override
		public void onDMRUpdate() {
			Log.e(TAG, "On DMR Update");
			String tabTag = getTabHost().getCurrentTabTag();
			Activity activity = getLocalActivityManager().getActivity(tabTag);
			if (activity instanceof NowPlayingActivity) {
				((NowPlayingActivity) activity).updateDMRControlView();
			}
		}
	};

	private void createTabs() {
		Intent intent = null;

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TabSpec library = m_tabHost.newTabSpec(getString(R.string.library));
		TextView tvMediaSource = (TextView) inflater.inflate(R.layout.cv_tabwidget, null);
		tvMediaSource.setText(R.string.library);
		library.setIndicator(tvMediaSource);
		intent = new Intent(this, LibraryActivity.class);
		library.setContent(intent);

		TabSpec nowPlayling = m_tabHost.newTabSpec(getString(R.string.now_playing));
		TextView tvPlaylist = (TextView) inflater.inflate(R.layout.cv_tabwidget, null);
		tvPlaylist.setText(R.string.now_playing);
		nowPlayling.setIndicator(tvPlaylist);
		intent = new Intent(this, NowPlayingActivity.class);
		nowPlayling.setContent(intent);

		m_tabHost.addTab(library);
		m_tabHost.addTab(nowPlayling);
		m_tabHost.setCurrentTab(DEFAULT_TAB_INDEX);
		m_tabHost.setOnTabChangedListener(changeListener);
		m_tabHost.refreshDrawableState();
		setTabTextColor();
	}

	private void setTabTextColor() {
		for (int i = 0; i < m_tabHost.getTabWidget().getChildCount(); ++i) {
			View view = m_tabHost.getTabWidget().getChildAt(i);
			if (view instanceof TextView)
				((TextView) view).setTextColor(getResources().getColor(R.color.blue));
		}
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
						((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
								.cancel(CoreUpnpService.NOTIFICATION);
					}
				}).setCancelable(false).create().show();
	}

	@Override
	public void finishFromChild(Activity child) {
		Log.e(TAG, "Finish from child " + m_tabHost.getCurrentTabTag());
		if (m_tabHost.getCurrentTabTag().equals(getString(R.string.library))) {
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

	private android.view.View.OnClickListener customMenuItemClick = new android.view.View.OnClickListener() {

		@Override
		public void onClick(View v) {
			m_ll_menu.setVisibility(View.GONE);
			switch (((Integer) v.getTag()).intValue()) {
			case 0:
				refreshDevicesList();
				break;
			case 1:
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
			case 2:
				Toast.makeText(MainActivity.this, "Show about dialog", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	private String m_messageToWrite;

	private void refreshDevicesList() {
		UPNP_PROCESSOR.refreshDevicesList();
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
		if (m_ll_menu.getVisibility() == View.VISIBLE) {
			m_ll_menu.setVisibility(View.GONE);
		} else {
			confirmExit();
		}

	}

	private void confirmExit() {
		new AlertDialog.Builder(MainActivity.this).setTitle("Confirm exit").setMessage("Are you sure want to exit?")
				.setPositiveButton(R.string.minimize, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						moveTaskToBack(true);
					}
				}).setNegativeButton(R.string.close, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create().show();
	}

	public void onMenuClick(View view) {
		if (m_ll_menu.getVisibility() == View.VISIBLE) {
			m_ll_menu.setVisibility(View.GONE);
		} else {
			m_ll_menu.setVisibility(View.VISIBLE);
			if (m_ll_menu.getChildCount() == 0) {
				String[] menuItems = getResources().getStringArray(R.array.menu_items);
				for (int i = 0; i < menuItems.length; ++i) {
					TextView tv = new TextView(this);
					tv.setText(menuItems[i]);
					tv.setTag(i);
					tv.setOnClickListener(customMenuItemClick);
					tv.setTextSize(20);
					tv.setBackgroundDrawable(MainActivity.this.getResources().getDrawable(R.drawable.bg_view_with_bottom_line));
					m_ll_menu.addView(tv);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			if (m_waitToWriteTAG && m_messageToWrite != null) {
				// write message to TAG
				writeDataToTAG(intent);
			} else {
				Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
				if (rawMsgs != null) {
					NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
					for (int i = 0; i < rawMsgs.length; i++) {
						msgs[i] = (NdefMessage) rawMsgs[i];
						Log.e(TAG, "NFC Tag receive");
						byte[] buffer = msgs[i].getRecords()[0].getPayload();
						String textEncoding = (buffer[0] & 0200) == 0 ? "UTF-8" : "UTF-16";
						int languageCodeLength = buffer[0] & 0077;
						try {
							String text = new String(buffer, languageCodeLength + 1, buffer.length - languageCodeLength - 1,
									textEncoding);
							String deviceUDN = "";
							if (text.startsWith("uuid:"))
								deviceUDN = text.substring(5);
							else
								deviceUDN = text;
							if (UPNP_PROCESSOR != null) {
								Device newDevice = null;
								if ((newDevice = UPNP_PROCESSOR.getRegistry().getDevice(new UDN(deviceUDN), true)) != null) {
									String alert = null;
									if (newDevice.getType().getType().equals("MediaServer")) {
										alert = "Detect Media Server From TAG";
										UPNP_PROCESSOR.setCurrentDMS(newDevice.getIdentity().getUdn());
									} else if (newDevice.getType().getType().equals("MediaRenderer")) {
										alert = "Detect Media Renderer From TAG";
										UPNP_PROCESSOR.setCurrentDMR(newDevice.getIdentity().getUdn());
									}
									if (alert != null)
										Toast.makeText(MainActivity.this, alert, Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(
											MainActivity.this,
											"Cannot find specified device from Nfc TAG, please check if device is connected to the network or the data on your TAG",
											Toast.LENGTH_LONG).show();
								}
							}
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
							Log.e(TAG, "NFC TAG parse text error");
						}

					}
				}
			}
		} else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			if (m_waitToWriteTAG && m_messageToWrite != null) {
				Log.e(TAG, "New Empty TAG detected, trying to write data");
				writeDataToTAG(intent);
			}
		}
	}

	private void writeDataToTAG(Intent intent) {
		try {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (NFCUtils.writeTag(NFCUtils.getNdefMessageFromString(m_messageToWrite), detectedTag)) {
				m_nfcProgressDialog.dismiss();
				Toast.makeText(this, "Success: Wrote text to nfc tag", Toast.LENGTH_LONG).show();
			} else {
				m_nfcProgressDialog.dismiss();
				Toast.makeText(this, "Write failed", Toast.LENGTH_LONG).show();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.e(TAG, "Unsupport encoding");
			Toast.makeText(this, "Write failed; cause = " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	};

	public void waitToWriteTAG(String text) {
		m_messageToWrite = text;
		m_waitToWriteTAG = true;
		m_nfcProgressDialog.show();
	}

	public void showRendererCompactView() {
		Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.compactrenderer_slidein);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_rendererCompactView.setVisibility(View.VISIBLE);
				m_rendererCompactView.updateListRenderer();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (btn_toggleRendererView != null)
					btn_toggleRendererView.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_navigate_down));
			}
		});
		m_rendererCompactView.startAnimation(animation);

	}

	public void hideRendererCompactView() {
		Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.compactrenderer_slideout);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_rendererCompactView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_rendererCompactView.setVisibility(View.GONE);
				if (btn_toggleRendererView != null)
					btn_toggleRendererView.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_navigate_up));
			}
		});
		m_rendererCompactView.startAnimation(animation);
	}

	public boolean isCompactRendererShowing() {
		return m_rendererCompactView.getVisibility() == View.VISIBLE;
	}

	public void onShowHideClick(View view) {
		if (isCompactRendererShowing())
			hideRendererCompactView();
		else
			showRendererCompactView();
	}
}
