package com.app.dlna.dmc.processor.localdevice.service;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.StorageFolder;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;
import org.teleal.common.util.MimeType;

import android.os.Environment;
import android.util.Log;

import com.app.dlna.dmc.processor.http.HTTPServerData;
import com.app.dlna.dmc.utility.Utility;

public class LocalContentDirectoryService extends AbstractContentDirectoryService {
	private static final String TAG = LocalContentDirectoryService.class.getName();
	private static List<MusicTrack> m_listMusic = null;
	private static List<VideoItem> m_listVideo = null;
	private static List<ImageItem> m_listPhoto = null;
	private static List<String> m_musicMap = null;
	private static List<String> m_videoMap = null;
	private static List<String> m_photoMap = null;

	public static void scanMedia() {
		m_listMusic = new ArrayList<MusicTrack>();
		m_listVideo = new ArrayList<VideoItem>();
		m_listPhoto = new ArrayList<ImageItem>();
		m_musicMap = new ArrayList<String>();
		m_videoMap = new ArrayList<String>();
		m_photoMap = new ArrayList<String>();

		m_musicMap.add(".mp3");
		m_musicMap.add(".wma");
		m_musicMap.add(".wav");
		m_musicMap.add(".mid");
		m_musicMap.add(".midi");

		m_videoMap.add(".mp4");
		m_videoMap.add(".flv");
		m_videoMap.add(".mpg");
		m_videoMap.add(".avi");
		m_videoMap.add(".mkv");
		m_videoMap.add(".m4v");

		m_photoMap.add(".jpg");
		m_photoMap.add(".jpeg");
		m_photoMap.add(".png");
		m_photoMap.add(".bmp");
		m_photoMap.add(".gif");
		Log.e(TAG, "Start media scanning");
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (HTTPServerData.HOST == null) {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					scanFile(Environment.getExternalStorageDirectory().getAbsolutePath());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

	// private Thread m_scaningThread = new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// try {
	// scanFile(Environment.getExternalStorageDirectory().getAbsolutePath());
	// } catch (Exception ex) {
	// ex.printStackTrace();
	// }
	// }
	// });

	private static void scanFile(String path) {
		try {
			File file = new File(path);
			for (File subFile : file.listFiles()) {
				if (subFile.isDirectory()) {
					// Log.i(TAG, "DIR = " + subFile.getAbsolutePath());
					scanFile(subFile.getAbsolutePath());
				} else if (subFile.length() >= 51200) {
					// Log.i(TAG, "FILE = " + subFile.getAbsolutePath());
					String fileName = subFile.getName();
					String mimeType = URLConnection.getFileNameMap().getContentTypeFor(subFile.getName());
					int dotPos = subFile.getName().lastIndexOf(".");
					String fileExtension = dotPos != -1 ? fileName.substring(dotPos) : null;
					if (mimeType != null) {
						Res res = new Res(new MimeType(mimeType.split("/")[0], mimeType.split("/")[1]), subFile.length(), Utility.createLink(subFile));
						if (fileExtension != null) {
							if (m_musicMap.contains(fileExtension)) {
								MusicTrack musicTrack = new MusicTrack("0/1/" + subFile.getName(), "0/1", subFile.getName(), "local dms", "", "", res);
								m_listMusic.add(musicTrack);
							}

							if (m_videoMap.contains(fileExtension)) {
								VideoItem videoItem = new VideoItem("0/2/" + subFile.getName(), "0/2", subFile.getName(), "local dms", res);
								m_listVideo.add(new VideoItem(videoItem));
							}
							if (m_photoMap.contains(fileExtension)) {
								ImageItem imageItem = new ImageItem("0/3/" + subFile.getName(), "0/3", subFile.getName(), "local dms", res);
								m_listPhoto.add(new ImageItem(imageItem));
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public BrowseResult browse(String objectID, BrowseFlag browseFlag, String filter, long firstResult, long maxResults, SortCriterion[] orderby)
			throws ContentDirectoryException {
		Log.d(TAG, "Browse " + objectID);
		BrowseResult br = null;
		int count = 0;
		try {
			final DIDLContent content = new DIDLContent();
			if (objectID.equals("0")) {
				content.addContainer(new StorageFolder("0/1", "0", "Music", "Android Local DMS", m_listMusic.size(), 0l));
				content.addContainer(new StorageFolder("0/2", "0", "Video", "Android Local DMS", m_listVideo.size(), 0l));
				content.addContainer(new StorageFolder("0/3", "0", "Photo", "Android Local DMS", m_listPhoto.size(), 0l));
				count = 3;
			}

			if (objectID.equals("0/1"))
				synchronized (m_listMusic) {
					for (DIDLObject didlObject : m_listMusic) {
						if (didlObject != null) {
							content.addItem((Item) didlObject);
						}
					}
					count = m_listMusic.size();
				}
			if (objectID.equals("0/2"))
				synchronized (m_listVideo) {
					for (DIDLObject didlObject : m_listVideo) {
						if (didlObject != null) {
							content.addItem((Item) didlObject);
						}
					}
					count = m_listVideo.size();
				}
			if (objectID.equals("0/3"))
				synchronized (m_listPhoto) {
					for (DIDLObject didlObject : m_listPhoto) {
						if (didlObject != null) {
							content.addItem((Item) didlObject);
						}
					}
					count = m_listPhoto.size();
				}
			DIDLParser parser = new DIDLParser();
			br = new BrowseResult(parser.generate(content), count, count);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return br;
	}

}
