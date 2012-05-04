package com.app.dlna.dmc.gui.customview.nowplaying;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;

public class MultiContentSurfaceView extends SurfaceView {

	private static final String TAG = MultiContentSurfaceView.class.getName();
	private ImageOverlayData m_imageOverlay = null;

	public MultiContentSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setImageOverlay(ImageOverlayData bm) {
		m_imageOverlay = bm;
	}

	public ImageOverlayData getBitmapOverlay() {
		return m_imageOverlay;
	}

	public static class ImageOverlayData {
		public Bitmap bm;
		public String url;

		public ImageOverlayData(Bitmap bm, String url) {
			this.bm = bm;
			this.url = url;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.i(TAG, "On Draw");
		if (m_imageOverlay != null && m_imageOverlay.bm != null) {
			canvas.drawColor(Color.BLACK);
			canvas.drawBitmap(m_imageOverlay.bm, 0, 0, new Paint());

		}
	}
}
