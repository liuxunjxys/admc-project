package com.app.dlna.dmc.gui.customview.playlist;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class PlaylistItemArrayAdapter extends ArrayAdapter<PlaylistItem> {
	private static final String TAG = PlaylistItemArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private Map<String, Bitmap> m_cacheImageItem;

	public PlaylistItemArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheImageItem = new HashMap<String, Bitmap>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.lvitem_playlist_item, null, false);
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
			Utility.loadImageItemThumbnail(holder.icon, object.getUri(), m_cacheImageItem);
			break;
		default:
			holder.icon.setImageResource(R.drawable.ic_didlobject_unknow);
			break;
		}

		holder.action.setTag(new Integer(position));

		// if (m_currentItem != null && m_currentItem.equals(object)) {
		// convertView.setBackgroundColor(Color.rgb(242, 189, 15));
		// holder.name.setTextColor(Color.BLACK);
		// } else {
		// convertView.setBackgroundColor(Color.BLACK);
		// holder.name.setTextColor(Color.WHITE);
		// }

		return convertView;
	}

	public void setViewHolder(View view) {
		Log.e(TAG, "SetConvertView");
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
		viewHolder.name = (TextView) view.findViewById(R.id.name);
		viewHolder.desc = (TextView) view.findViewById(R.id.desc);
		viewHolder.action = (ImageButton) view.findViewById(R.id.action);
		viewHolder.action.setOnClickListener(m_actionClick);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		ImageView icon;
		TextView name;
		TextView desc;
		ImageButton action;
	}

	private OnClickListener m_actionClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().removeItem(getItem(position));
			remove(getItem(position));
		}
	};
}
