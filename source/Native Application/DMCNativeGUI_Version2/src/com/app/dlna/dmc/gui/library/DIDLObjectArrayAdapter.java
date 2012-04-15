package com.app.dlna.dmc.gui.library;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.controller.nativegui.R;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;

public class DIDLObjectArrayAdapter extends ArrayAdapter<DIDLObject> {
	private static final String TAG = DIDLObjectArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private PlaylistProcessor m_playlistProcessor;
	private List<DIDLObject> m_data;
	private Activity m_activity;

	public DIDLObjectArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_data = new ArrayList<DIDLObject>();
		m_activity = (Activity) context;
	}

	public void setPlaylistProcessor(PlaylistProcessor processor) {
		m_playlistProcessor = processor;
	}

	@Override
	public void add(DIDLObject object) {
		m_data.add(object);
		super.add(object);
	}

	@Override
	public void remove(DIDLObject object) {
		m_data.remove(object);
		super.remove(object);
	}

	@Override
	public void clear() {
		m_data.clear();
		super.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (position >= 0 && position < getCount()) {
			if (convertView == null) {
				Log.e(TAG, "ConvertView = null");
				convertView = m_inflater.inflate(R.layout.didlobject_listview_item, null, false);
			}
			if (convertView.getTag() == null) {
				setViewHolder(convertView);
			}
			ViewHolder holder = (ViewHolder) convertView.getTag();

			DIDLObject object = getItem(position);
			holder.name.setText(object.getTitle());
			if (object.getId().equals("-1")) {
				holder.icon.setVisibility(View.GONE);
				return convertView;
			}
			holder.icon.setVisibility(View.VISIBLE);
			if (object instanceof Container) {
				holder.icon.setImageResource(R.drawable.folder);
			} else {
				if (object instanceof MusicTrack) {
					holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
				} else if (object instanceof VideoItem) {
					holder.icon.setImageResource(R.drawable.ic_didlobject_video);
				} else if (object instanceof ImageItem) {
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
		} else {
			return super.getView(position, convertView, parent);
		}

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

	@Override
	public Filter getFilter() {
		return new Filter() {

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
			}

			@Override
			protected FilterResults performFiltering(final CharSequence constraint) {
				m_activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						DIDLObjectArrayAdapter.super.clear();
						if (constraint != null && constraint.length() != 0) {
							String query = constraint.toString().toLowerCase();
							Log.i(TAG, query);
							for (DIDLObject object : m_data) {
								if (object.getTitle().toLowerCase().contains(query)) {
									Log.i(TAG, object.getTitle());
									DIDLObjectArrayAdapter.super.add(object);
								}
							}
						} else {
							for (DIDLObject object : m_data) {
								DIDLObjectArrayAdapter.super.add(object);
							}
						}
						notifyDataSetChanged();
					}
				});

				return null;

			}
		};
	}
}
