package com.app.dlna.dmc.gui.customview.nowplaying;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import app.dlna.controller.v4.R;

public class TopToolbarView extends LinearLayout {

	public TopToolbarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.cv_toolbar_nowplayling_top, this);

	}

}
