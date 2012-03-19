package com.app.dlna.dmc.gui.youtube;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dlna.dmc.nativeui.R;

public class YoutubeItemArrayAdapter extends ArrayAdapter<YoutubeItem> {

	private LayoutInflater m_inflater;
	private Map<String, Bitmap> m_cacheDMSIcon;
	private Activity m_mainActivity;

	public YoutubeItemArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheDMSIcon = new HashMap<String, Bitmap>();
		m_mainActivity = (Activity) context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.youtube_listview_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final YoutubeItem item = getItem(position);

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.title.setText(item.getTitle());
		holder.length.setText(item.getDuration() + " seconds");

		if (m_cacheDMSIcon.containsKey(item.getThumbnail())) {
			holder.thumbnail.setImageBitmap(m_cacheDMSIcon.get(item.getThumbnail()));
		} else {
			try {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							String urlString = item.getThumbnail();
							URL url = new URL(urlString);
							final Bitmap icon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
							m_cacheDMSIcon.put(item.getThumbnail(), icon);
							m_mainActivity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									YoutubeItemArrayAdapter.this.notifyDataSetChanged();
								}
							});

						} catch (Exception ex) {
							m_mainActivity.runOnUiThread(new Runnable() {
								public void run() {
									holder.thumbnail.setImageResource(R.drawable.icon_dms);
								};

							});
						}
					}
				}).start();
			} catch (Exception ex) {
			}
		}

		return convertView;
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.title = (TextView) view.findViewById(R.id.title);
		viewHolder.length = (TextView) view.findViewById(R.id.length);
		viewHolder.thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		TextView length;
		TextView title;
		ImageView thumbnail;
	}

}
