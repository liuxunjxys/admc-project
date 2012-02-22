package com.app.dlna.dmc.gui.devices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.app.dlna.dmc.gui.R;

@SuppressWarnings("rawtypes")
public class DeviceArrayAdapter extends ArrayAdapter<Device> {

	private static final String TAG = DeviceArrayAdapter.class.getName();
	private LayoutInflater m_inflater;
	private Map<String, Bitmap> m_cacheDMSIcon;
	private String m_currentDMSUDN = "";
	private Activity m_mainActivity;

	public DeviceArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheDMSIcon = new HashMap<String, Bitmap>();
		m_mainActivity = (Activity) context;
	}

	public void setCurrentDMSUDN(String uDN) {
		m_currentDMSUDN = uDN;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.device_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final Device device = getItem(position);
		Log.e(TAG, "adding device to listview :" + device.getDetails().getFriendlyName());

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.deviceName.setText(device.getDetails().getFriendlyName());
		final String udn = device.getIdentity().getUdn().getIdentifierString();
		if (udn.equals(m_currentDMSUDN)) {
			holder.selected.setChecked(true);
			Log.e(TAG, "Selected");
		} else {
			holder.selected.setChecked(false);
			Log.e(TAG, "NotSelected");
		}

		if (device instanceof RemoteDevice) {
			holder.deviceAddress.setText(((RemoteDevice) device).getIdentity().getDescriptorURL().getAuthority());
		} else {
			holder.deviceAddress.setText("Local device");
		}

		if (m_cacheDMSIcon.containsKey(udn)) {
			Log.d(TAG, "Cache hit");
			holder.deviceIcon.setImageBitmap(m_cacheDMSIcon.get(udn));
		} else {
			Log.d(TAG, "Cache miss");
			if (device instanceof RemoteDevice) {
				final Icon[] icons = device.getIcons();
				if (icons != null && icons[0] != null && icons[0].getUri() != null) {

					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								final RemoteDevice remoteDevice = (RemoteDevice) device;

								String urlString = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
										+ remoteDevice.getIdentity().getDescriptorURL().getAuthority() + icons[0].getUri().toString();
								Log.e(TAG, urlString);
								URL url = new URL(urlString);
								final Bitmap icon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
								m_cacheDMSIcon.put(udn, icon);
								m_mainActivity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										DeviceArrayAdapter.this.notifyDataSetChanged();
									}
								});

							} catch (MalformedURLException e) {
								Log.e(TAG, "Can't get Icon of device: " + device.getDisplayString());
								holder.deviceIcon.setImageResource(R.drawable.icon_dms);
							} catch (IOException e) {
								Log.e(TAG, "Can't get Icon of device: " + device.getDisplayString());
								holder.deviceIcon.setImageResource(R.drawable.icon_dms);
							}
						}
					}).start();

				}
			} else {
				holder.deviceIcon.setImageResource(R.drawable.ic_launcher);
			}
		}

		return convertView;
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
		viewHolder.deviceAddress = (TextView) view.findViewById(R.id.deviceAddress);
		viewHolder.deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
		viewHolder.selected = (RadioButton) view.findViewById(R.id.selected);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		TextView deviceAddress;
		TextView deviceName;
		ImageView deviceIcon;
		RadioButton selected;
	}
}
