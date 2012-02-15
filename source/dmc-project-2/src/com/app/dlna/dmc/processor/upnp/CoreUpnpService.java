package com.app.dlna.dmc.processor.upnp;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceConfiguration;
import org.teleal.cling.android.AndroidWifiSwitchableRouter;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.ModelUtil;
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

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.processor.http.HTTPServerData;
import com.app.dlna.dmc.processor.http.HttpThread;
import com.app.dlna.dmc.utility.Utility;

public class CoreUpnpService extends Service {
	private static final int NOTIFICATION = 1500;
	private static final String TAG = CoreUpnpService.class.getName();
	private HttpThread m_httpThread;
	private UpnpService upnpService;
	private Binder binder = new Binder();
	private NotificationManager m_notificationManager;
	private WifiLock m_wifiLock;

	@Override
	public void onCreate() {
		super.onCreate();
		WifiManager m_wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		m_wifiLock = m_wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "UpnpWifiLock");
		m_wifiLock.acquire();
		HTTPServerData.HOST = Utility.intToIp(m_wifiManager.getDhcpInfo().ipAddress);

		m_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		m_httpThread = new HttpThread();
		m_httpThread.start();

		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		upnpService = new UpnpServiceImpl(createConfiguration(wifiManager)) {
			@Override
			protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
				AndroidWifiSwitchableRouter router = CoreUpnpService.this.createRouter(getConfiguration(), protocolFactory,
						wifiManager, connectivityManager);
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

	protected AndroidWifiSwitchableRouter createRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory,
			WifiManager wifiManager, ConnectivityManager connectivityManager) {
		return new AndroidWifiSwitchableRouter(configuration, protocolFactory, wifiManager, connectivityManager);
	}

	@Override
	public void onDestroy() {
		if (!ModelUtil.ANDROID_EMULATOR && isListeningForConnectivityChanges())
			unregisterReceiver(((AndroidWifiSwitchableRouter) upnpService.getRouter()).getBroadcastReceiver());
		try {
			upnpService.shutdown();
		} catch (Exception ex) {
		}

		try {
			m_wifiLock.release();
		} catch (Exception ex) {
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

	public class Binder extends android.os.Binder implements AndroidUpnpService {

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
		Notification notification = new Notification(R.drawable.ic_launcher, "CoreUpnpService started",
				System.currentTimeMillis());

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);

		notification.setLatestEventInfo(this, "CoreUpnpService", "Service is running", contentIntent);

		m_notificationManager.notify(NOTIFICATION, notification);
	}
}
