package com.app.dlna.dmc.gui.customview.nowplaying;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
		if (convertView == null || !(convertView instanceof ImageView)) {
			convertView = new ImageView(m_context);
		}
		switch (item.getType()) {
		case AUDIO:
			((ImageView) convertView).setImageResource(R.drawable.ic_didlobject_audio);
			break;
		case VIDEO:
			((ImageView) convertView).setImageResource(R.drawable.ic_didlobject_video);
			break;
		case IMAGE:
			((ImageView) convertView).setScaleType(ScaleType.FIT_START);
			Utility.loadImageItemThumbnail((ImageView) convertView, item.getUri(), Cache.getBitmapCache(), MAX_SIZE);
			break;
		default:
			((ImageView) convertView).setImageResource(R.drawable.ic_didlobject_unknow);
			break;
		}
		return convertView;
	}

}
