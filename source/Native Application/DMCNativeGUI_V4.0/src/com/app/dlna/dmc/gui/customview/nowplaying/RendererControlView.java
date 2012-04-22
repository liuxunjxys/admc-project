package com.app.dlna.dmc.gui.customview.nowplaying;

import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListner;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class RendererControlView extends LinearLayout {

	private int m_playingState;
	private static final int STATE_PLAYING = 0;
	private static final int STATE__PAUSE = 1;
	private static final int STATE_STOP = 2;
	private TextView m_tv_current;
	private TextView m_tv_max;
	private ImageView m_btn_playPause, m_btn_stop, m_btn_next, m_btn_prev;
	private SeekBar m_sb_duration;
	private boolean m_isSeeking = false;

	public RendererControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_nowplayling_renderer_control, this);

		m_tv_current = (TextView) findViewById(R.id.tv_current);
		m_tv_max = (TextView) findViewById(R.id.tv_max);

		m_btn_prev = (ImageView) findViewById(R.id.btn_prev);
		m_btn_prev.setOnClickListener(onPrevClick);
		m_btn_playPause = (ImageView) findViewById(R.id.btn_playPause);
		m_btn_playPause.setOnClickListener(onPlayPauseClick);
		m_btn_stop = (ImageView) findViewById(R.id.btn_stop);
		m_btn_stop.setOnClickListener(onStopClick);
		m_btn_next = (ImageView) findViewById(R.id.btn_next);
		m_btn_next.setOnClickListener(onNextClick);

		m_sb_duration = (SeekBar) findViewById(R.id.sb_duration);
		m_sb_duration.setOnSeekBarChangeListener(onSeekListener);

	}

	public void connectToDMR() {
		MainActivity.UPNP_PROCESSOR.getDMRProcessor().addListener(dmrListener);
	}

	private OnClickListener onPlayPauseClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (m_playingState) {
			case STATE__PAUSE:
			case STATE_STOP:
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().play();
				break;
			case STATE_PLAYING:
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().pause();
				break;
			default:
				break;
			}
		}
	};
	private OnClickListener onStopClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			MainActivity.UPNP_PROCESSOR.getDMRProcessor().stop();
		}
	};

	private OnClickListener onNextClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			doNext();
		}
	};

	private OnClickListener onPrevClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			doPrevious();
		}
	};

	private void doNext() {
		if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor() != null && MainActivity.UPNP_PROCESSOR.getDMRProcessor() != null) {
			MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().next();
			updateCurrentPlaylistItem();
		}
	}

	private void updateCurrentPlaylistItem() {
		final PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getCurrentItem();
		if (item != null) {
			if (MainActivity.UPNP_PROCESSOR.getDMRProcessor() != null)
				MainActivity.UPNP_PROCESSOR.getDMRProcessor().setURIandPlay(item.getUri());
		}
	}

	public void onPreviousClick(View view) {
		doPrevious();
	}

	private void doPrevious() {
		if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor() != null && MainActivity.UPNP_PROCESSOR.getDMRProcessor() != null) {
			MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().previous();
			updateCurrentPlaylistItem();
		}
	}

	private OnSeekBarChangeListener onSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_isSeeking = false;
			MainActivity.UPNP_PROCESSOR.getDMRProcessor().seek(Utility.getTimeString(seekBar.getProgress()));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_isSeeking = true;

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			m_tv_current.setText(Utility.getTimeString(m_sb_duration.getProgress()));
		}
	};

	private DMRProcessorListner dmrListener = new DMRProcessorListner() {

		@Override
		public void onUpdatePosition(final long current, final long max) {
			if (m_isSeeking)
				return;
			final String currentText = Utility.getTimeString(current);
			final String maxText = Utility.getTimeString(max);
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_tv_current.setText(currentText);
					m_tv_max.setText(maxText);
					m_sb_duration.setMax((int) max);
					m_sb_duration.setProgress((int) current);
					m_sb_duration.invalidate();
				}
			});
		}

		@Override
		public void onStoped() {
			m_playingState = STATE_STOP;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_media_play));
				}
			});
		}

		@Override
		public void onPlaying() {
			m_playingState = STATE_PLAYING;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_media_pause));
				}
			});

		}

		@Override
		public void onPaused() {
			m_playingState = STATE__PAUSE;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_media_play));
				}
			});

		}

		@Override
		public void onErrorEvent(final String error) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
				}
			});
		}

		@Override
		public void onEndTrack() {

		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onActionFail(Action actionCallback, UpnpResponse response, final String cause) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getContext(), "Action fail: cause = " + cause, Toast.LENGTH_SHORT).show();
				}
			});
		}
	};

}
