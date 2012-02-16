package com.app.dlna.dmc.gui.nowplaying;

import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.types.UDN;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dlna.dmc.gui.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListner;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class NowPlayingActivity extends UpnpListenerActivity implements DMRProcessorListner {
	private static final String TAG = NowPlayingActivity.class.getName();
	private DMRProcessor m_dmrProcessor;
	private UpnpProcessor m_upnpProcessor = null;
	private static final int STATE_PAUSE = 1;
	private static final int STATE_PLAYING = 2;
	private static final int STATE_STOP = 3;
	private int m_currentState;
	private Button m_btn_PlayPause;
	private Button m_btn_Stop;
	private SeekBar m_sb_playingProgress;
	private TextView m_tv_progressTime;
	private boolean m_isSeeking = false;
	private SeekBar m_sb_volume;
	private TextView m_tv_soundValue;
	protected boolean m_isFailed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "NowPlaying onCreate");
		m_currentState = STATE_STOP;
		setContentView(R.layout.nowplaying_activity);

		m_upnpProcessor = new UpnpProcessorImpl(NowPlayingActivity.this);
		m_upnpProcessor.bindUpnpService();

		m_btn_PlayPause = (Button) findViewById(R.id.playPause);
		m_btn_Stop = (Button) findViewById(R.id.stop);
		m_btn_Stop.setEnabled(false);
		m_sb_playingProgress = (SeekBar) findViewById(R.id.playingProgress);
		m_tv_progressTime = (TextView) findViewById(R.id.progressTime);
		m_sb_volume = (SeekBar) findViewById(R.id.volumeControl);
		m_tv_soundValue = (TextView) findViewById(R.id.soundValue);
		m_tv_soundValue.setVisibility(View.GONE);

		m_sb_playingProgress.setOnSeekBarChangeListener(playbackSeekListener);
		m_sb_volume.setOnSeekBarChangeListener(volumeSeekListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "NowPlaying onResume");
		m_upnpProcessor.addListener(NowPlayingActivity.this);
		m_dmrProcessor = m_upnpProcessor.getDMRProcessor();
		if (m_dmrProcessor == null) {
			Toast.makeText(NowPlayingActivity.this, "Cannot get DMR Processor", Toast.LENGTH_SHORT).show();
		} else {
			m_dmrProcessor.addListener(NowPlayingActivity.this);
		}
	}

	@Override
	protected void onPause() {
		Log.i(TAG, "NowPlaying onPause");
		m_upnpProcessor.removeListener(NowPlayingActivity.this);
		m_dmrProcessor = m_upnpProcessor.getDMRProcessor();
		if (m_dmrProcessor != null)
			m_dmrProcessor.removeListener(NowPlayingActivity.this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "NowPlaying onDestroy");
		m_upnpProcessor.unbindUpnpService();
		super.onDestroy();
	}

	private OnSeekBarChangeListener playbackSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_isSeeking = false;
			Log.e(TAG, "Progress = " + seekBar.getProgress());
			m_dmrProcessor.seek(getTimeString(seekBar.getProgress()));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_isSeeking = true;

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			m_tv_progressTime.setText(getTimeString(m_sb_playingProgress.getProgress()) + " / " + getTimeString(m_sb_playingProgress.getMax()));
		}
	};

	private OnSeekBarChangeListener volumeSeekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_tv_soundValue.setVisibility(View.GONE);
			m_dmrProcessor.setVolume(seekBar.getProgress());
			Log.e(TAG, "Stop tracking");
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			m_tv_soundValue.setVisibility(View.VISIBLE);
			Log.e(TAG, "Start tracking");
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			m_tv_soundValue.setText(String.valueOf(seekBar.getProgress()));
		}
	};

	public void onPlayPauseClick(View view) {
		switch (m_currentState) {
		case STATE_PAUSE:
		case STATE_STOP:
			m_dmrProcessor.play();
			break;
		case STATE_PLAYING:
			m_dmrProcessor.pause();
			break;
		default:
			break;
		}
	}

	public void onStopClick(View view) {
		m_dmrProcessor.stop();
	}

	public void onNextClick(View view) {
	}

	public void onPreviousClick(View view) {
	}

	public void onSoundClick(View view) {
		if (m_sb_volume.getVisibility() == View.VISIBLE) {
			m_sb_volume.setVisibility(View.GONE);
		} else {
			m_sb_volume.setVisibility(View.VISIBLE);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onActionFail(final Action actionCallback, final UpnpResponse operation, final String cause) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_dmrProcessor.dispose();
				m_upnpProcessor.setCurrentDMR(new UDN("null"));
				if (!m_isFailed) {
					m_isFailed = true;
					try {
						new AlertDialog.Builder(NowPlayingActivity.this).setTitle("Error").setMessage("Remote Device Error: " + cause)
								.setPositiveButton("OK", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										NowPlayingActivity.this.finish();
									}
								}).setCancelable(false).show();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});

	}

	@Override
	public void onUpdatePosition(final long current, final long max) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (!m_isSeeking) {
					m_sb_playingProgress.setMax((int) max);
					m_sb_playingProgress.setProgress((int) current);
					m_sb_playingProgress.invalidate();
					m_tv_progressTime.setText(getTimeString(current) + " / " + getTimeString(max));
				}
				m_sb_volume.setProgress(m_dmrProcessor.getVolume());
			}
		});
	}

	private String getTimeString(long seconds) {
		StringBuilder sb = new StringBuilder();

		long hour = seconds / 3600;
		long minute = (seconds - hour * 3600) / 60;
		long second = seconds - hour * 3600 - minute * 60;
		sb.append(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", second));

		return sb.toString();
	}

	@Override
	public void onPaused() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_btn_PlayPause.setBackgroundResource(R.drawable.play);
				m_btn_Stop.setEnabled(true);
				m_currentState = STATE_PAUSE;
			}
		});

	}

	@Override
	public void onStoped() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_btn_Stop.setEnabled(false);
				m_currentState = STATE_STOP;
				m_btn_PlayPause.setBackgroundResource(R.drawable.play);
			}
		});

	}

	@Override
	public void onPlaying() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_btn_PlayPause.setBackgroundResource(R.drawable.pause);
				m_btn_Stop.setEnabled(true);
				m_currentState = STATE_PLAYING;
			}
		});

	}

	@Override
	public void onStartComplete() {
		m_dmrProcessor = m_upnpProcessor.getDMRProcessor();
		if (m_dmrProcessor != null) {
			m_dmrProcessor.addListener(NowPlayingActivity.this);
		}
	}
}
