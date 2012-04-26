package com.app.dlna.dmc.gui.subactivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.nowplaying.RendererControlView;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class NowPlayingActivity extends Activity {

	private RendererControlView m_rendererControl;
	protected String TAG = NowPlayingActivity.class.getName();
	private ViewFlipper m_viewFlipper;
	private ProgressDialog m_progressDialog;
	private View m_CurrentView;
	private Animation m_animFlipInNext;
	private Animation m_animFlipOutNext;
	private Animation m_animFlipInPrevious;
	private Animation m_animFlipOutPrevious;
	private boolean m_isAnimating;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);
		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);

		m_viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		m_progressDialog = new ProgressDialog(NowPlayingActivity.this);
		m_progressDialog.setTitle("Loading image");
		m_progressDialog.setCancelable(true);

		m_animFlipInNext = AnimationUtils.loadAnimation(this, R.anim.flipinnext);
		m_animFlipOutNext = AnimationUtils.loadAnimation(this, R.anim.flipoutnext);
		m_animFlipInPrevious = AnimationUtils.loadAnimation(this, R.anim.flipinprevious);
		m_animFlipOutPrevious = AnimationUtils.loadAnimation(this, R.anim.flipoutprevious);

		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(this);
		m_viewFlipper.setOnTouchListener(activitySwipeDetector);
		m_viewFlipper.addView(getLayoutInflater().inflate(R.layout.cv_galery_image, null));
		m_viewFlipper.addView(getLayoutInflater().inflate(R.layout.cv_galery_image, null));
	}

	private AnimationListener m_animationListner = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			m_isAnimating = true;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			m_isAnimating = false;
		}
	};

	public void loadNext() {
		if (m_isAnimating)
			return;
		m_viewFlipper.setInAnimation(m_animFlipInNext);
		m_viewFlipper.setOutAnimation(m_animFlipOutNext);
		m_viewFlipper.getInAnimation().setAnimationListener(m_animationListner);
		m_viewFlipper.showNext();
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().next();
		updateCurrentPlaylistItem();
		updateItemInfo();
	}

	public void loadPrev() {
		if (m_isAnimating)
			return;
		m_viewFlipper.setInAnimation(m_animFlipInPrevious);
		m_viewFlipper.setOutAnimation(m_animFlipOutPrevious);
		m_viewFlipper.getInAnimation().setAnimationListener(m_animationListner);
		m_viewFlipper.showPrevious();
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().previous();
		updateCurrentPlaylistItem();
	}

	private void updateItemInfo() {
		View view = m_viewFlipper.getCurrentView();
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
		if (item == null)
			return;
		((TextView) view.findViewById(R.id.title)).setText(item.getTitle());
		ImageView iv = (ImageView) view.findViewById(R.id.image);
		switch (item.getType()) {
		case AUDIO:
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_audio));
			break;
		case VIDEO:
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_video));
			break;
		case IMAGE:
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_image));
			break;
		default:
			break;
		}
	}

	private void updateCurrentPlaylistItem() {
		final PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
		if (item != null) {
			if (MainActivity.UPNP_PROCESSOR.getDMRProcessor() != null)
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setURIandPlay(item.getUrl());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_rendererControl.connectToDMR();
		updateItemInfo();
	}

	private class LoadImageAsync extends AsyncTask<String, Void, Map<Integer, Bitmap>> {

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "On pre excute");
			m_viewFlipper.setClickable(false);
			m_viewFlipper.setEnabled(false);
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
			m_viewFlipper.setEnabled(true);
			m_viewFlipper.setClickable(true);
			m_progressDialog.dismiss();
			super.onPostExecute(result);
		}

	}
}
