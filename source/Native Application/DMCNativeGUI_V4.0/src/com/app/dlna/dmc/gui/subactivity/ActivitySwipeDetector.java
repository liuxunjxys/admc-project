package com.app.dlna.dmc.gui.subactivity;

import android.view.MotionEvent;
import android.view.View;

public class ActivitySwipeDetector implements View.OnTouchListener {
	private NowPlayingActivity m_activity;
	private static final int MIN_DISTANCE = 100;
	private float m_downX, m_upX;

	public ActivitySwipeDetector(NowPlayingActivity activity) {
		this.m_activity = activity;
	}

	public void onRightToLeftSwipe() {
		m_activity.loadNext();
	}

	public void onLeftToRightSwipe() {
		m_activity.loadPrev();
	}

	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			m_downX = event.getX();
			return true;
		}
		case MotionEvent.ACTION_UP: {
			m_upX = event.getX();

			float deltaX = m_downX - m_upX;

			if (Math.abs(deltaX) > MIN_DISTANCE) {
				if (deltaX < 0) {
					this.onLeftToRightSwipe();
					return true;
				}
				if (deltaX > 0) {
					this.onRightToLeftSwipe();
					return true;
				}
			} else {
				return false;
			}
			return true;
		}
		}
		return false;
	}
}