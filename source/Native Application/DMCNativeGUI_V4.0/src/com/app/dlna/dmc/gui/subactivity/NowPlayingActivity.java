package com.app.dlna.dmc.gui.subactivity;

import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.nowplaying.RendererControlView;
import com.app.dlna.dmc.processor.async.AsyncTaskWithProgressDialog;
import com.app.dlna.dmc.processor.impl.LocalDMRProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor.PlaylistListener;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class NowPlayingActivity extends Activity implements Callback {
	protected String TAG = NowPlayingActivity.class.getName();
	private RendererControlView m_rendererControl;
	private ViewFlipper m_viewFlipper;
	private ProgressDialog m_progressDialog;
	private Animation m_animFlipInNext;
	private Animation m_animFlipOutNext;
	private Animation m_animFlipInPrevious;
	private Animation m_animFlipOutPrevious;
	private SurfaceView m_surface;
	private SurfaceHolder m_holder;
	private ImageView m_image;
	private LinearLayout m_content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);
		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);

		m_progressDialog = new ProgressDialog(NowPlayingActivity.this);
		m_progressDialog.setTitle("Loading image");
		m_progressDialog.setCancelable(true);

		m_swipeDetector = new SwipeDetector(this);
		m_image = (ImageView) findViewById(R.id.image);
		m_image.setOnTouchListener(m_swipeDetector);
		m_surface = (SurfaceView) findViewById(R.id.surface);
		m_surface.setOnTouchListener(m_swipeDetector);
		m_holder = m_surface.getHolder();
		m_holder.addCallback(this);
		m_holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		m_content = (LinearLayout) findViewById(R.id.content);

		m_animFlipInNext = AnimationUtils.loadAnimation(this, R.anim.flipinnext);
		m_animFlipInNext.setAnimationListener(m_animationListner);
		m_animFlipOutNext = AnimationUtils.loadAnimation(this, R.anim.flipoutnext);
		m_animFlipOutNext.setAnimationListener(m_animationListner);
		m_animFlipInPrevious = AnimationUtils.loadAnimation(this, R.anim.flipinprevious);
		m_animFlipInPrevious.setAnimationListener(m_animationListner);
		m_animFlipOutPrevious = AnimationUtils.loadAnimation(this, R.anim.flipoutprevious);
		m_animFlipOutPrevious.setAnimationListener(m_animationListner);

		m_viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
		m_viewFlipper.setOnTouchListener(m_swipeDetector);
		m_viewFlipper.addView(getLayoutInflater().inflate(R.layout.cv_tv_title, null));
		m_viewFlipper.addView(getLayoutInflater().inflate(R.layout.cv_tv_title, null));

	}

	private PlaylistListener m_playlistListener = new PlaylistListener() {

		@Override
		public void onPrev() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_viewFlipper.setInAnimation(m_animFlipInPrevious);
					m_viewFlipper.setOutAnimation(m_animFlipOutPrevious);
					m_viewFlipper.showPrevious();
					updateItemInfo();
				}
			});
		}

		@Override
		public void onNext() {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_viewFlipper.setInAnimation(m_animFlipInNext);
					m_viewFlipper.setOutAnimation(m_animFlipOutNext);
					m_viewFlipper.showNext();
					updateItemInfo();
				}
			});

		}
	};

	private AnimationListener m_animationListner = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
			if (item != null)
				((TextView) m_viewFlipper.getCurrentView()).setText(item.getTitle());
		}
	};
	private SwipeDetector m_swipeDetector;

	public void doNext() {
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().next();
	}

	public void doPrev() {
		MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().previous();
	}

	public void updateItemInfo() {
		if (MainActivity.UPNP_PROCESSOR.getCurrentDMR() == null)
			return;
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
		if (item == null)
			return;
		((TextView) m_viewFlipper.getCurrentView()).setText(item.getTitle());
		switch (item.getType()) {
		case AUDIO: {
			m_image.setVisibility(View.VISIBLE);
			m_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_audio_large));
			m_surface.setVisibility(View.GONE);
			break;
		}
		case VIDEO: {
			if (MainActivity.UPNP_PROCESSOR.getDMRProcessor() instanceof LocalDMRProcessorImpl) {
				m_surface.setVisibility(View.VISIBLE);
				m_image.setVisibility(View.GONE);
			} else {
				m_image.setVisibility(View.VISIBLE);
				m_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_video_large));
				m_surface.setVisibility(View.GONE);
			}
			break;
		}
		case IMAGE: {
			new AsyncTaskWithProgressDialog<String, Void, Bitmap>("Loading image") {

				protected void onPreExecute() {
					m_image.setVisibility(View.VISIBLE);
					m_surface.setVisibility(View.GONE);
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
					if (result == null) {
						m_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_didlobject_image_large));
					} else {
						m_image.setImageBitmap(result);
					}
				}
			}.execute(new String[] { item.getUrl(), "512", "512" });
			break;
		}
		default: {
			break;
		}
		}
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor != null) {
			dmrProcessor.setURIandPlay(item);
		}
		m_rendererControl.setCurrentSpinnerSelected(item);
		updateSurfaceView();
	}

	// private void updateCurrentPlaylistItem() {
	// final PlaylistItem item =
	// MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
	// if (item != null) {
	// if (MainActivity.UPNP_PROCESSOR.getDMRProcessor() != null)
	// MainActivity.UPNP_PROCESSOR.getDMRProcessor().setURIandPlay(item.getUrl());
	// }
	// }

	@Override
	protected void onResume() {
		super.onResume();
		updatePlaylist();
		updateItemInfo();
		m_rendererControl.connectToDMR();
		m_rendererControl.updateToolbar();
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

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i(TAG, "widht = " + width + ", height = " + height);
		updateSurfaceView();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		updateSurfaceView();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		updateSurfaceView();
	}

	private void updateSurfaceView() {
		try {
			if (MainActivity.UPNP_PROCESSOR.getDMRProcessor() instanceof LocalDMRProcessorImpl) {
				LocalDMRProcessorImpl localDMR = (LocalDMRProcessorImpl) MainActivity.UPNP_PROCESSOR.getDMRProcessor();
				if (m_surface.getVisibility() == View.VISIBLE) {
					localDMR.getPlayer().setDisplay(m_holder);
					localDMR.getPlayer().setSufaceDimension(m_content.getWidth(), m_content.getHeight());
				} else {
					localDMR.getPlayer().setDisplay(null);
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
