package com.app.dlna.dmc.gui.localcontent;

import org.teleal.cling.support.model.item.Item;

public class ItemDisplay {
	private int m_iconId;
	private Item m_item;

	public ItemDisplay(Item item, int iconId) {
		m_iconId = iconId;
		m_item = item;
	}

	public int getIconId() {
		return m_iconId;
	}

	public void setIconId(int iconId) {
		this.m_iconId = iconId;
	}

	public Item getItem() {
		return m_item;
	}

	public void setItem(Item item) {
		this.m_item = item;
	}
}
