package com.app.dlna.dmc.processor.interfaces;

public interface YoutubeProcessor {

	void getDirectLink(String link, IYoutubeProcessorListener callback);

	void registURL(String link, IYoutubeProcessorListener callback);

	public interface IYoutubeProcessorListener {
		void onStartPorcess();

		void onComplete(String result);

		void onFail(Exception ex);
	}
}
