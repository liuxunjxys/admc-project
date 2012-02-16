package com.app.dlna.dmc.processor.upnp;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.processor.http.HTTPServerData;
import com.app.dlna.dmc.processor.http.HttpThread;
import com.app.dlna.dmc.processor.impl.DMRProcessorImpl;
import com.app.dlna.dmc.processor.impl.DMSProcessorImpl;
import com.app.dlna.dmc.processor.impl.PlaylistProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.utility.Utility;

public class CoreUpnpService extends Service {
	private static final int NOTIFICATION = 1500;
	private static final String TAG = CoreUpnpService.class.getName();
	private HttpThread m_httpThread;
	private UpnpService upnpService;
	private CoreUpnpServiceBinder binder = new CoreUpnpServiceBinder();
	private RemoteDevice m_currentDMS;
	private RemoteDevice m_currentDMR;
	private PlaylistProcessor m_playlistProcessor;
	private NotificationManager m_notificationManager;
	private DMSProcessor m_dmsProcessor;
	private DMRProcessor m_dmrProcessor;
	private WifiLock m_wifiLock;

	@Override
	public void onCreate() {
		super.onCreate();
		WifiManager m_wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// prevent wifi sleep when screen
		m_wifiLock = m_wifiManager.createWifiLock(3, "UpnpWifiLock");
		m_wifiLock.acquire();
		HTTPServerData.HOST = Utility.intToIp(m_wifiManager.getDhcpInfo().ipAddress);

		m_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		m_httpThread = new HttpThread();
		m_httpThread.start();
		// Create playlist with capacity = 100;
		m_playlistProcessor = new PlaylistProcessorImpl(100);

		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		upnpService = new UpnpServiceImpl(createConfiguration(wifiManager)) {
			@Override
			protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
				AndroidWifiSwitchableRouter router = CoreUpnpService.this.createRouter(getConfiguration(), protocolFactory, wifiManager, connectivityManager);
				if (!ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges()) {
					registerReceiver(router.getBroadcastReceiver(), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
				}
				return router;
			}
		};

		showNotification();

	}

	protected AndroidUpnpServiceConfiguration createConfiguration(WifiManager wifiManager) {
		return new AndroidUpnpServiceConfiguration(wifiManager);
	}

	protected AndroidWifiSwitchableRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, WifiManager wifiManager,
			ConnectivityManager connectivityManager) {
		return new AndroidWifiSwitchableRouter(configuration, protocolFactory, wifiManager, connectivityManager);
	}

	@Override
	public void onDestroy() {
		if (!ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges())
			unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());
		try {
			upnpService.shutdown();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			m_wifiLock.release();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		m_httpThread.stopHttpThread();
		m_notificationManager.cancel(NOTIFICATION);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	protected boolean isListeningForConnectivityChanges() {
		return true;
	}

	public class CoreUpnpServiceBinder extends android.os.Binder implements AndroidUpnpService {

		public PlaylistProcessor getPlaylistProcessor() {
			return m_playlistProcessor;
		}

		public DMSProcessor getDMSProcessor() {
			return m_dmsProcessor;
		}

		public DMRProcessor getDMRProcessor() {
			return m_dmrProcessor;
		}

		public void setCurrentDMS(UDN uDN) {
			m_currentDMS = upnpService.getRegistry().getRemoteDevice(uDN, true);
			if (m_currentDMS != null) {
				Log.d(TAG, "CURRENT DMS:" + m_currentDMS.toString());
				Toast.makeText(getApplicationContext(), "Set DMS complete: " + m_currentDMS.getDisplayString(), Toast.LENGTH_SHORT).show();
				m_dmsProcessor = new DMSProcessorImpl(getControlPoint(), m_currentDMS);
			} else {
				Log.e(TAG, "GET DMS FAIL:" + uDN.toString());
				Toast.makeText(getApplicationContext(), "Set DMS fail. Cannot get DMS info; UDN = " + uDN.toString(), Toast.LENGTH_SHORT).show();
				m_dmsProcessor = null;
			}
		}

		public void setCurrentDMR(UDN uDN) {
			m_currentDMR = upnpService.getRegistry().getRemoteDevice(uDN, true);
			if (m_currentDMR != null) {
				Log.d(TAG, "CURRENT DMR:" + m_currentDMR.toString());
				Toast.makeText(getApplicationContext(), "Set DMR complete: " + m_currentDMR.getDisplayString(), Toast.LENGTH_SHORT).show();
				m_dmrProcessor = new DMRProcessorImpl(m_currentDMR, getControlPoint());
			} else {
				Log.e(TAG, "GET DMR FAIL:" + uDN.toString());
				Toast.makeText(getApplicationContext(), "Set DMR fail. Cannot get DMR info; UDN = " + uDN.toString(), Toast.LENGTH_SHORT).show();
				m_dmrProcessor = null;
			}
		}

		public RemoteDevice getCurrentDMS() {
			return m_currentDMS;
		}

		public RemoteDevice getCurrentDMR() {
			return m_currentDMR;
		}

		public UpnpService get() {
			return upnpService;
		}

		public UpnpServiceConfiguration getConfiguration() {
			return upnpService.getConfiguration();
		}

		public Registry getRegistry() {
			return upnpService.getRegistry();
		}

		public ControlPoint getControlPoint() {
			return upnpService.getControlPoint();
		}
	}

	private void showNotification() {
		Notification notification = new Notification(R.drawable.ic_launcher, "CoreUpnpService started", System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(CoreUpnpService.this, MainActivity.class), 0);

		notification.setLatestEventInfo(this, "CoreUpnpService", "Service is running", contentIntent);
		notification.flags = Notification.FLAG_NO_CLEAR;

		m_notificationManager.notify(NOTIFICATION, notification);
	}
}