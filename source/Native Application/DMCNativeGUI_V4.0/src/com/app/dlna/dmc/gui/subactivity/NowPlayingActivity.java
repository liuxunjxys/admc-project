package com.app.dlna.dmc.gui.subactivity;

import com.app.dlna.dmc.gui.customview.nowplaying.GaleryViewAdapter;
import com.app.dlna.dmc.gui.customview.nowplaying.RendererControlView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Gallery;
import app.dlna.controller.v4.R;

public class NowPlayingActivity extends Activity {

	private RendererControlView m_rendererControl;
	private Gallery m_galery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nowplaying);

		m_rendererControl = (RendererControlView) findViewById(R.id.rendererControlView);
		m_galery = (Gallery) findViewById(R.id.galery);
		m_galery.setAdapter(new GaleryViewAdapter(NowPlayingActivity.this));
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_rendererControl.connectToDMR();
		((GaleryViewAdapter)m_galery.getAdapter()).notifyDataSetChanged();
	}
}
