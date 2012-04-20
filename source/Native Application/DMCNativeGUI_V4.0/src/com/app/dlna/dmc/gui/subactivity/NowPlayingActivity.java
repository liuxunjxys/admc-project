package com.app.dlna.dmc.gui.subactivity;

import com.app.dlna.dmc.gui.customview.nowplaying.RendererControlView;

import android.app.Activity;
import android.os.Bundle;
import app.dlna.controller.v4.R;

public class NowPlayingActivity extends Activity {

	private RendererControlView m_rendererControl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);

		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_rendererControl.connectToDMR();
	}
}
