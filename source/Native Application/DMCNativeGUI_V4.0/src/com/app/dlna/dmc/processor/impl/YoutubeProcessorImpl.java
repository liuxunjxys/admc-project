package com.app.dlna.dmc.processor.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
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
	private static final String REQUEST_COUNT = "50";
	private static final String QUERY_VIDEO = "http://gdata.youtube.com/feeds/api/videos?v=2&q={query}&start-index=1&max-results={count}&v=2&alt=json";
	private static final String QUERY_VIDEO_INFO = "http://www.youtube.com/get_video_info?video_id={id}";

	// &account_playback_token=JphhpkbSQMOcYF-P84VNk3QdCSl8MEAxMzM2MzEyMTA2
	// &url_encoded_fmt_stream_map=
	// url%3Dhttp%253A%252F%252Fo-o.preferred.fpt-sgn1.v6.lscache8.c.youtube.com%252Fvideoplayback%253Fupn%253DJeTmm3jg7-0%2526sparams%253Dcp%25252Cid%25252Cip%25252Cipbits%25252Citag%25252Cratebypass%25252Csource%25252Cupn%25252Cexpire%2526fexp%253D909519%25252C919310%25252C907217%25252C919306%2526itag%253D43%2526ip%253D1.0.0.0%2526signature%253D8E60B43A32C7A5A5F3386A40483773D59F4D1D46.D39A96793D616CABBCFB2B222E8A98B033D675D8%2526sver%253D3%2526ratebypass%253Dyes%2526source%253Dyoutube%2526expire%253D1336333571%2526key%253Dyt1%2526ipbits%253D8%2526cp%253DU0hSS1NOUV9MS0NOMl9LSFVGOjc3TjdnbzVwTnpK%2526id%253D8354800dc3f9835a%26quality%3Dmedium%26fallback_host%3Dtc.v6.cache8.c.youtube.com%26type%3Dvideo%252Fwebm%253B%2Bcodecs%253D%2522vp8.0%252C%2Bvorbis%2522%26itag%3D43%2C
	// url%3Dhttp%253A%252F%252Fo-o.preferred.fpt-sgn1.v6.lscache7.c.youtube.com%252Fvideoplayback%253Fupn%253DJeTmm3jg7-0%2526sparams%253Dalgorithm%25252Cburst%25252Ccp%25252Cfactor%25252Cid%25252Cip%25252Cipbits%25252Citag%25252Csource%25252Cupn%25252Cexpire%2526fexp%253D909519%25252C919310%25252C907217%25252C919306%2526algorithm%253Dthrottle-factor%2526itag%253D34%2526ip%253D1.0.0.0%2526burst%253D40%2526sver%253D3%2526signature%253DC4DE685F184E2AE7BBB8D9DD29CDDBF6303AB26D.A4DA8A573582EE23C4E4EFCBFD439F5B73840661%2526source%253Dyoutube%2526expire%253D1336333571%2526key%253Dyt1%2526ipbits%253D8%2526factor%253D1.25%2526cp%253DU0hSS1NOUV9MS0NOMl9LSFVGOjc3TjdnbzVwTnpK%2526id%253D8354800dc3f9835a%26quality%3Dmedium%26fallback_host%3Dtc.v6.cache7.c.youtube.com%26type%3Dvideo%252Fx-flv%26itag%3D34%2C
	// url%3Dhttp%253A%252F%252Fo-o.preferred.fpt-sgn1.v7.lscache1.c.youtube.com%252Fvideoplayback%253Fupn%253DJeTmm3jg7-0%2526sparams%253Dcp%25252Cid%25252Cip%25252Cipbits%25252Citag%25252Cratebypass%25252Csource%25252Cupn%25252Cexpire%2526fexp%253D909519%25252C919310%25252C907217%25252C919306%2526itag%253D18%2526ip%253D1.0.0.0%2526signature%253DC789A824ED468AFDDEC16B25306FF3EE429AFEBE.574604AE9C459398F9E371327390484A2809FDCC%2526sver%253D3%2526ratebypass%253Dyes%2526source%253Dyoutube%2526expire%253D1336333571%2526key%253Dyt1%2526ipbits%253D8%2526cp%253DU0hSS1NOUV9MS0NOMl9LSFVGOjc3TjdnbzVwTnpK%2526id%253D8354800dc3f9835a%26quality%3Dmedium%26fallback_host%3Dtc.v7.cache1.c.youtube.com%26type%3Dvideo%252Fmp4%253B%2Bcodecs%253D%2522avc1.42001E%252C%2Bmp4a.40.2%2522%26itag%3D18%2C
	// url%3Dhttp%253A%252F%252Fo-o.preferred.fpt-sgn1.v15.lscache2.c.youtube.com%252Fvideoplayback%253Fupn%253DJeTmm3jg7-0%2526sparams%253Dalgorithm%25252Cburst%25252Ccp%25252Cfactor%25252Cid%25252Cip%25252Cipbits%25252Citag%25252Csource%25252Cupn%25252Cexpire%2526fexp%253D909519%25252C919310%25252C907217%25252C919306%2526algorithm%253Dthrottle-factor%2526itag%253D5%2526ip%253D1.0.0.0%2526burst%253D40%2526sver%253D3%2526signature%253D054CFB8086EF8C3ABD4891F3457D5965A770065D.41CC0D91346ED1713EBA7D01A13C48FA42A6A59E%2526source%253Dyoutube%2526expire%253D1336333571%2526key%253Dyt1%2526ipbits%253D8%2526factor%253D1.25%2526cp%253DU0hSS1NOUV9MS0NOMl9LSFVGOjc3TjdnbzVwTnpK%2526id%253D8354800dc3f9835a%26quality%3Dsmall%26fallback_host%3Dtc.v15.cache2.c.youtube.com%26type%3Dvideo%252Fx-flv%26itag%3D5
	// &allow_embed=1
	// &user_gender=m
	// &fexp=909519%2C919310%2C907217%2C919306
	// &allow_ratings=1
	// &keywords=robot%2Cwoman%2Cjapan
	// &track_embed=0
	// &view_count=1387338
	// &video_verticals=%5B231%2C+174%2C+1141%5D
	// &fmt_list=43%2F320x240%2F99%2F0%2F0%2C34%2F320x240%2F9%2F0%2F115%2C18%2F320x240%2F9%2F0%2F115%2C5%2F320x240%2F7%2F0%2F0
	// &author=videogamesartist
	// &muted=0&length_seconds=26
	// &user_age=32
	// &has_cc=False
	// &tmi=1
	// &ftoken=vciXM6oMw6hZPNLAPl34WLsdIip8MTMzNjM5ODUwNkAxMzM2MzEyMTA2
	// &status=ok
	// &tabsb=1
	// &watermark=%2Chttp%3A%2F%2Fs.ytimg.com%2Fyt%2Fimg%2Fwatermark%2Fyoutube_watermark-vflHX6b6E.png%2Chttp%3A%2F%2Fs.ytimg.com%2Fyt%2Fimg%2Fwatermark%2Fyoutube_hd_watermark-vflAzLcD6.png
	// &timestamp=1336312106
	// &plid=AAS_XmhwbaSZV4_b
	// &endscreen_module=http%3A%2F%2Fs.ytimg.com%2Fyt%2Fswfbin%2Fendscreen-vfljxohQq.swf
	// &watch_ajax_token=H4iG7usUqdcWGSz2Bp-TsZAnKo18MTMzNjM5ODUwNkAxMzM2MzEyMTA2
	// &hl=en_US
	// &no_get_video_log=1
	// &fshd=1
	// &vq=auto
	// &avg_rating=4.69915254237
	// &logwatch=1
	// &sw=0.1
	// &token=vjVQa1PpcFO88ND1CMNbWGgnSQdE47U5aD49JMDILIU%3D
	// &thumbnail_url=http%3A%2F%2Fi4.ytimg.com%2Fvi%2Fg1SADcP5g1o%2Fdefault.jpg
	// &video_id=g1SADcP5g1o&title=Realistic+Robot+Woman

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
						if (inputLine.contains("img.src")) {
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
					URL jsonURL = new URL(QUERY_VIDEO.replace("{query}", _query).replace("{count}", REQUEST_COUNT));
					String jsonTxt = IOUtils.toString(jsonURL, "utf-8");
					Log.i(TAG, "jsonTxt = " + jsonTxt);
					JSONObject jj = new JSONObject(jsonTxt);
					JSONObject feeds = jj.getJSONObject("feed");
					JSONArray entries = feeds.getJSONArray("entry");
					List<YoutubeItem> youtubeItems = new ArrayList<YoutubeItem>();
					for (int i = 0; i < entries.length(); ++i) {
						JSONObject mediaGroup = entries.getJSONObject(i).getJSONObject("media$group");
						YoutubeItem youtubeItem = new YoutubeItem();
						youtubeItem.setId(mediaGroup.getJSONObject("yt$videoid").getString("$t"));
						youtubeItem.setTitle(mediaGroup.getJSONObject("media$title").getString("$t"));
						youtubeItem.setDuration(Long.valueOf(mediaGroup.getJSONObject("yt$duration").getString(
								"seconds")));
						JSONArray thumbs = mediaGroup.getJSONArray("media$thumbnail");
						if (thumbs.length() > 0)
							youtubeItem.setThumbnail(thumbs.getJSONObject(0).getString("url"));
						youtubeItem.setAuthor(entries.getJSONObject(i).getJSONArray("author").getJSONObject(0)
								.getJSONObject("name").getString("$t"));
						youtubeItems.add(youtubeItem);
					}
					callback.onSearchComplete(youtubeItems);
				} catch (Exception ex) {
					callback.onFail(ex);
				}

			}
		}).start();

	}

}
