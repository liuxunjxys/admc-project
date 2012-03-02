package com.app.dlna.dmc.gui.plugin.devices;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.UDN;
import org.teleal.common.util.Base64Coder;

import android.util.Log;

import com.app.dlna.dmc.gui.UIWithPhonegapActivity;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor.UpnpProcessorListener;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class DevicesPlugin extends Plugin implements UpnpProcessorListener {
	private static final String TAG = DevicesPlugin.class.getSimpleName();
	private static final String ACTION_START = "start";
	private static final String ACTION_STOP = "stop";
	private static final String ACTION_SET_DMS = "setDMS";
	private static final String ACTION_SET_DMR = "setDMR";
	@SuppressWarnings("rawtypes")
	private List<Device> m_dms_list = new ArrayList<Device>();
	@SuppressWarnings("rawtypes")
	private List<Device> m_dmr_list = new ArrayList<Device>();

	@SuppressWarnings("rawtypes")
	@Override
	public PluginResult execute(String action, JSONArray data, String callID) {
		Log.e(TAG, "Call start");
		PluginResult result = new PluginResult(Status.OK);
		if (ACTION_START.equals(action)) {
			UIWithPhonegapActivity.UPNP_PROCESSOR.addListener(this);
			for (Device device : UIWithPhonegapActivity.UPNP_PROCESSOR.getDMSList()) {
				addDMS(device);
			}
			for (Device device : UIWithPhonegapActivity.UPNP_PROCESSOR.getDMRList()) {
				addDMR(device);
			}
		} else if (ACTION_STOP.equals(action)) {
			UIWithPhonegapActivity.UPNP_PROCESSOR.removeListener(this);
		} else if (ACTION_SET_DMS.equals(action)) {
			try {
				setDMS(data.getString(0));
			} catch (Exception ex) {
				result = new PluginResult(Status.JSON_EXCEPTION);
			}
		} else if (ACTION_SET_DMR.equals(action)) {
			try {
				setDMR(data.getString(0));
			} catch (Exception ex) {
				result = new PluginResult(Status.JSON_EXCEPTION);
			}
		}
		return result;
	}

	private void setDMR(String udn) {
		UIWithPhonegapActivity.UPNP_PROCESSOR.setCurrentDMR(new UDN(udn));
	}

	private void setDMS(String udn) {
		UIWithPhonegapActivity.UPNP_PROCESSOR.setCurrentDMS(new UDN(udn));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceAdded(Device device) {
		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			if (device.getType().getType().equals("MediaServer")) {
				addDMS(device);
			} else if (device.getType().getType().equals("MediaRenderer")) {
				addDMR(device);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void addDMR(Device device) {
		if (!m_dmr_list.contains(device)) {
			m_dmr_list.add(device);
			String dmr_html = createDeviceElement(device, "dmr");
			sendJavascript("add_device(" + dmr_html + ",'dmr');");
		}
	}

	@SuppressWarnings("rawtypes")
	private void addDMS(Device device) {
		if (!m_dms_list.contains(device)) {
			m_dms_list.add(device);
			String dms_html = createDeviceElement(device, "dms");
			sendJavascript("add_device(" + dms_html + ",'dms');");
		}
	}

	@SuppressWarnings("rawtypes")
	private String createDeviceElement(Device device, String type) {

		final String udn = device.getIdentity().getUdn().getIdentifierString();
		String deviceImage = "";
		String deviceAddress = "";
		String deviceName = "";
		deviceName = device.getDetails().getFriendlyName();
		if (device instanceof RemoteDevice) {
			deviceAddress = ((RemoteDevice) device).getIdentity().getDescriptorURL().getAuthority();
			final Icon[] icons = device.getIcons();
			if (icons != null && icons[0] != null && icons[0].getUri() != null) {

				final RemoteDevice remoteDevice = (RemoteDevice) device;

				deviceImage = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
						+ remoteDevice.getIdentity().getDescriptorURL().getAuthority() + icons[0].getUri().toString();
			}
		} else {
			deviceAddress = "Local Device";
			byte[] bytes = device.getIcons()[0].getData();
			if (bytes.length != 0) {
				String base64String = new String(Base64Coder.encode(bytes));
				Log.e(TAG, base64String);
				deviceImage = "data:image/png;base64," + base64String;
			}
		}

		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append("<div class='device_list_item' type='" + type + "' udn='" + udn + "' onclick='onDeviceClick(this);'>");
		result.append("<div align='center' class='device_icon'>");
		result.append("<img class='img_device_icon' src='" + deviceImage + "'/>");
		result.append("</div>");
		result.append("<div class='device_info'>");
		result.append("<div class='div_device_name'>" + deviceName + "</div>");
		result.append("<div class='div_device_address'>" + deviceAddress + "</div>");
		result.append("</div>");
		result.append("</div>\"");
		return result.toString();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onDeviceRemoved(Device device) {
		if (device.getType().getNamespace().equals("schemas-upnp-org")) {
			if (device.getType().getType().equals("MediaServer")) {
				removeDMS(device);
			} else if (device.getType().getType().equals("MediaRenderer")) {
				removeDMR(device);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void removeDMR(Device device) {

	}

	@SuppressWarnings("rawtypes")
	private void removeDMS(Device device) {

	}

	@Override
	public void onStartComplete() {
	}

}
