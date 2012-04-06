package com.app.dlna.dmc.phonegapui.plugin;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.app.dlna.dmc.phonegapui.MainActivity;
import com.app.dlna.dmc.processor.http.HTTPServerData;
import com.app.dlna.dmc.processor.impl.YoutubeProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor.IYoutubeProcessorListener;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;
import com.app.dlna.dmc.processor.youtube.YoutubeItem;
import com.phonegap.api.Plugin;
import com.phonegap.api.PluginResult;
import com.phonegap.api.PluginResult.Status;

public class YoutubePlugin extends Plugin {
	private static final String TAG = YoutubePlugin.class.getName();
	private static final String ACTION_QUERY = "query";
	private static final String ACTION_ADD_TO_PLAYLIST = "addToPlaylist";
	private List<YoutubeItem> m_resultList;

	@Override
	public PluginResult execute(String action, JSONArray data, String callbackId) {

		if (ACTION_ADD_TO_PLAYLIST.equals(action)) {
			// Add to playlist
			Log.i(TAG, "add to playlist");
			try {
				int idx = data.getInt(0);
				boolean proxy = data.getBoolean(1);
				if (m_resultList != null && idx < m_resultList.size() - 1) {
					YoutubeItem item = m_resultList.get(idx);
					String title = item.getTitle();
					String link = item.getUrl();
					if (proxy) {
						executeProxyMode(title, link);
					} else {
						executeNoProxy(title, link);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return new PluginResult(Status.JSON_EXCEPTION);
			}

		} else if (ACTION_QUERY.equals(action)) {
			// query
			Log.i(TAG, "query");
			try {
				String query = data.getString(0);

				new YoutubeProcessorImpl().executeQuery(query, new IYoutubeProcessorListener() {

					@Override
					public void onStartPorcess() {
						showProgress();
					}

					@Override
					public void onSearchComplete(List<YoutubeItem> result) {
						dismissProgress();
						JSONArray array = new JSONArray();
						m_resultList = result;
						for (int i = 0; i < result.size(); ++i) {
							array.put(createJsonFromYoutubeItem(result.get(i), i));
						}
						sendJavascript("showYoutubeResult('" + array.toString().replace("'", "\\'") + "');");
						Log.i(TAG, "resutl = " + array.toString());
					}

					@Override
					public void onFail(Exception ex) {
						dismissProgress();
					}

					@Override
					public void onGetDirectLinkComplete(String result) {
						dismissProgress();
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
				return new PluginResult(Status.JSON_EXCEPTION);
			}
		}

		return null;
	}

	private JSONObject createJsonFromYoutubeItem(YoutubeItem item, int idx) {
		JSONObject result = new JSONObject();
		try {
			result.put("idx", String.valueOf(idx));
			result.put("title", item.getTitle().trim().replace("\"", "\\\""));
			Log.i(TAG, "title = " + item.getTitle());
			result.put("thumb", item.getThumbnail());
			result.put("duration", item.getDuration());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void insertPlaylistItem(final String title, final String link, final String directlink) {
		PlaylistProcessor playlistProcessor = MainActivity.UPNP_PROCESSOR.getPlaylistProcessor();
		if (playlistProcessor != null) {
			final PlaylistItem item = new PlaylistItem();
			item.setTitle(title);
			item.setUrl(directlink);
			item.setType(Type.VIDEO);
			if (playlistProcessor.addItem(item))
				MainActivity.INSTANCE.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						sendJavascript("hideLoadingIcon();");
						Toast.makeText(MainActivity.INSTANCE,
								"Add item \"" + item.getTitle() + "\" to playlist sucess", Toast.LENGTH_SHORT).show();
					}
				});

			else {
				if (playlistProcessor.isFull()) {
					MainActivity.INSTANCE.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							sendJavascript("hideLoadingIcon();");
							Toast.makeText(MainActivity.INSTANCE, "Current playlist is full", Toast.LENGTH_SHORT)
									.show();
						}
					});
				} else {
					MainActivity.INSTANCE.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							sendJavascript("hideLoadingIcon();");
							Toast.makeText(MainActivity.INSTANCE, "Item already exits in current Playlist",
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}

	}

	private void executeProxyMode(final String title, final String link) {
		try {
			YoutubeProcessor m_youtubeProcessor = new YoutubeProcessorImpl();
			m_youtubeProcessor.registURL(link, new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgress();
				}

				@Override
				public void onFail(Exception ex) {
					dismissProgress();
					ex.printStackTrace();
				}

				@Override
				public void onSearchComplete(List<YoutubeItem> result) {
					dismissProgress();
				}

				@Override
				public void onGetDirectLinkComplete(String result) {
					dismissProgress();
					String generatedURL;
					try {
						generatedURL = new URI("http", HTTPServerData.HOST + ":" + HTTPServerData.PORT, result, null,
								null).toString();
						Log.i(TAG, "Generated URL = " + generatedURL);
						insertPlaylistItem(title, link, generatedURL);
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}

				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void executeNoProxy(final String title, final String link) {
		try {
			YoutubeProcessor m_youtubeProcessor = new YoutubeProcessorImpl();
			m_youtubeProcessor.getDirectLink(link, new IYoutubeProcessorListener() {

				@Override
				public void onStartPorcess() {
					showProgress();
				}

				@Override
				public void onGetDirectLinkComplete(String result) {
					dismissProgress();
					insertPlaylistItem(title, link, result);
				}

				@Override
				public void onFail(Exception ex) {
					dismissProgress();
				}

				@Override
				public void onSearchComplete(List<YoutubeItem> result) {
					dismissProgress();

				}

			});
		} catch (Exception ex) {
			Toast.makeText(MainActivity.INSTANCE, ex.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	public static void showProgress() {
		// Log.i(TAG, "show progress");
		// if (PROGRESS_DIALOG == null) {
		// PROGRESS_DIALOG = new ProgressDialog(MainActivity.INSTANCE);
		// PROGRESS_DIALOG.setMessage("Contact to Youtube server");
		// PROGRESS_DIALOG.setTitle("Loading...");
		// PROGRESS_DIALOG.setCancelable(true);
		// }
		// MainActivity.INSTANCE.runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// PROGRESS_DIALOG.show();
		// Log.i(TAG, "showing...");
		// }
		// });
	}

	public static void dismissProgress() {
		// MainActivity.INSTANCE.runOnUiThread(new Runnable() {
		//
		// @Override
		// public void run() {
		// Log.i(TAG, "dissmis progress");
		// if (PROGRESS_DIALOG != null) {
		// PROGRESS_DIALOG.dismiss();
		// }
		// }
		// });

	}
}
