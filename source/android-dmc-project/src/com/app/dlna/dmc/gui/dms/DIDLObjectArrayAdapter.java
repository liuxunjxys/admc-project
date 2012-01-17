package com.app.dlna.dmc.gui.dms;

import java.util.List;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.dmc.R;

public class DIDLObjectArrayAdapter extends ArrayAdapter<DIDLObject> {
	private static final String TAG = DIDLObjectArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;

	public DIDLObjectArrayAdapter(Context context, int resource, int textViewResourceId, List<DIDLObject> objects) {
		super(context, resource, textViewResourceId, objects);
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

		DIDLObject object = getItem(position);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.name.setText(object.getTitle());
		if (object instanceof Container) {
			holder.icon.setImageResource(R.drawable.folder);
		} else {
			holder.icon.setImageResource(R.drawable.file);
		}

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
