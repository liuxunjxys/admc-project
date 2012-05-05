package com.app.dlna.dmc.gui.customview.nowplaying;

import android.media.MediaPlayer;
import android.view.SurfaceHolder;

public class LocalMediaPlayer extends MediaPlayer {

	private SurfaceHolder m_surfaceHolder;
	private int surface_width;
	private int surface_height;

	@Override
	public void setDisplay(SurfaceHolder sh) {
		super.setDisplay(sh);
		m_surfaceHolder = sh;
	}

	public SurfaceHolder getDisplay() {
		return m_surfaceHolder;
	}

	public void setSufaceDimension(int width, int height) {
		surface_width = width;
		surface_height = height;
		scaleContent();
	}

	public int getDisplayWidth() {
		return surface_width;
	}

	public int getDisplayHeight() {
		return surface_height;
	}

	public void scaleContent() {
		int video_width = getVideoWidth();
		int video_height = getVideoHeight();

		if (video_width != 0 && video_height != 0 && m_surfaceHolder != null) {
			float scale_width = (float) video_width / surface_width;
			float scale_height = (float) video_height / surface_height;
			float max_scale = scale_width > scale_height ? scale_width : scale_height;
			int target_width = (int) (video_width / max_scale);
			int target_height = (int) (video_height / max_scale);
			m_surfaceHolder.setFixedSize(target_width, target_height);
		}
	}
}
