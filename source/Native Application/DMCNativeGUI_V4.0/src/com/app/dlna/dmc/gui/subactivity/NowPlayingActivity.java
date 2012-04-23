package com.app.dlna.dmc.gui.subactivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.nowplaying.RendererControlView;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;
import com.app.dlna.dmc.utility.Utility;

public class NowPlayingActivity extends Activity {

	private RendererControlView m_rendererControl;
	protected String TAG = NowPlayingActivity.class.getName();
	private ViewPager m_pager;
	private ProgressDialog m_progressDialog;
	private View m_CurrentView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);
		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);

		m_pager = (ViewPager) findViewById(R.id.viewPager);
		m_progressDialog = new ProgressDialog(NowPlayingActivity.this);
		m_progressDialog.setTitle("Loading image");
		m_progressDialog.setCancelable(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_rendererControl.connectToDMR();

		m_pager.setOnPageChangeListener(m_onPageChangeListener);
		m_pager.setAdapter(m_pagerAdapter);
	}

	private PagerAdapter m_pagerAdapter = new PagerAdapter() {

		@Override
		public boolean isViewFromObject(View view, Object key) {
			return view == key;
		}

		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			m_CurrentView = (View) object;
			Log.i(TAG, "setPrimary");
		};

		@Override
		public int getCount() {
			return MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems().size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		};

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View convertView = null;
			PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getItemAt(position);
			if (convertView == null) {
				convertView = ((LayoutInflater) NowPlayingActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_galery_image, null);
			}
			ImageView image = (ImageView) convertView.findViewById(R.id.image);
			switch (item.getType()) {
			case AUDIO:
				image.setImageResource(R.drawable.ic_didlobject_audio);
				break;
			case VIDEO:
				image.setImageResource(R.drawable.ic_didlobject_video);
				break;
			case IMAGE:
				image.setImageResource(R.drawable.ic_didlobject_image);
				break;
			default:
				image.setImageResource(R.drawable.ic_didlobject_unknow);
				break;
			}
			container.addView(convertView);
			return convertView;
		}
	};

	private OnPageChangeListener m_onPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			Log.i(TAG, "Pageselected");
			PlaylistProcessor playlist = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			PlaylistItem item = playlist.getItemAt(position);
			(new LoadImageAsync()).execute(item.getUri(), "256", "256", String.valueOf(position));
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	private class LoadImageAsync extends AsyncTask<String, Void, Map<Integer, Bitmap>> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "On pre excute");
			m_pager.setClickable(false);
			m_pager.setEnabled(false);
			m_progressDialog.show();
			super.onPreExecute();
		}

		@Override
		protected Map<Integer, Bitmap> doInBackground(String... params) {
			Log.i(TAG, "Load in background");
			String url = params[0];
			int width = Integer.parseInt(params[1]);
			int height = Integer.parseInt(params[2]);
			try {
				Map<Integer, Bitmap> result = new HashMap<Integer, Bitmap>();
				result.put(Integer.parseInt(params[3]), Utility.getBitmapFromURL(url, width < height ? width : height));
				return result;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Map<Integer, Bitmap> result) {
			if (result == null) {
				return;
			} else {
				int position = result.keySet().iterator().next();
				if (m_CurrentView != null) {
					ImageView img = (ImageView) m_CurrentView.findViewById(R.id.image);
					img.setImageBitmap(result.get(position));
					img.invalidate();
				}
			}
			m_pager.setEnabled(true);
			m_pager.setClickable(true);
			m_progressDialog.dismiss();
			super.onPostExecute(result);
		}

	}
}
