package com.app.dlna.dmc.gui.localcontent;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.dms.DIDLObjectArrayAdapter;

public class ItemDisplayArrayAdapter extends ArrayAdapter<ItemDisplay> {
	private static final String TAG = DIDLObjectArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;

	public ItemDisplayArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			Log.e(TAG, "ConvertView = null");
			convertView = m_inflater.inflate(R.layout.didlobject_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}

		ItemDisplay object = getItem(position);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.name.setText(object.getItem().getTitle());
		holder.icon.setImageResource(object.getIconId());
		return convertView;
	}

	public void setViewHolder(View view) {
		Log.e(TAG, "SetConvertView");
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		viewHolder.name = (TextView) view.findViewById(R.id.itemName);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		ImageView icon;
		TextView name;
	}

}