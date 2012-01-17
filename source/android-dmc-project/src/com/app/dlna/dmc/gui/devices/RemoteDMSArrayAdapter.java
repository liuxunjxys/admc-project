package com.app.dlna.dmc.gui.devices;

import org.teleal.cling.model.meta.RemoteDevice;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.dlna.dmc.R;

public class RemoteDMSArrayAdapter extends ArrayAdapter<RemoteDevice> {

	private static final String TAG = RemoteDMSArrayAdapter.class.getName();
	private LayoutInflater m_inflater;

	public RemoteDMSArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.dms_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		RemoteDevice device = getItem(position);
		Log.e(TAG, "adding device to listview :" + device.getDetails().getFriendlyName());

		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.deviceName.setText(device.getDetails().getFriendlyName());
		holder.deviceType.setText("Digital Media Server");

		return convertView;
	}

	@Override
	public void add(RemoteDevice object) {
		if (object.getType().getNamespace().compareTo("schemas-upnp-org") == 0 && object.getType().getType().compareTo("MediaServer") == 0)
			super.add(object);
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
		viewHolder.deviceType = (TextView) view.findViewById(R.id.deviceType);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		TextView deviceType;
		TextView deviceName;
	}
}
