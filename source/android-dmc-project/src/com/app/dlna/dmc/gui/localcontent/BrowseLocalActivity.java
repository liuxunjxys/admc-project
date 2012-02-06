package com.app.dlna.dmc.gui.localcontent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.DIDLObject.Property.UPNP.ICON;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.Item;
import org.teleal.common.util.MimeType;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.devices.DMRListActivity;
import com.app.dlna.dmc.gui.dms.DIDLObjectArrayAdapter;
import com.app.dlna.dmc.processor.http.HttpThread;

public class BrowseLocalActivity extends Activity {

	private static final String TAG = BrowseLocalActivity.class.getName();
	private static final int VM_MUSIC = 0;
	private static final int VM_VIDEO = 1;
	private static final int VM_PHOTO = 2;

	private int m_viewMode = VM_MUSIC;

	private DIDLObjectArrayAdapter m_adapter = null;
	private ListView m_listView = null;
	private List<Item> m_listMusic = null;
	private List<Item> m_listVideo = null;
	private List<Item> m_listPhoto = null;

	private List<String> m_musicMap = null;
	private List<String> m_videoMap = null;
	private List<String> m_photoMap = null;
	private String m_ip = null;
	private int m_port = 9777;

	private Thread m_scaningThread = new Thread(new Runnable() {

		@Override
		public void run() {
			try {
				scanFile(Environment.getExternalStorageDirectory().getAbsolutePath());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	});

	private HttpThread m_httpThread = null;

	private WifiManager m_wifiManager;

	private OnItemClickListener m_itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			DIDLObject object = m_adapter.getItem(position);
			String url = null;
			if (object.getResources() != null && object.getResources().get(0) != null) {
				url = object.getResources().get(0).getValue();
			}
			Log.e(TAG, url);
			Intent intent = new Intent(BrowseLocalActivity.this, DMRListActivity.class);
			intent.putExtra("URL", url);
			intent.putExtra("Title", object.getTitle());
			Bundle extraInfo = new Bundle();

			try {
				if (object.getFirstProperty(ICON.class) != null)
					extraInfo.putString("IconURL", object.getFirstProperty(ICON.class).getValue().toURL().toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}

			intent.putExtra("ExtraInfo", extraInfo);

			Log.e(TAG, "Item title = " + object.getTitle());
			BrowseLocalActivity.this.startActivity(intent);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browselocal_activity);

		m_wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
		m_ip = intToIp(m_wifiManager.getDhcpInfo().ipAddress);

		m_listMusic = new ArrayList<Item>();
		m_listVideo = new ArrayList<Item>();
		m_listPhoto = new ArrayList<Item>();

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

		m_photoMap.add(".jpg");
		m_photoMap.add(".jpeg");
		m_photoMap.add(".png");
		m_photoMap.add(".bmp");
		m_photoMap.add(".gif");

		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new DIDLObjectArrayAdapter(BrowseLocalActivity.this, 0, 0, new ArrayList<DIDLObject>());
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(m_itemClickListener);

		m_scaningThread.start();
		m_httpThread = new HttpThread(m_port);
		m_httpThread.start();
	}

	private void scanFile(String path) {
		try {
			File file = new File(path);
			for (File subFile : file.listFiles()) {
				if (subFile.isDirectory()) {
					scanFile(subFile.getAbsolutePath());
				} else {
					String fileName = subFile.getName();
					String mimeType = URLConnection.getFileNameMap().getContentTypeFor(subFile.getName());
					int dotPos = subFile.getName().lastIndexOf(".");
					String fileExtension = dotPos != -1 ? fileName.substring(dotPos) : null;
					Item object = new Item();
					object.setTitle(subFile.getName());
					if (mimeType != null) {
						List<Res> res = new ArrayList<Res>();
						res.add(new Res(new MimeType(mimeType.split("/")[0], mimeType.split("/")[1]), subFile.length(), createLink(subFile)));
						object.setResources(res);
					}
					if (fileExtension != null) {
						if (m_musicMap.contains(fileExtension)) {
							BrowseLocalActivity.this.addItem(object, VM_MUSIC);
							m_listMusic.add(object);
						}
						if (m_photoMap.contains(fileExtension)) {
							BrowseLocalActivity.this.addItem(object, VM_PHOTO);
							m_listPhoto.add(object);
						}
						if (m_videoMap.contains(fileExtension)) {
							BrowseLocalActivity.this.addItem(object, VM_VIDEO);
							m_listVideo.add(object);
						}
					}

				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String createLink(File file) {
		try {
			return new URI("http", m_ip + ":" + m_port, file.getAbsolutePath(), null, null).toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String intToIp(int i) {
		String result = "";
		result = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
		return result;
	}

	public void onMusicClick(View view) {
		m_viewMode = VM_MUSIC;
		refreshMusicList();
	}

	private void refreshMusicList() {
		m_adapter.clear();
		synchronized (m_listMusic) {
			for (Item item : m_listMusic) {
				addItem((Item) item, VM_MUSIC);
			}
		}
	}

	public void onVideoClick(View view) {
		m_viewMode = VM_VIDEO;
		refreshVideoList();
	}

	private void refreshVideoList() {
		m_adapter.clear();
		synchronized (m_listVideo) {
			for (Item item : m_listVideo) {
				addItem((Item) item, VM_VIDEO);
			}
		}
	}

	public void onPhotoClick(View view) {
		m_viewMode = VM_PHOTO;
		refreshPhotoList();
	}

	private void refreshPhotoList() {
		m_adapter.clear();
		synchronized (m_listPhoto) {
			for (Item item : m_listPhoto) {
				addItem((Item) item, VM_PHOTO);
			}
		}
	}

	private void addItem(final Item item, int type) {
		if (type == m_viewMode) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_adapter.add(item);
				}
			});
		}
	}

}
