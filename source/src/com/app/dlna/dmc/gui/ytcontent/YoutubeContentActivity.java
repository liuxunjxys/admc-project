package com.app.dlna.dmc.gui.ytcontent;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.abstractactivity.UpnpListenerActivity;
import com.app.dlna.dmc.gui.devices.DMRListActivity;
import com.app.dlna.dmc.gui.ytcontent.YoutubeProcessor.IYoutubeProcessorListener;
import com.app.dlna.dmc.processor.http.HTTPServerData;

public class YoutubeContentActivity extends UpnpListenerActivity {

	private static final String TAG = YoutubeContentActivity.class.getName();
	private CheckBox m_cb_enable_proxy;
	private EditText m_ed_link;
	private YoutubeProcessor m_youtubeProcessor;
	private ProgressDialog m_progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ytcontent_activitiy);
		m_cb_enable_proxy = (CheckBox) findViewById(R.id.enable_proxy_mode);
		m_ed_link = (EditText) findViewById(R.id.youtube_link);
		m_youtubeProcessor = new YoutubeProcessorImpl();
		m_progressDialog = new ProgressDialog(YoutubeContentActivity.this);
		m_progressDialog.setMessage("Contact to Youtube server");
		m_progressDialog.setTitle("Loading...");
		m_progressDialog.setCancelable(false);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
			Toast.makeText(YoutubeContentActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
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

	private void switchToDMRList(String link, String directlink) {
		Intent intent = new Intent(YoutubeContentActivity.this, DMRListActivity.class);
		intent.putExtra("URL", directlink);
		intent.putExtra("Title", link);
		YoutubeContentActivity.this.startActivity(intent);
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
						generatedURL = new URI("http", HTTPServerData.HOST + ":" + HTTPServerData.PORT, result, null, null)
								.toString();
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

}
