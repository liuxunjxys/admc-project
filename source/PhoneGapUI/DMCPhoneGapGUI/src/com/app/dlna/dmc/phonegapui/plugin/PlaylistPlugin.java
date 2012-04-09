package com.app.dlna.dmc.phonegapui.plugin;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;

import com.app.dlna.dmc.phonegapui.MainActivity;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class PlaylistPlugin extends Plugin {
	private static final String ACTION_LOAD_PLAYLIST = "loadPlaylist";
	private static final String ACTION_ITEM_CLICK = "itemClick";
	private static final String TAG = PlaylistPlugin.class.getName();

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {
		if (ACTION_LOAD_PLAYLIST.equals(action)) {
			// call load Playlist
			Log.i(TAG, "call load playlist");
			loadPlaylist();
		} else if (ACTION_ITEM_CLICK.equals(action)) {
			try {
				Log.i(TAG, "item click idx = " + data.getInt(0));
				playItem(data.getInt(0));
			} catch (JSONException e) {
				e.printStackTrace();
				return new PluginResult(Status.JSON_EXCEPTION);
			}
		}
		return null;
	}

	private void playItem(int position) {
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems().get(position);
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		String url = item.getUri();
		if (dmrProcessor == null) {
			// Toast.makeText(PlaylistActivity.this, "Cannot get DMRProcessor",
			// Toast.LENGTH_SHORT).show();
			new AlertDialog.Builder(MainActivity.INSTANCE).setTitle("Error")
					.setMessage("Cannot get DMRProcessor. Please select another one")
					.setPositiveButton("Ok", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.INSTANCE.finish();
						}
					}).create().show();
		} else {
			dmrProcessor.setURIandPlay(url);
			MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().setCurrentItem(item);
			validateListView(item);
		}
	}

	private void validateListView(PlaylistItem item) {

	}

	public void loadPlaylist() {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		sendJavascript("clearPlaylist();");
		if (playlistProcessor != null) {
			JSONArray array = new JSONArray();
			List<PlaylistItem> items = playlistProcessor.getAllItems();
			for (int i = 0; i < items.size(); ++i) {
				array.put(getJSONFromPlaylistItem(items.get(i), i));
			}
			sendJavascript("loadPlaylistItems('" + array.toString().replace("'", "\\'") + "');");
		}
		sendJavascript("hideLoadingIcon();");
	}

	private JSONObject getJSONFromPlaylistItem(PlaylistItem item, int idx) {
		JSONObject result = new JSONObject();
		try {
			result.put("name", item.getTitle().trim().replace("\"", "\\\""));
			result.put("idx", idx);
			switch (item.getType()) {
			case AUDIO:
				result.put("icon", "img/ic_didlobject_audio.png");
				break;
			case VIDEO:
				result.put("icon", "img/ic_didlobject_video.png");
				break;
			case IMAGE:
				result.put("icon", "img/ic_didlobject_image.png");
				break;
			default:
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
}
