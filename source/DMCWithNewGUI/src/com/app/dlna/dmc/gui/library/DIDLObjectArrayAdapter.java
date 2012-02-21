package com.app.dlna.dmc.gui.library;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.cling.support.model.item.VideoItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;

public class DIDLObjectArrayAdapter extends ArrayAdapter<DIDLObject> {
	private static final String TAG = DIDLObjectArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private PlaylistProcessor m_playlistProcessor;

	public DIDLObjectArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setPlaylistProcessor(PlaylistProcessor processor) {
		m_playlistProcessor = processor;
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
			if (object instanceof MusicTrack) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
			} else if (object instanceof VideoItem) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_video);
			} else if (object instanceof Photo) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_image);
			} else {
				holder.icon.setImageResource(R.drawable.file);
			}
		}
		if (m_playlistProcessor != null)
			if (object instanceof Item)
				if (m_playlistProcessor.containsUrl(object.getResources().get(0).getValue())) {
					holder.checked.setVisibility(View.VISIBLE);
				} else {
					holder.checked.setVisibility(View.GONE);
				}
			else {
				holder.checked.setVisibility(View.GONE);
			}

		return convertView;
	}

	public void setViewHolder(View view) {
		Log.e(TAG, "SetConvertView");
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.icon = (ImageView) view.findViewById(R.id.itemIcon);
		viewHolder.name = (TextView) view.findViewById(R.id.itemName);
		viewHolder.checked = (ImageView) view.findViewById(R.id.checked);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		ImageView icon;
		TextView name;
		ImageView checked;
	}

}
