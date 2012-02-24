package com.app.dlna.dmc.gui.youtube;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.processor.http.HTTPServerData;
import com.app.dlna.dmc.processor.impl.UpnpProcessorImpl;
import com.app.dlna.dmc.processor.impl.YoutubeProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor.IYoutubeProcessorListener;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class YoutubeActivity extends UpnpListenerActivity {
	private static final String TAG = YoutubeActivity.class.getName();

	private CheckBox m_cb_enable_proxy;
	private EditText m_ed_link;
	private YoutubeProcessor m_youtubeProcessor;
	private ProgressDialog m_progressDialog;
	private PlaylistProcessor m_playlistProcessor;
	private UpnpProcessor m_upnpProcessor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ytcontent_activitiy);
		m_cb_enable_proxy = (CheckBox) findViewById(R.id.enable_proxy_mode);
		m_ed_link = (EditText) findViewById(R.id.youtube_link);

		m_upnpProcessor = new UpnpProcessorImpl(YoutubeActivity.this);
		m_upnpProcessor.bindUpnpService();

		m_youtubeProcessor = new YoutubeProcessorImpl();
		m_progressDialog = new ProgressDialog(YoutubeActivity.this);
		m_progressDialog.setMessage("Contact to Youtube server");
		m_progressDialog.setTitle("Loading...");
		m_progressDialog.setCancelable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_playlistProcessor = m_upnpProcessor.getPlaylistProcessor();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		m_upnpProcessor.unbindUpnpService();
	}

	private void executeNoProxy(final String link) {
		try {
			m_youtubeProcessor.getDirectLink(link, new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgressDialog();
				}

				@Override
				public void onComplete(String result) {
					dissmissProgressDialog();
					switchToDMRList(link, result);
				}

				@Override
				public void onFail(Exception ex) {
					dissmissProgressDialog();
				}

			});
		} catch (Exception ex) {
			Toast.makeText(YoutubeActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void dissmissProgressDialog() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_progressDialog.dismiss();
			}
		});
	}

	protected void showProgressDialog() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_progressDialog.show();
			}
		});
	}

	private void switchToDMRList(final String link, final String directlink) {
		// Intent intent = new Intent(YoutubeActivity.this, DMRListActivity.class);
		// intent.putExtra("URL", directlink);
		// intent.putExtra("Title", link);
		// YoutubeActivity.this.startActivity(intent);
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_playlistProcessor != null) {
					PlaylistItem item = new PlaylistItem();
					item.setTitle(link);
					item.setUrl(directlink);
					item.setType(Type.VIDEO);
					if (m_playlistProcessor.addItem(item))
						Toast.makeText(YoutubeActivity.this, "Add item \"" + item.getTitle() + "\" to playlist sucess", Toast.LENGTH_SHORT).show();
					else {
						if (m_playlistProcessor.isFull()) {
							Toast.makeText(YoutubeActivity.this, "Current playlist is full", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(YoutubeActivity.this, "Item already exits in current Playlist", Toast.LENGTH_SHORT).show();
						}
					}
				}
			}
		});

	}

	private void executeEnableProxy(final String link) {
		try {
			m_youtubeProcessor.registURL(link, new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgressDialog();

				}

				@Override
				public void onFail(Exception ex) {
					dissmissProgressDialog();
				}

				@Override
				public void onComplete(String result) {
					dissmissProgressDialog();
					String generatedURL;
					try {
						generatedURL = new URI("http", HTTPServerData.HOST + ":" + HTTPServerData.PORT, result, null, null).toString();
						Log.i(TAG, "Generated URL = " + generatedURL);
						switchToDMRList(link, generatedURL);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onSubmitClick(View view) {
		String link = m_ed_link.getText().toString();
		if (m_cb_enable_proxy.isChecked()) {
			executeEnableProxy(link);
		} else {
			executeNoProxy(link);
		}
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		m_playlistProcessor = m_upnpProcessor.getPlaylistProcessor();
	}
}
