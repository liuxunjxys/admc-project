package com.app.dlna.dmc.gui.customview.playlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;
import com.app.dlna.dmc.gui.subactivity.LibraryActivity;

public class PlaylistToolbar extends LinearLayout {

	private ImageView m_btn_back;
	private PlaylistView m_playlistView;

	public PlaylistToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_playlist_toolbar, this);
		m_btn_back = ((ImageView) findViewById(R.id.btn_back));
		m_btn_back.setEnabled(false);

		((ImageView) findViewById(R.id.btn_showhide)).setOnClickListener(onShowHideClick);
	}

	public void setLocalNetworkView(HomeNetworkView localNetworkView) {
	}

	public void setBackButtonEnabled(boolean enabled) {
		m_btn_back.setEnabled(enabled);
	}

	private OnClickListener onShowHideClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getContext() instanceof LibraryActivity) {
				LibraryActivity activity = (LibraryActivity) v.getContext();
				if (activity.isCompactRendererShowing()) {
					activity.hideRendererCompactView();
					((ImageView) v).setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_navigate_up));
				} else {
					activity.showRendererCompactView();
					((ImageView) v).setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_navigate_down));
				}
			}
		}
	};

	public void setPlaylistView(PlaylistView playlistView) {
		m_playlistView = playlistView;
	}
	
}
