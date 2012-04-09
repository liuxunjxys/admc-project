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
	private static final String ACTION_NEXT = "next";
	private static final String ACTION_PREV = "prev";
	private static final String ACTION_PLAY = "play";
	private static final String ACTION_PAUSE = "pause";
	private static final String ACTION_STOP = "stop";
	private static final String ACTION_SET_VOLUME = "setVolume";
	private static final String ACTION_SEEK = "seek";

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
		} else if (ACTION_NEXT.equals(action)) {
			doNext();
		} else if (ACTION_PREV.equals(action)) {
			doPrev();
		} else if (ACTION_PLAY.equals(action)) {
			doPlay();
		} else if (ACTION_PAUSE.equals(action)) {
			doPause();
		} else if (ACTION_STOP.equals(action)) {
			doStop();
		} else if (ACTION_SEEK.equals(action)) {
			doSeek();
		} else if (ACTION_SET_VOLUME.equals(action)) {
			doSetVolume();
		}

		return null;
	}

	private void doSetVolume() {
		Log.i(TAG, "SetVolume");
		
	}

	private void doSeek() {
		Log.i(TAG, "Seek");

	}

	private void doStop() {
		Log.i(TAG, "Stop");
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor != null) {
			dmrProcessor.stop();
		}
	}

	private void doPause() {
		Log.i(TAG, "Pause");
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor != null) {
			dmrProcessor.pause();
		}
	}

	private void doPlay() {
		Log.i(TAG, "Play");
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (dmrProcessor != null) {
			dmrProcessor.play();
		}
	}

	private void doPrev() {
		Log.i(TAG, "Prev");
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (playlistProcessor != null && dmrProcessor != null) {
			playlistProcessor.previous();
			dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem().getUri());
		}
	}

	private void doNext() {
		Log.i(TAG, "Next");
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		if (playlistProcessor != null && dmrProcessor != null) {
			playlistProcessor.next();
			dmrProcessor.setURIandPlay(playlistProcessor.getCurrentItem().getUri());
		}
	}

	private void playItem(int position) {
		PlaylistItem item = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().getAllItems().get(position);
		DMRProcessor dmrProcessor = MainActivity.UPNP_PROCESSOR.getDMRProcessor();
		String url = item.getUri();
		if (dmrProcessor == null) {
			Log.e(TAG, "DMR Processor is null");
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
