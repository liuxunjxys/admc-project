package com.app.dlna.dmc.processor.impl;

import java.io.File;

import org.teleal.cling.support.model.DIDLObject;

import android.app.Activity;
import android.widget.Toast;

import com.app.dlna.dmc.processor.download.DownloadThread;
import com.app.dlna.dmc.processor.download.DownloadThread.DownloadListener;
import com.app.dlna.dmc.processor.interfaces.DownloadProcessor;

public class DownloadProcessorImpl implements DownloadProcessor {

	private Activity m_activity;
	private File m_sdRoot;

	public DownloadProcessorImpl(Activity activity) {
		m_activity = activity;
		// TODO: must change app_name here
		m_sdRoot = new File("/mnt/sdcard/DMCProject");
		m_sdRoot.mkdir();
	}

	@Override
	public int startDownload(DIDLObject item) {
		DownloadThread downloadThread = new DownloadThread(item, m_sdRoot, new DownloadListener() {

			@Override
			public void onDownloadFail(final DownloadThread downloadThread, final Exception ex) {
				m_activity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(m_activity,
								"Download failed, file: " + downloadThread.getItem().getTitle() + ", error = " + ex.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onDownloadComplete(final DownloadThread downloadThread) {

				m_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(m_activity, "Download complete, file: " + downloadThread.getItem().getTitle(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});

		downloadThread.startDownload();
		return 0;
	}

	@Override
	public void stopDownload(int id) {

	}

	@Override
	public void stopAllDownloads() {

	}

}
