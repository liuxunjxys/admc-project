package com.app.dlna.dmc.processor.download;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.teleal.cling.support.model.DIDLObject;

import android.util.Log;

public class DownloadThread extends Thread {
	private static final String TAG = DownloadThread.class.getName();
	private DIDLObject m_item;
	private boolean m_isRunning;
	private File m_parent;
	private DownloadListener m_listener;

	public DownloadThread(DIDLObject item, File parrent, DownloadListener listener) {
		m_item = item;
		m_parent = parrent;
		m_listener = listener;
	}

	public void startDownload() {
		m_isRunning = true;
		this.start();
	}

	public void stopDownload() {
		m_isRunning = false;
		this.interrupt();
	}

	public DIDLObject getItem() {
		return m_item;
	}

	@Override
	public void run() {
		super.run();
		Log.i(TAG, "Download item " + m_item.getTitle());
		DataOutputStream dos = null;
		DataInputStream dis = null;
		HttpURLConnection connection = null;
		try {
			URL u = new URL(m_item.getResources().get(0).getValue());
			connection = (HttpURLConnection) u.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.connect();
			String contentType = "";
			String filename = m_item.getTitle();
			Log.i(TAG, "Content type = " + (contentType = connection.getContentType()));
			if (contentType != null) {
				if (contentType.equals("audio/mpeg")) {
					filename += ".mp3";
				}
			}
			File newFile = new File(m_parent, filename);
			dos = new DataOutputStream(new FileOutputStream(newFile));
			dis = new DataInputStream(connection.getInputStream());
			byte[] buffer = new byte[204800];
			int read = 0;
			int size = 0;
			while ((read = dis.read(buffer)) > 0 && m_isRunning) {
				size += read;
				dos.write(buffer, 0, read);
				dos.flush();
			}
			Log.i(TAG, "Download complete, file = " + newFile.getAbsolutePath() + ", size = " + size);
			m_listener.onDownloadComplete(this);
		} catch (Exception ex) {
			ex.printStackTrace();
			if (m_listener != null)
				m_listener.onDownloadFail(this, ex);
		} finally {
			if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (dis != null)
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (connection != null)
				connection.disconnect();
		}

	}

	public interface DownloadListener {
		public void onDownloadComplete(DownloadThread downloadThread);

		public void onDownloadFail(DownloadThread downloadThread, Exception ex);
	}
}
