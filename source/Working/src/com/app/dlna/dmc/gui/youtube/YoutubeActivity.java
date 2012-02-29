package com.app.dlna.dmc.gui.youtube;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
	private EditText m_ed_keyword;
	private YoutubeProcessor m_youtubeProcessor;
	private ProgressDialog m_progressDialog;
	private PlaylistProcessor m_playlistProcessor;
	private UpnpProcessor m_upnpProcessor;
	private ListView m_listView;
	private YoutubeItemArrayAdapter m_adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ytcontent_activitiy);
		m_cb_enable_proxy = (CheckBox) findViewById(R.id.enable_proxy_mode);
		m_ed_keyword = (EditText) findViewById(R.id.youtube_link);

		m_upnpProcessor = new UpnpProcessorImpl(YoutubeActivity.this);
		m_upnpProcessor.bindUpnpService();

		m_youtubeProcessor = new YoutubeProcessorImpl();
		m_progressDialog = new ProgressDialog(YoutubeActivity.this);
		m_progressDialog.setMessage("Contact to Youtube server");
		m_progressDialog.setTitle("Loading...");
		m_progressDialog.setCancelable(true);

		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new YoutubeItemArrayAdapter(YoutubeActivity.this, 0);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(itemClickListener);
	}

	private OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			YoutubeItem item = m_adapter.getItem(position);
			String title = item.getTitle();
			String link = item.getUrl();
			if (m_cb_enable_proxy.isChecked()) {
				executeProxyMode(title, link);
			} else {
				executeNoProxy(title, link);
			}
		}
	};

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

	private void executeNoProxy(final String title, final String link) {
		try {
			m_youtubeProcessor.getDirectLink(link, new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgressDialog();
				}

				@Override
				public void onComplete(String result) {
					dissmissProgressDialog();
					insertPlaylistItem(title, link, result);
				}

				@Override
				public void onFail(Exception ex) {
					dissmissProgressDialog();
				}

				@Override
				public void onSearchComplete(List<YoutubeItem> result) {

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

	private void insertPlaylistItem(final String title, final String link, final String directlink) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (m_playlistProcessor != null) {
					PlaylistItem item = new PlaylistItem();
					item.setTitle(title);
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

	private void executeProxyMode(final String title, final String link) {
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
						insertPlaylistItem(title, link, generatedURL);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

				}

				@Override
				public void onSearchComplete(List<YoutubeItem> result) {

				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onSubmitClick(View view) {

		if (m_ed_keyword.getText() != null && !m_ed_keyword.getText().toString().trim().isEmpty())
			m_youtubeProcessor.executeQuery(m_ed_keyword.getText().toString().trim(), new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgressDialog();
				}

				@Override
				public void onSearchComplete(final List<YoutubeItem> result) {
					dissmissProgressDialog();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							synchronized (m_adapter) {
								m_adapter.clear();
								for (YoutubeItem item : result) {
									m_adapter.add(item);
								}
							}
						}
					});
				}

				@Override
				public void onFail(Exception ex) {
					dissmissProgressDialog();
				}

				@Override
				public void onComplete(String result) {
					dissmissProgressDialog();
				}
			});
	}

	@Override
	public void onStartComplete() {
		super.onStartComplete();
		m_playlistProcessor = m_upnpProcessor.getPlaylistProcessor();
	}
}
