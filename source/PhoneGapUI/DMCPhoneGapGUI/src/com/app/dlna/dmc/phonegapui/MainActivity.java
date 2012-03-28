package com.app.dlna.dmc.phonegapui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerDroidGapActivity;
import com.app.dlna.dmc.phonegapui.plugin.DevicesPlugin;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.receiver.SDCardReceiver;
import com.app.dlna.dmc.processor.systemservice.RestartService;

public class MainActivity extends UpnpListenerDroidGapActivity {

	public static MainActivity INSTANCE;
	public static UpnpProcessor UPNP_PROCESSOR;
	BroadcastReceiver m_mountedReceiver = new SDCardReceiver();
	private ProgressDialog m_routerProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.init();
		super.appView.getSettings().setJavaScriptEnabled(true);
		super.loadUrl("file:///android_asset/www/index.html");
		INSTANCE = this;
		UPNP_PROCESSOR = new UpnpProcessorImpl(MainActivity.this);
		UPNP_PROCESSOR.bindUpnpService();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addDataScheme("file");

		registerReceiver(m_mountedReceiver, filter);

	}

	protected void onResume() {
		super.onResume();
		Log.i(TAG, "MainActivity onResume");
	};

	protected void onPause() {
		super.onPause();
		Log.i(TAG, "MainActivity onPause");
	}

	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "MainActivity onDestroy");
		UPNP_PROCESSOR.unbindUpnpService();
		unregisterReceiver(m_mountedReceiver);
	};

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		UPNP_PROCESSOR.addListener(new DevicesPlugin(MainActivity.this));
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

	// @SuppressWarnings("rawtypes")
	// @Override
	// public void onDeviceAdded(Device device) {
	// super.onDeviceAdded(device);
	// if (device.getType().getNamespace().equals("schemas-upnp-org")) {
	// if (device.getType().getType().equals("MediaServer")) {
	// addDMS(device);
	// } else if (device.getType().getType().equals("MediaRenderer")) {
	// addDMR(device);
	// }
	// }
	// }
	//
	// @SuppressWarnings("rawtypes")
	// @Override
	// public void onDeviceRemoved(Device device) {
	// super.onDeviceRemoved(device);
	// if (device.getType().getNamespace().equals("schemas-upnp-org")) {
	// if (device.getType().getType().equals("MediaServer")) {
	// removeDMS(device);
	// } else if (device.getType().getType().equals("MediaRenderer")) {
	// removeDMR(device);
	// }
	// }
	// }
	//
	// @SuppressWarnings("rawtypes")
	// public void refresh() {
	// // synchronized (m_dmsAdapter) {
	// // m_dmsAdapter.clear();
	// // if (MainActivity.UPNP_PROCESSOR.getCurrentDMS() != null) {
	// //
	// m_dmsAdapter.setCurrentDeviceUDN(MainActivity.UPNP_PROCESSOR.getCurrentDMS().getIdentity().getUdn()
	// // .getIdentifierString());
	// // } else {
	// // m_dmsAdapter.setCurrentDeviceUDN("");
	// // }
	// // }
	// //
	// // synchronized (m_dmrAdapter) {
	// // m_dmrAdapter.clear();
	// // if (MainActivity.UPNP_PROCESSOR.getCurrentDMR() != null) {
	// //
	// m_dmrAdapter.setCurrentDeviceUDN(MainActivity.UPNP_PROCESSOR.getCurrentDMR().getIdentity().getUdn()
	// // .getIdentifierString());
	// // } else {
	// // m_dmrAdapter.setCurrentDeviceUDN("");
	// // }
	// // }
	// Log.i(TAG, "refresh");
	// for (Device device : MainActivity.UPNP_PROCESSOR.getDMSList()) {
	// Log.w(TAG, "device = " + device);
	// addDMS(device);
	// }
	//
	// for (Device device : MainActivity.UPNP_PROCESSOR.getDMRList()) {
	// Log.w(TAG, "device = " + device);
	// addDMR(device);
	// }
	// }
	//
	// @SuppressWarnings("rawtypes")
	// private void addDMR(final Device device) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // synchronized (m_dmrAdapter) {
	// // if (device instanceof LocalDevice)
	// // m_dmrAdapter.insert(device, 0);
	// // else
	// // m_dmrAdapter.add(device);
	// // }
	// Log.i(TAG, "add DMR " + device);
	// }
	// });
	//
	// }
	//
	// @SuppressWarnings("rawtypes")
	// private void removeDMR(final Device device) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // synchronized (m_dmrAdapter) {
	// // m_dmrAdapter.remove(device);
	// // }
	// Log.i(TAG, "remove DMR " + device);
	// }
	// });
	// }
	//
	// @SuppressWarnings("rawtypes")
	// private void addDMS(final Device device) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // synchronized (m_dmsAdapter) {
	// // if (device instanceof LocalDevice)
	// // m_dmsAdapter.insert(device, 0);
	// // else
	// // m_dmsAdapter.add(device);
	// // }
	// Log.i(TAG, "add DMS " + device);
	// }
	// });
	//
	// }
	//
	// @SuppressWarnings("rawtypes")
	// private void removeDMS(final Device device) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// // synchronized (m_dmsAdapter) {
	// // m_dmsAdapter.remove(device);
	// // }
	// Log.i(TAG, "remove DMS " + device);
	// }
	// });
	// }
}
