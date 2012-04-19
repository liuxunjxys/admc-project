package com.app.dlna.dmc.gui.customview.playlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;

public class PlaylistToolbar extends LinearLayout {

	private ImageView m_btn_back;
	private PlaylistView m_playlistView;

	public PlaylistToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_playlist, this);
		m_btn_back = ((ImageView) findViewById(R.id.btn_back));
		m_btn_back.setOnClickListener(m_backClick);
	}

	public void setLocalNetworkView(HomeNetworkView localNetworkView) {
	}

	public void setBackButtonEnabled(boolean enabled) {
		m_btn_back.setEnabled(enabled);
	}

	public void setPlaylistView(PlaylistView playlistView) {
		m_playlistView = playlistView;
	}

	public void enableBack() {
		m_btn_back.setEnabled(true);
	}

	public void disableBack() {
		m_btn_back.setEnabled(false);
	}

	private OnClickListener m_backClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			m_playlistView.backToListPlaylist();
		}
	};

}
