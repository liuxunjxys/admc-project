package com.app.dlna.dmc.processor.playlist;

public class PlaylistItem {
	public enum Type {
		AUDIO, VIDEO, IMAGE
	};

	private String m_url;
	private String m_title;
	private Type m_type;

	public String getUrl() {
		return m_url;
	}

	public void setUrl(String url) {
		this.m_url = url;
	}

	public String getTitle() {
		return m_title;
	}

	public void setTitle(String title) {
		this.m_title = title;
	}

	public Type getType() {
		return m_type;
	}

	public void setType(Type type) {
		this.m_type = type;
	}

}
