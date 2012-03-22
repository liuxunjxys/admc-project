package com.app.dlna.dmc.processor.receiver;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.teleal.cling.android.AndroidNetworkAddressFactory;
import org.teleal.cling.transport.SwitchableRouter;

import com.app.dlna.dmc.processor.http.HTTPServerData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {
	private static final String TAG = NetworkStateReceiver.class.getSimpleName();
	private SwitchableRouter m_router;
	private RouterStateListener m_routerStateListener;
	private boolean m_disableWifiPending;
	protected int DISABLE_STATE_TIMEOUT = 10; // seconds
	private NetworkInterface m_interfaceCache = null;

	public NetworkStateReceiver(SwitchableRouter router, RouterStateListener routerStateListener) {
		m_router = router;
		m_routerStateListener = routerStateListener;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")
				&& !intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
			return;
		NetworkInterface ni = AndroidNetworkAddressFactory.getWifiNetworkInterface(
				(WifiManager) context.getSystemService(Context.WIFI_SERVICE),
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
		if (ni == null) {
			Log.i(TAG, "Disable router");
			m_router.disable();
			m_routerStateListener.onRouterDisabled();
			m_disableWifiPending = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						for (int i = 0; i < DISABLE_STATE_TIMEOUT; ++i) {
							Thread.sleep(1000);
							if (m_disableWifiPending) {
								if (i == DISABLE_STATE_TIMEOUT - 1)
									if (m_routerStateListener != null)
										m_routerStateListener.onRouterError("No network found");
							} else {
								break;
							}
						}
					} catch (InterruptedException e) {
					}

				}
			}).start();
		} else {
			Log.i(TAG, "Enable router");
			m_router.enable();
			m_routerStateListener.onRouterEnabled();

			if (m_interfaceCache == null || !ni.equals(m_interfaceCache)) {
				m_routerStateListener.onNetworkChanged(ni);
			}
			m_interfaceCache = ni;

			List<InetAddress> inets = Collections.list(ni.getInetAddresses());
			for (InetAddress inet : inets) {
				if (inet instanceof Inet4Address) {
					HTTPServerData.HOST = inet.getHostAddress();
					break;
				}
			}
			// while (ni.getInetAddresses().hasMoreElements()) {
			// InetAddress inet = ni.getInetAddresses().nextElement();
			// Log.i(TAG, inet.toString());
			// if (inet instanceof Inet4Address) {
			// HTTPServerData.HOST = inet.getHostAddress();
			// break;
			// }
			// }
			Log.i(TAG, "HOST = " + HTTPServerData.HOST);
			m_disableWifiPending = false;
		}
	}

	public interface RouterStateListener {
		void onRouterError(String cause);

		void onNetworkChanged(NetworkInterface ni);

		void onRouterEnabled();

		void onRouterDisabled();
	}
}
