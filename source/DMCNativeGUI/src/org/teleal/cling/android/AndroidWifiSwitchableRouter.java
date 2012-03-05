/*
 * Copyright (C) 2011 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.android;

import java.net.NetworkInterface;
import java.util.logging.Logger;

import org.teleal.cling.UpnpServiceConfiguration;
import org.teleal.cling.android.AndroidWifiSwitchableRouter.RouterStateListener;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.transport.Router;
import org.teleal.cling.transport.SwitchableRouterImpl;
import org.teleal.cling.transport.spi.InitializationException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Switches the network transport layer on/off by monitoring WiFi connectivity.
 * <p>
 * This implementation listens to connectivity changes in an Android environment. Register the {@link #getBroadcastReceiver()} instance with intent
 * <code>android.net.conn.CONNECTIVITY_CHANGE</code>.
 * </p>
 * 
 * @author Christian Bauer
 */
public class AndroidWifiSwitchableRouter extends SwitchableRouterImpl {

	private static final String TAG = "AndroidWifiSwitchableRouter";
	private static final int DISABLE_STATE_TIMEOUT = 15; // second

	// ndphu
	private boolean m_disableWifiPending = false;

	private static Logger log = Logger.getLogger(Router.class.getName());
	// TODO: remake the broadcast receiver to listen the connection state change event

	final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInterface ni = AndroidNetworkAddressFactory.getWifiNetworkInterface(wifiManager, connectivityManager);
			if (ni == null) {
				Log.i(TAG, "Disable router");
				disable();
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
											m_routerStateListener.onRouterDisabled();
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
				enable();
				m_disableWifiPending = false;
			}
		}
	};

	final private WifiManager wifiManager;
	final private ConnectivityManager connectivityManager;
	private WifiManager.MulticastLock multicastLock;
	private RouterStateListener m_routerStateListener;

	public AndroidWifiSwitchableRouter(UpnpServiceConfiguration configuration, ProtocolFactory protocolFactory, WifiManager wifiManager,
			ConnectivityManager connectivityManager) {
		super(configuration, protocolFactory);
		this.wifiManager = wifiManager;
		this.connectivityManager = connectivityManager;

		enable();
	}

	public BroadcastReceiver getBroadcastReceiver() {
		return broadcastReceiver;
	}

	protected WifiManager getWifiManager() {
		return wifiManager;
	}

	protected ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}

	@Override
	public boolean enable() throws RouterLockAcquisitionException {
		lock(writeLock);
		try {
			boolean enabled;
			if ((enabled = super.enable())) {

				// Enable multicast on the WiFi network interface, requires android.permission.CHANGE_WIFI_MULTICAST_STATE
				multicastLock = getWifiManager().createMulticastLock(getClass().getSimpleName());
				multicastLock.acquire();
			}
			return enabled;
		} finally {
			unlock(writeLock);
		}
	}

	@Override
	public void handleStartFailure(InitializationException ex) {
		if (multicastLock != null && multicastLock.isHeld()) {
			multicastLock.release();
			multicastLock = null;
		}
		super.handleStartFailure(ex);
	}

	@Override
	public boolean disable() throws RouterLockAcquisitionException {
		lock(writeLock);
		try {
			if (multicastLock != null && multicastLock.isHeld()) {
				multicastLock.release();
				multicastLock = null;
			}
			return super.disable();
		} finally {
			unlock(writeLock);
		}
	}

	@Override
	protected int getLockTimeoutMillis() {
		return 100000;
	}

	public void setRouterStateListener(RouterStateListener listener) {
		m_routerStateListener = listener;
	}

	public interface RouterStateListener {
		void onRouterDisabled();
	}

}
