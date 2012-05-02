package com.app.dlna.dmc.gui.subactivity;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
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
import com.app.dlna.dmc.gui.customview.nowplaying.TopToolbarView;
import com.app.dlna.dmc.processor.async.AsyncTaskWithProgressDialog;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor.PlaylistListener;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class NowPlayingActivity extends Activity {
	protected String TAG = NowPlayingActivity.class.getName();
	private RendererControlView m_rendererControl;
	private TopToolbarView m_topToolbar;
	private ViewFlipper m_viewFlipper;
	private ProgressDialog m_progressDialog;
	private Animation m_animFlipInNext;
	private Animation m_animFlipOutNext;
	private Animation m_animFlipInPrevious;
	private Animation m_animFlipOutPrevious;
	private boolean m_waiting;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);
		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);
		m_topToolbar = (TopToolbarView) findViewById(R.id.topToolbar);

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

	private PlaylistListener m_playlistListener = new PlaylistListener() {

		@Override
		public void onPrev() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (m_waiting)
						return;
					m_viewFlipper.setInAnimation(m_animFlipInPrevious);
					m_viewFlipper.setOutAnimation(m_animFlipOutPrevious);
					m_viewFlipper.getInAnimation().setAnimationListener(m_animationListner);
					m_viewFlipper.showPrevious();
					updateCurrentPlaylistItem();
					updateItemInfo();
				}
			});
		}

		@Override
		public void onNext() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (m_waiting)
						return;
					m_viewFlipper.setInAnimation(m_animFlipInNext);
					m_viewFlipper.setOutAnimation(m_animFlipOutNext);
					m_viewFlipper.getInAnimation().setAnimationListener(m_animationListner);
					m_viewFlipper.showNext();
					updateCurrentPlaylistItem();
					updateItemInfo();
				}
			});

		}
	};

	private AnimationListener m_animationListner = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			m_waiting = true;
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			m_waiting = false;
		}
	};

	public void doNext() {
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().next();
	}

	public void doPrev() {
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().previous();
	}

	public void updateItemInfo() {
		View view = m_viewFlipper.getCurrentView();
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
		if (item == null)
			return;
		((TextView) view.findViewById(R.id.title)).setText(item.getTitle());
		ImageView iv = (ImageView) view.findViewById(R.id.image);
		switch (item.getType()) {
		case AUDIO:
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_audio_large));
			break;
		case VIDEO:
			iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_video_large));
			break;
		case IMAGE:
			iv.setImageDrawable(null);
			new AsyncTaskWithProgressDialog<String, Void, Bitmap>("Loading image") {

				protected void onPreExecute() {
					m_waiting = true;
				};

				@Override
				protected Bitmap doInBackground(String... params) {
					Log.i(TAG, "Load in background");
					String url = params[0];
					int width = Integer.parseInt(params[1]);
					int height = Integer.parseInt(params[2]);
					try {
						return Utility.getBitmapFromURL(url, width < height ? width : height);
					} catch (MalformedURLException e) {
						e.printStackTrace();
						return null;
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					// super.onPostExecute(result);
					m_waiting = false;
					ImageView iv = (ImageView) m_viewFlipper.getCurrentView().findViewById(R.id.image);
					if (result == null) {
						iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_image_large));
					} else {
						Log.i(TAG, "w = " + result.getWidth() + "; h = " + result.getHeight());
						iv.setImageBitmap(result);
					}
				}
			}.execute(new String[] { item.getUrl(), "512", "512" });
			break;
		default:
			break;
		}
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor != null) {
			dmrProcessor.setURIandPlay(item.getUrl());
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
		updatePlaylist();
		updateItemInfo();
		m_topToolbar.updateToolbar();
	}

	public void updatePlaylist() {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null)
			playlistProcessor.addListener(m_playlistListener);
	}

	@Override
	protected void onPause() {
		super.onPause();
		m_rendererControl.disconnectToDMR();
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null)
			playlistProcessor.removeListener(m_playlistListener);
	}

	public void updateDMRControlView() {
		m_rendererControl.connectToDMR();
	}

}
