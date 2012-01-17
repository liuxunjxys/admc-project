package com.app.dlna.dmc.gui.dmr;

import org.teleal.cling.model.meta.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.processor.ProcessorFactory;
import com.app.dlna.dmc.processor.interfaces.IDMRProcessor;
import com.app.dlna.dmc.processor.interfaces.IDMRProcessor.DMRProcessorListner;

public class DMRControllerActivity extends Activity implements DMRProcessorListner {

	private static final String TAG = DMRControllerActivity.class.getName();
	private String m_url;
	private IDMRProcessor m_processor;
	private static final int STATE_PAUSE = 1;
	private static final int STATE_PLAYING = 2;
	private static final int STATE_STOP = 3;
	private int m_currentState;
	private Button m_btn_PlayPause;
	private Button m_btn_Stop;
	private SeekBar m_sb_playingProgress;
	private TextView m_tv_progressTime;
	private boolean m_isSeeking = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_currentState = STATE_STOP;
		setContentView(R.layout.dmrcontroller_activity);

		m_btn_PlayPause = (Button) findViewById(R.id.playPause);
		m_btn_Stop = (Button) findViewById(R.id.stop);
		m_btn_Stop.setEnabled(false);
		m_sb_playingProgress = (SeekBar) findViewById(R.id.playingProgress);
		m_tv_progressTime = (TextView) findViewById(R.id.progressTime);

		m_sb_playingProgress.setOnSeekBarChangeListener(seekListener);

		Intent intent = getIntent();
		m_url = intent.getStringExtra("URL");
		Log.e(TAG, "Content uri = " + m_url);
		if (m_url == null) {
			new AlertDialog.Builder(DMRControllerActivity.this).setTitle("Error").setMessage("URL is null")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DMRControllerActivity.this.finish();
						}
					}).show();
		} else {
			String UDN = intent.getStringExtra("UDN");
			Log.e(TAG, "UDN = " + UDN);
			m_processor = ProcessorFactory.getDMRProcessorInstance(ProcessorFactory.getProcessorInstance(DMRControllerActivity.this).getRemoteDevice(UDN));
			m_processor.addListener(DMRControllerActivity.this);
			if (m_processor != null) {
				m_processor.setURI(m_url);
			}
		}
	}

	private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			m_isSeeking = false;
			Log.e(TAG, "Progress = " + seekBar.getProgress());
			m_processor.seek(getTimeString(seekBar.getProgress()));
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

	public void onPlayPauseClick(View view) {
		switch (m_currentState) {
		case STATE_PAUSE:
		case STATE_STOP:
			m_processor.play();
			break;
		case STATE_PLAYING:
			m_processor.pause();
			break;
		default:
			break;
		}
	}

	public void onStopClick(View view) {
		m_processor.stop();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onActionComplete(final Action action) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				String actionName = action.getName();
				if (actionName.compareTo("Play") == 0) {
					m_currentState = STATE_PLAYING;
					m_btn_PlayPause.setText("Pause");
					m_btn_Stop.setEnabled(true);
				} else if (actionName.compareTo("Pause") == 0) {
					m_currentState = STATE_PAUSE;
					m_btn_PlayPause.setText("Play");
					m_btn_Stop.setEnabled(true);
				} else if (actionName.compareTo("Stop") == 0) {
					m_btn_PlayPause.setText("Play");
					m_btn_Stop.setEnabled(false);
					m_currentState = STATE_STOP;
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		m_processor.dispose();
		m_processor.removeListener(DMRControllerActivity.this);
		super.onDestroy();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onActionFail(Action actionCallback, final String cause) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				new AlertDialog.Builder(DMRControllerActivity.this).setTitle("Error").setMessage(cause)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								DMRControllerActivity.this.finish();
							}
						}).setCancelable(false).show();
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

}
