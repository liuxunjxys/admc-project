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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
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
import com.app.dlna.dmc.processor.http.HttpThread;

public class BrowseLocalActivity extends Activity {

	private static final String TAG = BrowseLocalActivity.class.getName();
	private static final int VM_MUSIC = 0;
	private static final int VM_VIDEO = 1;
	private static final int VM_PHOTO = 2;

	private int m_viewMode = VM_MUSIC;

	private ItemDisplayArrayAdapter m_adapter = null;
	private ListView m_listView = null;
	private List<ItemDisplay> m_listMusic = null;
	private List<ItemDisplay> m_listVideo = null;
	private List<ItemDisplay> m_listPhoto = null;

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
			DIDLObject object = m_adapter.getItem(position).getItem();
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

		if (!checkSDCard()) {
			new AlertDialog.Builder(BrowseLocalActivity.this).setMessage("Sdcard is busy").setTitle("Error")
					.setOnCancelListener(new OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							BrowseLocalActivity.this.finish();
						}
					}).setPositiveButton("Back", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							BrowseLocalActivity.this.finish();
						}
					}).create().show();
		}

		m_wifiManager = (WifiManager) this.getSystemService(WIFI_SERVICE);
		m_ip = intToIp(m_wifiManager.getDhcpInfo().ipAddress);

		m_listMusic = new ArrayList<ItemDisplay>();
		m_listVideo = new ArrayList<ItemDisplay>();
		m_listPhoto = new ArrayList<ItemDisplay>();

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

		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new ItemDisplayArrayAdapter(BrowseLocalActivity.this, 0);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(m_itemClickListener);

		m_scaningThread.start();
		m_httpThread = new HttpThread(m_port);
		m_httpThread.start();
	}

	private boolean checkSDCard() {
		Log.d(TAG, "check sd card");
		try {
			return new File("/mnt/sdcard/").canRead() == true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.d(TAG, "check fail");
			return false;
		}
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
						if (subFile.getAbsolutePath().endsWith("mkv")) {
							Log.e(TAG, mimeType.toString());
						}
						List<Res> res = new ArrayList<Res>();
						res.add(new Res(new MimeType(mimeType.split("/")[0], mimeType.split("/")[1]), subFile.length(),
								createLink(subFile)));
						object.setResources(res);
					}
					if (fileExtension != null) {
						if (m_musicMap.contains(fileExtension)) {
							ItemDisplay itemdisplay = new ItemDisplay(object, R.drawable.file_music);
							BrowseLocalActivity.this.addItem(itemdisplay, VM_MUSIC);
							m_listMusic.add(itemdisplay);
						}
						if (m_photoMap.contains(fileExtension)) {
							ItemDisplay itemdisplay = new ItemDisplay(object, R.drawable.file_picture);
							BrowseLocalActivity.this.addItem(itemdisplay, VM_PHOTO);
							m_listPhoto.add(itemdisplay);
						}
						if (m_videoMap.contains(fileExtension)) {
							ItemDisplay itemdisplay = new ItemDisplay(object, R.drawable.file_movie);
							BrowseLocalActivity.this.addItem(itemdisplay, VM_VIDEO);
							m_listVideo.add(itemdisplay);
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
			for (ItemDisplay item : m_listMusic) {
				addItem(item, VM_MUSIC);
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
			for (ItemDisplay item : m_listVideo) {
				addItem(item, VM_VIDEO);
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
			for (ItemDisplay item : m_listPhoto) {
				addItem(item, VM_PHOTO);
			}
		}
	}

	private void addItem(final ItemDisplay item, int type) {
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
