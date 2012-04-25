package com.app.dlna.dmc.gui.customview.playlist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;
import com.app.dlna.dmc.processor.async.ProgressDialogAsyncTask;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.Playlist;
import com.app.dlna.dmc.processor.playlist.PlaylistManager;

public class PlaylistToolbar extends LinearLayout {

	private ImageView m_btn_back;
	private ImageView m_btn_save;
	private ImageView m_btn_clear;
	private ImageView m_btn_remove;
	private PlaylistView m_playlistView;

	public PlaylistToolbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_playlist, this);
		m_btn_back = (ImageView) findViewById(R.id.btn_back);
		m_btn_back.setOnClickListener(m_backClick);
		m_btn_save = (ImageView) findViewById(R.id.btn_save);
		m_btn_save.setOnClickListener(m_saveClick);
		m_btn_clear = (ImageView) findViewById(R.id.btn_clear);
		m_btn_clear.setOnClickListener(m_clearClick);
		m_btn_remove = (ImageView) findViewById(R.id.btn_remove);
		m_btn_remove.setOnClickListener(m_removeClick);
	}

	public void setLocalNetworkView(HomeNetworkView localNetworkView) {
	}

	public void setBackButtonEnabled(boolean enabled) {
		m_btn_back.setEnabled(enabled);
	}

	public void setPlaylistView(PlaylistView playlistView) {
		m_playlistView = playlistView;
	}

	private OnClickListener m_backClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			m_playlistView.backToListPlaylist();
		}
	};

	private OnClickListener m_saveClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getData().getId() == 1)
				createSaveDialog();
		}
	};
	private OnClickListener m_clearClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			new ProgressDialogAsyncTask<Void, Void, Void>("Clear Playlist") {

				@Override
				protected Void doInBackground(Void... params) {
					PlaylistManager.clearPlaylist(MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getData().getId());
					return null;
				}

				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					m_playlistView.backToListPlaylist();
				};

			}.execute(new Void[] {});
		}
	};
	private OnClickListener m_removeClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PlaylistProcessor processor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			if (processor.getData().getId() != -1) {
				confirmDelete();
			}
		}
	};

	private void createSaveDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

		alert.setTitle("Save Playlist");
		alert.setMessage("Insert playlist name:");

		final EditText input = new EditText(getContext());
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value != null && value.trim().length() != 0) {
					final Playlist playlist = new Playlist();
					playlist.setName(value);

					new ProgressDialogAsyncTask<Void, Void, Void>("Create Playlist") {

						@Override
						protected Void doInBackground(Void... params) {
							PlaylistManager.createPlaylist(playlist);
							return null;
						}

						protected void onPostExecute(Void result) {
							super.onPostExecute(result);
							m_playlistView.backToListPlaylist();
						};
					};

				}
			}
		});

		alert.setNegativeButton("Cancel", null);

		alert.show();
	}

	private void confirmDelete() {
		final PlaylistProcessor processor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
		alert.setTitle("Confirm Delete");
		alert.setMessage("Are you sure to delete \"" + processor.getData().getName() + "\"?");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				new ProgressDialogAsyncTask<Void, Void, Boolean>("Delete Playlist") {

					@Override
					protected Boolean doInBackground(Void... params) {
						return PlaylistManager.deletePlaylist(processor);
					}

					@Override
					protected void onPostExecute(Boolean result) {
						super.onPostExecute(result);
						String resultText = "";
						if (result) {
							resultText = "Delete playlist success";
						} else {
							resultText = "Delete playlist failed, try again later";
						}
						Toast.makeText(getContext(), resultText, Toast.LENGTH_SHORT).show();
						m_playlistView.backToListPlaylist();
					}

				};

			}
		});

		alert.setNegativeButton("Canel", null);

		alert.show();
	}

	public void updateToolbar(int state) {
		switch (state) {
		case PlaylistView.VM_LIST:
			this.setVisibility(View.GONE);
			break;
		case PlaylistView.VM_DETAILS:
			this.setVisibility(View.VISIBLE);
			PlaylistProcessor processor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
			if (processor.getData().getId() == 1) {
				// Unsaved playlist
				m_btn_remove.setVisibility(View.GONE);
				m_btn_save.setVisibility(View.VISIBLE);
			} else {
				m_btn_remove.setVisibility(View.VISIBLE);
				m_btn_save.setVisibility(View.GONE);
			}
			break;
		}
	}
}
