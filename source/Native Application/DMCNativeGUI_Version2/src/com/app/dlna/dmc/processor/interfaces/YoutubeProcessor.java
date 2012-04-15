package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

import com.app.dlna.dmc.processor.localdevice.YoutubeItem;

public interface YoutubeProcessor {

	void executeQuery(String query, IYoutubeProcessorListener callback);

	void getDirectLink(String link, IYoutubeProcessorListener callback);

	void registURL(String link, IYoutubeProcessorListener callback);

	public interface IYoutubeProcessorListener {
		void onStartPorcess();

		void onComplete(String result);

		void onFail(Exception ex);

		void onSearchComplete(List<YoutubeItem> result);
	}
}
