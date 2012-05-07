package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

import com.app.dlna.dmc.processor.youtube.YoutubeItem;

public interface YoutubeProcessor {

	void executeQuery(String query, IYoutubeProcessorListener callback);

	void getDirectLink(YoutubeItem item, IYoutubeProcessorListener callback);

	void registURL(String link, IYoutubeProcessorListener callback);

	public interface IYoutubeProcessorListener {
		void onStartPorcess();

		void onGetDirectLinkComplete(YoutubeItem result);

		void onFail(Exception ex);

		void onSearchComplete(List<YoutubeItem> result);
	}
}
