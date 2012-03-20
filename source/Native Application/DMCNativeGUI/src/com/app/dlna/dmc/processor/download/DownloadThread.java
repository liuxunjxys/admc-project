package com.app.dlna.dmc.processor.download;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.teleal.cling.support.model.DIDLObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.app.dlna.dmc.nativeui.R;

public class DownloadThread extends Thread {
	private static final String TAG = DownloadThread.class.getName();
	private DIDLObject m_item;
	private boolean m_isRunning;
	private File m_parent;
	private DownloadListener m_listener;
	private int m_downloadID;
	private Context m_context;
	private NotificationManager m_notificationManager;
	private Notification m_notification;

	public DownloadThread(DIDLObject item, File parrent, DownloadListener listener, int downloadID, Context context) {
		m_item = item;
		m_parent = parrent;
		m_listener = listener;
		m_downloadID = downloadID;
		m_context = context;
		m_notificationManager = (NotificationManager) m_context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public void startDownload() {
		m_isRunning = true;
		m_notification = new Notification(R.drawable.ic_download, "Downloading content", System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(m_context, 0, new Intent(), 0);
		m_notification.setLatestEventInfo(m_context, "Download content", "Downloading content", contentIntent);
		m_notification.flags = Notification.FLAG_NO_CLEAR;
		RemoteViews contentView = new RemoteViews(m_context.getPackageName(), R.layout.download_notification);
		contentView.setTextViewText(R.id.contentName, m_item.getTitle());
		m_notification.contentView = contentView;
		contentView.setProgressBar(R.id.downloadProgress, 0, 0, false);
		m_notificationManager.notify(m_downloadID, m_notification);
		this.start();
	}

	public void stopDownload() {
		m_isRunning = false;
		this.interrupt();
		m_notificationManager.cancel(m_downloadID);
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
			long size = 0;
			long second = System.currentTimeMillis();
			long maxsize = m_item.getResources().get(0).getSize();
			Log.d(TAG, "second = " + second);
			while ((read = dis.read(buffer)) > 0 && m_isRunning) {
				size += read;
				dos.write(buffer, 0, read);
				dos.flush();
				long newSecond = System.currentTimeMillis();
				Log.d(TAG, "new second = " + second);
				if (Math.abs(newSecond - second) > 1000) {
					int percent = (int) (size * 100 / maxsize);
					second = newSecond;
					m_notification.contentView.setProgressBar(R.id.downloadProgress, 100, percent, false);
					m_notification.contentView.setTextViewText(R.id.downloaded, size + "/" + maxsize);
					m_notificationManager.notify(m_downloadID, m_notification);
				}

			}
			if (size == maxsize) {
				Log.i(TAG, "Download complete, file = " + newFile.getAbsolutePath() + ", size = " + size);
				m_listener.onDownloadComplete(this);
			}
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
		m_notificationManager.cancel(m_downloadID);

	}

	public interface DownloadListener {
		public void onDownloadComplete(DownloadThread downloadThread);

		public void onDownloadFail(DownloadThread downloadThread, Exception ex);
	}
}
