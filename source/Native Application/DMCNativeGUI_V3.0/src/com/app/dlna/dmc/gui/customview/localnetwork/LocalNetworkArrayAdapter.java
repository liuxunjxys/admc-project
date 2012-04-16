package com.app.dlna.dmc.gui.customview.localnetwork;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import app.dlna.controller.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.utility.Utility;

public class LocalNetworkArrayAdapter extends ArrayAdapter<ListviewItem> {
	private LayoutInflater m_inflater = null;
	private Map<String, Bitmap> m_cacheDMSIcon;

	public LocalNetworkArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheDMSIcon = new HashMap<String, Bitmap>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView instanceof TextView) {
			convertView = m_inflater.inflate(R.layout.lvitem_localnetwork, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final ListviewItem object = getItem(position);

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		if (object.getData() instanceof Device)
			setDeviceItem((Device) object.getData(), holder);
		else if (object.getData() instanceof DIDLObject) {
			DIDLObject didlObject = (DIDLObject) object.getData();
			if (didlObject.getId().equals("-1")) {
				TextView tv = new TextView(getContext());
				tv.setHeight(48);
				tv.setGravity(Gravity.CENTER);
				tv.setText("Load more items");
				tv.setTextColor(Color.BLUE);
				return tv;
			} else {
				setDIDLObject(didlObject, holder);
			}
		}

		return convertView;
	}

	@SuppressWarnings("rawtypes")
	private void setDeviceItem(final Device device, final ViewHolder holder) {
		holder.name.setText(device.getDetails().getFriendlyName());
		final String udn = device.getIdentity().getUdn().getIdentifierString();

		if (device instanceof RemoteDevice) {
			holder.desc.setText(((RemoteDevice) device).getIdentity().getDescriptorURL().getAuthority());
		} else {
			holder.desc.setText("Local device");
		}

		if (m_cacheDMSIcon.containsKey(udn)) {
			holder.icon.setImageBitmap(m_cacheDMSIcon.get(udn));
		} else {
			if (device instanceof RemoteDevice) {
				try {
					final Icon[] icons = device.getIcons();
					if (icons != null && icons[0] != null && icons[0].getUri() != null) {

						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									final RemoteDevice remoteDevice = (RemoteDevice) device;

									String urlString = remoteDevice.getIdentity().getDescriptorURL().getProtocol()
											+ "://" + remoteDevice.getIdentity().getDescriptorURL().getAuthority()
											+ icons[0].getUri().toString();
									URL url = new URL(urlString);
									final Bitmap icon = BitmapFactory.decodeStream(url.openConnection()
											.getInputStream());
									m_cacheDMSIcon.put(udn, icon);
									MainActivity.INSTANCE.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											LocalNetworkArrayAdapter.this.notifyDataSetChanged();
										}
									});

								} catch (Exception ex) {
									MainActivity.INSTANCE.runOnUiThread(new Runnable() {

										@Override
										public void run() {
											holder.icon.setImageBitmap(null);
										}
									});
									ex.printStackTrace();
								}
							}
						}).start();

					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				holder.icon.setImageResource(R.drawable.ic_launcher);
			}
		}

	}

	private void setDIDLObject(DIDLObject object, ViewHolder holder) {
		holder.name.setText(object.getTitle());
		if (object.getId().equals("-1")) {
			holder.icon.setVisibility(View.GONE);
			holder.desc.setText("");
			return;
		}
		holder.icon.setVisibility(View.VISIBLE);
		if (object instanceof Container) {
			holder.icon.setImageResource(R.drawable.ic_didlobject_container);
			int childCount = ((Container) object).getChildCount();
			String childCountStr = "";
			if (childCount == 0)
				childCountStr = "empty";
			else if (childCount == 1)
				childCountStr = "1 child";
			else
				childCountStr = childCount + " childs";
			holder.desc.setText(childCountStr);
		} else {
			if (object instanceof MusicTrack) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
			} else if (object instanceof VideoItem) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_video);
			} else if (object instanceof ImageItem) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_image);
			} else {
				holder.icon.setImageResource(R.drawable.ic_didlobject_unknow);
			}
			if (object.getResources().size() > 0) {
				holder.desc.setText(Utility.convertSizeToString(object.getResources().get(0).getSize()));
			}
		}
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.name = (TextView) view.findViewById(R.id.name);
		viewHolder.desc = (TextView) view.findViewById(R.id.desc);
		viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
		view.setTag(viewHolder);
	}
	
	private class ViewHolder {
		TextView desc;
		TextView name;
		ImageView icon;
	}

}
