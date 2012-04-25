package com.app.dlna.dmc.gui.customview.nowplaying;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.cache.Cache;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class GaleryViewAdapter extends BaseAdapter {

	private Context m_context;
	private static final int MAX_SIZE = 128;

	public GaleryViewAdapter(Context context) {
		m_context = context;
	}

	@Override
	public int getCount() {
		return MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems().size();
	}

	@Override
	public Object getItem(int position) {
		return MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getItemAt(position);
		if (convertView == null) {
			convertView = ((LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.cv_galery_image, null);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		ViewHolder holder = (ViewHolder) convertView.getTag();
		switch (item.getType()) {
		case AUDIO:
			holder.image.setImageResource(R.drawable.ic_didlobject_audio);
			break;
		case VIDEO:
			holder.image.setImageResource(R.drawable.ic_didlobject_video);
			break;
		case IMAGE:
			holder.image.setScaleType(ScaleType.FIT_START);
			Utility.loadImageItemThumbnail(holder.image, item.getUrl(), Cache.getBitmapCache(), MAX_SIZE);
			break;
		default:
			holder.image.setImageResource(R.drawable.ic_didlobject_unknow);
			break;
		}
		return convertView;
	}

	private void setViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.image = (ImageView) convertView.findViewById(R.id.image);
		convertView.setTag(holder);
	}

	private class ViewHolder {
		ImageView image;
	}

}
