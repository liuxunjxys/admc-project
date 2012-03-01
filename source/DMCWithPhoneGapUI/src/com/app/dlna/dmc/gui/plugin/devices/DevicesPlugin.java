package com.app.dlna.dmc.gui.plugin.devices;

import org.json.JSONArray;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;

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

	@Override
	public PluginResult execute(String arg0, JSONArray arg1, String arg2) {
		Log.e(TAG, "Call start");
		if (ACTION_START.equals(arg0)) {
			UIWithPhonegapActivity.UPNP_PROCESSOR.addListener(this);
		}
		return new PluginResult(Status.OK);
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

	}

	@SuppressWarnings("rawtypes")
	private void addDMS(Device device) {
		String dms_html = createDMS(device);
		sendJavascript("add_dms(" + dms_html + ");");
	}

	@SuppressWarnings("rawtypes")
	private String createDMS(Device device) {

		final String udn = device.getIdentity().getUdn().getIdentifierString();
		// if (udn.equals(m_currentUDN)) {
		// holder.selected.setChecked(true);
		// } else {
		// holder.selected.setChecked(false);
		// }
		String deviceImage = "";
		String deviceAddress = "";
		if (device instanceof RemoteDevice) {
			deviceAddress = ((RemoteDevice) device).getIdentity().getDescriptorURL().getAuthority();
			try {
				final Icon[] icons = device.getIcons();
				if (icons != null && icons[0] != null && icons[0].getUri() != null) {

					final RemoteDevice remoteDevice = (RemoteDevice) device;

					deviceImage = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
							+ remoteDevice.getIdentity().getDescriptorURL().getAuthority() + icons[0].getUri().toString();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			deviceAddress = "Local Device";
		}

		StringBuilder result = new StringBuilder();
		result.append("\"");
		result.append("<div class='device_list_item'>");
		result.append("<div align='center' class='device_icon'><img style='width: 48px; height: 48px;' src='" + deviceImage + "'/></div>");
		result.append("<div class='device_info'/>");
		result.append("</div>\"");
		return result.toString();
		// return "\"<table>" + "<tr class='tr_itemcontent'>" + "<td class='td_listitem_left'>" + "<img class='img_imgoflistitem' alt='Server image' src='"
		// + deviceImage + "'/>" + "</td>" + "<td class='td_listitem_middle'>" + "<h5>" + device.getDetails().getFriendlyName() + "</h5>" + "<p>"
		// + deviceAddress + "</p>" + "</td>" + "</tr>" + "</table>\"";
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
