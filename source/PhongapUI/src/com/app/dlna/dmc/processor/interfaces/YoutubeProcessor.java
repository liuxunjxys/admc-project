package com.app.dlna.dmc.processor.interfaces;

import java.util.List;

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

	public class YoutubeItem {
		private String title;
		private String duration;
		private String description;
		private String thumbnail;
		private String url;

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDuration() {
			return duration;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getThumbnail() {
			return thumbnail;
		}

		public void setThumbnail(String thumbnail) {
			this.thumbnail = thumbnail;
		}
	}
}
