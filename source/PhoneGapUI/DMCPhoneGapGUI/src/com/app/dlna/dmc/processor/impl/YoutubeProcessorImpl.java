package com.app.dlna.dmc.processor.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.app.dlna.dmc.processor.http.HTTPLinkManager;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor;
import com.app.dlna.dmc.processor.youtube.YoutubeItem;
import com.app.dlna.dmc.utility.Utility;

public class YoutubeProcessorImpl implements YoutubeProcessor {

	private static final String TAG = YoutubeProcessorImpl.class.getName();

	@Override
	public void getDirectLink(final String link, final IYoutubeProcessorListener callback) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					callback.onStartPorcess();
					URL youtube = new URL(link);
					Socket socket = new Socket(youtube.getHost(), 80);
					PrintStream ps = new PrintStream(socket.getOutputStream());
					ps.println("GET " + youtube.getFile() + " HTTP/1.1");
					ps.println("Host: " + youtube.getAuthority());
					ps.println("Connection: close");
					ps.println("User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1");
					ps.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
					ps.println();
					ps.flush();

					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String inputLine;
					String directlink = null;
					while ((inputLine = in.readLine()) != null) {
						System.out.println(inputLine);
						if (inputLine.contains("img.src")) {
							System.out.println(" Line = " + inputLine);
							directlink = inputLine.substring(inputLine.indexOf('"') + 1, inputLine.lastIndexOf('"'))
									.replace("\\u0026", "&").replace("\\", "").replace("generate_204", "videoplayback");
							System.out.println(" Direct Link = " + directlink);
							// TODO: find out what is crossdomain.xml
							// if (!directlink.contains("crossdomain.xml"))
							// break;
						}
					}
					in.close();
					ps.close();
					callback.onGetDirectLinkComplete(directlink);
				} catch (Exception ex) {
					Log.e(TAG, "Get Direct Link Fail");
					ex.printStackTrace();
					callback.onFail(ex);
				}
			}
		}).start();

	}

	@Override
	public void registURL(String link, final IYoutubeProcessorListener callback) {
		callback.onStartPorcess();
		getDirectLink(link, new IYoutubeProcessorListener() {

			@Override
			public void onStartPorcess() {

			}

			@Override
			public void onFail(Exception ex) {

			}

			@Override
			public void onGetDirectLinkComplete(String result) {
				String disget = Utility.getMD5(result);
				String disgetLink = "/" + disget.substring(0, disget.length() - 1) + ".mp4";
				try {
					HTTPLinkManager.LINK_MAP.put(disgetLink, result);
					Log.d(TAG, "DirectLink = " + result);
					Log.d(TAG, "DisgetLink = " + disgetLink);
					callback.onGetDirectLinkComplete(disgetLink);
				} catch (Exception ex) {
					ex.printStackTrace();
					callback.onFail(ex);
				}

			}

			@Override
			public void onSearchComplete(List<YoutubeItem> result) {

			}
		});

	}

	@Override
	public void executeQuery(final String query, final IYoutubeProcessorListener callback) {
		callback.onStartPorcess();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.i(TAG, "Execute search, query = " + query);
					String _query = query.replace(" ", "%20");
					URL jsonURL = new URL("http://gdata.youtube.com/feeds/api/videos?q=" + _query
							+ "&orderby=updated&v=2&alt=jsonc");
					URLConnection jc = jsonURL.openConnection();
					InputStream is = jc.getInputStream();
					String jsonTxt = IOUtils.toString(is);

					JSONObject jj = new JSONObject(jsonTxt);

					JSONObject jdata = jj.getJSONObject("data");
					JSONArray aitems = jdata.getJSONArray("items");
					List<YoutubeItem> youtubeItems = new ArrayList<YoutubeItem>();
					for (int i = 0; i < aitems.length(); ++i) {
						JSONObject item = aitems.getJSONObject(i);
						YoutubeItem youtubeItem = new YoutubeItem();
						youtubeItem.setTitle(item.getString("title"));
						youtubeItem.setDescription(item.getString("description"));
						youtubeItem.setThumbnail(item.getJSONObject("thumbnail").getString("sqDefault"));
						youtubeItem.setUrl(item.getJSONObject("player").getString("default"));
						youtubeItem.setDuration(item.getString("duration"));
						youtubeItems.add(youtubeItem);
						if (youtubeItems.size() >= 40)
							break;
					}
					callback.onSearchComplete(youtubeItems);
				} catch (Exception ex) {
					callback.onFail(ex);
				}

			}
		}).start();

	}

}
