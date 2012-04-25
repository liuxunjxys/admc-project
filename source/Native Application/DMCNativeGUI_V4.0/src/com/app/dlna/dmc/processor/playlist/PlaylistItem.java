package com.app.dlna.dmc.processor.playlist;

public class PlaylistItem {
	public enum Type {
		AUDIO, VIDEO, IMAGE
	};

	private long m_id;
	private String m_url;
	private String m_title;
	private Type m_type;

	public void setId(long id) {
		m_id = id;
	}

	public long getId() {
		return m_id;
	}

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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PlaylistItem))
			return false;
		PlaylistItem other = (PlaylistItem) o;
		if (other.getUrl().equals(this.m_url) && other.getType().equals(this.m_type)
				&& other.getTitle().equals(this.m_title))
			return true;
		return false;
	}

}
