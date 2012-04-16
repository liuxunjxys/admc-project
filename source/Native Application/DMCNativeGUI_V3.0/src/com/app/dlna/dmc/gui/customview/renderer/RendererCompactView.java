package com.app.dlna.dmc.gui.customview.renderer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import app.dlna.controller.R;

public class RendererCompactView extends LinearLayout {

	public RendererCompactView(Context context, AttributeSet attrs) {
		super(context, attrs);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_dmr_compact,
				this);
	}

	public RendererCompactView(Context context) {
		super(context);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_dmr_compact,
				this);
	}

}
