package com.app.dlna.dmc.processor.localdevice.renderer;

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.AVTransportException;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.model.DeviceCapabilities;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportSettings;
import org.teleal.cling.support.model.TransportState;

import android.media.MediaPlayer;
import android.util.Log;

import com.app.dlna.dmc.utility.Utility;

public class LocalAVTransportService extends AbstractAVTransportService {

	private static final String TAG = LocalAVTransportService.class.getSimpleName();
	MediaPlayer player = null;
	String url = null;
	private static final int STATE_PLAYING = 0;
	private static final int STATE_STOPED = 1;
	private static final int STATE_PAUSE = 2;
	private static final int STATE_NO_MEDIA = 3;
	private int state = STATE_NO_MEDIA;

	@Override
	public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String currentURI, String currentURIMetaData) throws AVTransportException {
		url = currentURI;
		Log.w(TAG, "setAVTransportURI");
	}

	@Override
	public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String nextURI, String nextURIMetaData) throws AVTransportException {
		Log.w(TAG, "setNextAVTransportURI");
	}

	@Override
	public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getMediaInfo");
		if (player != null) {
		}
		return new MediaInfo();
	}

	@Override
	public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getTransportInfo");

		switch (state) {
		case STATE_PLAYING:
			return new TransportInfo(TransportState.PLAYING);
		case STATE_NO_MEDIA:
			return new TransportInfo(TransportState.NO_MEDIA_PRESENT);
		case STATE_PAUSE:
			return new TransportInfo(TransportState.PAUSED_PLAYBACK);
		case STATE_STOPED:
			return new TransportInfo(TransportState.STOPPED);
		default:
			return new TransportInfo();
		}
	}

	@Override
	public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getPositionInfo");
		if (player != null)
			if (player.isPlaying())
				return new PositionInfo(1, Utility.converTimeToString(player.getDuration() / 1000), url,
						Utility.converTimeToString(player.getCurrentPosition() / 1000), Utility.converTimeToString(player.getDuration() / 1000));

		return new PositionInfo();
	}

	@Override
	public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getDeviceCapabilities");
		return null;
	}

	@Override
	public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getTransportSettings");
		return null;
	}

	@Override
	public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "stop");
		if (player != null && player.isPlaying()) {
			player.stop();
			state = STATE_STOPED;
		}
	}

	@Override
	public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
		Log.w(TAG, "play");

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (player != null)
						synchronized (player) {
							if (player != null) {
								player.stop();
								player.release();
							}
							if (url != null) {
								player = new MediaPlayer();
								player.reset();
								player.setDataSource(url);
								player.prepare();
								player.start();
								Log.d(TAG, "Stream duration:" + String.valueOf(player.getDuration()));
							}
						}
					else {
						player = new MediaPlayer();
						player.reset();
						player.setDataSource(url);
						player.prepare();
						player.start();
					}
					state = STATE_PLAYING;

				} catch (Exception ex) {
					state = STATE_STOPED;
				}
			}
		}).start();

	}

	@Override
	public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "pause");
		if (player != null && player.isPlaying()) {
			player.pause();
			state = STATE_PAUSE;
		}
	}

	@Override
	public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "record");

	}

	@Override
	public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
		Log.w(TAG, "seek");
		Log.i(TAG, "seek " + unit + " " + target);
		try {
			String[] split = target.split(":");
			int h = Integer.parseInt(split[0]);
			int m = Integer.parseInt(split[1]);
			int s = Integer.parseInt(split[2]);
			int seekTarget = (h * 3600 + m * 60 + s) * 1000;
			player.seekTo(seekTarget);
		} catch (Exception ex) {

		}
	}

	@Override
	public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "next");

	}

	@Override
	public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "previous");

	}

	@Override
	public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
		Log.w(TAG, "setPlayMode");

	}

	@Override
	public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
		Log.w(TAG, "setRecordQualityMode");

	}

	@Override
	public String getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		Log.w(TAG, "getCurrentTransportActions");
		return null;
	}

}
