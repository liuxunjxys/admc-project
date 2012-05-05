package com.app.dlna.dmc.gui.customview.nowplaying;

import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.gui.customview.adapter.CustomArrayAdapter;
import com.app.dlna.dmc.gui.subactivity.NowPlayingActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListner;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.Playlist;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistManager;
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

	private Spinner m_spinner_playlist;
	private Spinner m_spinner_playlistItem;
	private CustomArrayAdapter m_playlistAdapter;
	private CustomArrayAdapter m_playlistItemAdapter;
	private ImageView m_tv_currentPlaylistName;
	private ImageView m_btn_playlistItemDropdown;
	private boolean m_flagPlaylistItem = true;
	private boolean m_flagPlaylist = true;

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

		m_tv_currentPlaylistName = (ImageView) findViewById(R.id.tv_playlistName);

		m_spinner_playlist = (Spinner) findViewById(R.id.spinner_playlist);
		m_playlistAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlist.setAdapter(m_playlistAdapter);
		m_spinner_playlist.setOnItemSelectedListener(m_playlistSelected);

		m_spinner_playlistItem = (Spinner) findViewById(R.id.playlistItem);
		m_playlistItemAdapter = new CustomArrayAdapter(MainActivity.INSTANCE, 0);
		m_spinner_playlistItem.setAdapter(m_playlistItemAdapter);
		m_spinner_playlistItem.setOnItemSelectedListener(m_playlistItemSelected);

		m_btn_playlistItemDropdown = (ImageView) findViewById(R.id.btn_fakeDropdown);
		m_btn_playlistItemDropdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_spinner_playlistItem.performClick();
			}
		});

		m_tv_currentPlaylistName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				m_spinner_playlist.performClick();
			}
		});

		updateToolbar();

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
					DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
					m_sb_volume.setMax(dmrProcessor.getMaxVolume());
					m_sb_volume.setProgress(dmrProcessor.getVolume());
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
					m_sb_duration.setProgress(0);
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

	private OnItemSelectedListener m_playlistItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			if (m_flagPlaylistItem) {
				m_flagPlaylistItem = !m_flagPlaylistItem;
				return;
			}
			PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			if (playlistProcessor == null)
				return;
			playlistProcessor.setCurrentItem(position);
			// DMRProcessor dmrProcessor =
			// MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			// if (dmrProcessor != null) {
			// dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem());
			// }
			NowPlayingActivity activity = (NowPlayingActivity) getContext();
			activity.updateItemInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	private OnItemSelectedListener m_playlistSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> adapter, View view, int position, long arg3) {
			if (m_flagPlaylist) {
				m_flagPlaylist = !m_flagPlaylist;
				return;
			}
			PlaylistProcessor playlistProcessor = PlaylistManager.getPlaylistProcessor((Playlist) m_playlistAdapter
					.getItem(position).getData());
			if (playlistProcessor.getAllItems().size() == 0) {
				Toast.makeText(getContext(), "Playlist is empty", Toast.LENGTH_SHORT).show();
				return;
			}

			MainActivity.UPNP_PROCESSOR.setPlaylistProcessor(playlistProcessor);
			MainActivity.UPNP_PROCESSOR.getDMRProcessor().setPlaylistProcessor(playlistProcessor);
			// m_tv_currentPlaylistName.setText(MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getData().getName());
			updatePlaylistItemSpinner();
			NowPlayingActivity activity = (NowPlayingActivity) getContext();
			activity.updatePlaylist();
			activity.updateItemInfo();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}

	};

	public void updateToolbar() {
		m_playlistAdapter.clear();
		for (Playlist playlist : PlaylistManager.getAllPlaylist())
			m_playlistAdapter.add(new AdapterItem(playlist));
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			m_spinner_playlist
					.setSelection(m_playlistAdapter.getPosition(new AdapterItem(playlistProcessor.getData())));
			// m_tv_currentPlaylistName.setText(playlistProcessor.getData().getName());
			updatePlaylistItemSpinner();
			DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
			if (dmrProcessor != null)
				dmrProcessor.setPlaylistProcessor(playlistProcessor);
		}
	}

	public void updatePlaylistItemSpinner() {
		m_playlistItemAdapter.clear();
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			for (PlaylistItem item : playlistProcessor.getAllItems())
				m_playlistItemAdapter.add(new AdapterItem(item));
			setCurrentSpinnerSelected(playlistProcessor.getCurrentItem());
		}

	}

	public void setCurrentSpinnerSelected(PlaylistItem item) {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor.getAllItems().size() > 0)
			m_spinner_playlistItem.setSelection(m_playlistItemAdapter.getPosition(new AdapterItem(playlistProcessor
					.getCurrentItem())));
	}

}
