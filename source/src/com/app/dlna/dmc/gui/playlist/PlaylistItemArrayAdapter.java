package com.app.dlna.dmc.gui.playlist;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class PlaylistItemArrayAdapter extends ArrayAdapter<PlaylistItem> {
	private static final String TAG = PlaylistItemArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private PlaylistItem m_currentItem;

	public PlaylistItemArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setCurrentItem(PlaylistItem item) {
		m_currentItem = item;
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

		PlaylistItem object = getItem(position);
		ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.name.setText(object.getTitle());
		switch (object.getType()) {
		case AUDIO:
			holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
			break;
		case VIDEO:
			holder.icon.setImageResource(R.drawable.ic_didlobject_video);
			break;
		case IMAGE:
			holder.icon.setImageResource(R.drawable.ic_didlobject_image);
			break;
		default:
			break;
		}
		if (m_currentItem != null && m_currentItem.equals(object)) {
			convertView.setBackgroundColor(Color.rgb(242, 189, 15));
			holder.name.setTextColor(Color.BLACK);
		} else {
			convertView.setBackgroundColor(Color.BLACK);
			holder.name.setTextColor(Color.WHITE);
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
