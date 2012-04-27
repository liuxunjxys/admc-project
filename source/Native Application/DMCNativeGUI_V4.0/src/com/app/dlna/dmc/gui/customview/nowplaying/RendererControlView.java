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
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.subactivity.NowPlayingActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListner;
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
	private SeekBar m_sb_volume;
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
		m_sb_volume = (SeekBar) findViewById(R.id.sb_volume);
		m_sb_duration.setOnSeekBarChangeListener(onSeekListener);
		m_sb_volume.setOnSeekBarChangeListener(onSeekListener);

	}

	public void connectToDMR() {
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor == null)
			return;
		dmrProcessor.addListener(m_dmrListener);
		dmrProcessor.setRunning(true);

	}

	public void disconnectToDMR() {
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor == null)
			return;
		dmrProcessor.removeListener(m_dmrListener);
	}

	private OnClickListener onPlayPauseClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			switch (m_playingState) {
			case STATE__PAUSE:
			case STATE_STOP:
				if (dmrProcessor != null)
					dmrProcessor.play();
				break;
			case STATE_PLAYING:
				if (dmrProcessor != null)
					dmrProcessor.pause();
				break;
			default:
				break;
			}
		}
	};
	private OnClickListener onStopClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor == null)
				return;
			dmrProcessor.stop();
		}
	};

	private OnClickListener onNextClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// doNext();
			NowPlayingActivity nowplaying = (NowPlayingActivity) RendererControlView.this.getContext();
			nowplaying.doNext();
		}
	};

	// private void doNext() {
	// if (RendererControlView.this.getContext() instanceof NowPlayingActivity)
	// {
	// NowPlayingActivity nowplaying = (NowPlayingActivity)
	// RendererControlView.this.getContext();
	// nowplaying.doNext();
	// }
	// }

	private OnClickListener onPrevClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// doPrev();
			NowPlayingActivity nowplaying = (NowPlayingActivity) RendererControlView.this.getContext();
			nowplaying.doPrev();
		}
	};

	// private void doPrev() {
	// if (RendererControlView.this.getContext() instanceof NowPlayingActivity)
	// {
	// NowPlayingActivity nowplaying = (NowPlayingActivity)
	// RendererControlView.this.getContext();
	// nowplaying.doPrev();
	// }
	// }

	private OnSeekBarChangeListener onSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_isSeeking = false;
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor == null)
				return;
			if (seekBar.equals(m_sb_duration)) {
				dmrProcessor.seek(Utility.getTimeString(seekBar.getProgress()));
			} else {
				dmrProcessor.setVolume(seekBar.getProgress());
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_isSeeking = true;

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (seekBar.equals(m_sb_duration))
				m_tv_current.setText(Utility.getTimeString(m_sb_duration.getProgress()));
		}
	};

	private DMRProcessorListner m_dmrListener = new DMRProcessorListner() {

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
					m_sb_volume.setProgress(MainActivity.UPNP_PROCESSOR.getDMRProcessor().getVolume());
				}
			});
		}

		@Override
		public void onStoped() {
			m_playingState = STATE_STOP;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.ic_btn_media_play));
				}
			});
		}

		@Override
		public void onPlaying() {
			m_playingState = STATE_PLAYING;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.ic_btn_media_pause));
				}
			});

		}

		@Override
		public void onPaused() {
			m_playingState = STATE__PAUSE;
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_btn_playPause.setImageDrawable(getContext().getResources().getDrawable(
							R.drawable.ic_btn_media_play));
				}
			});

		}

		@Override
		public void onErrorEvent(final String error) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					MainActivity.UPNP_PROCESSOR.refreshDevicesList();
				}
			});
		}

		@Override
		public void onEndTrack() {

		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onActionFail(Action actionCallback, final UpnpResponse response, final String cause) {
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					MainActivity.UPNP_PROCESSOR.refreshDevicesList();
				}
			});
		}

	};

}
