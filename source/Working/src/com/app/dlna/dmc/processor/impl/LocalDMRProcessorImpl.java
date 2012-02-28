package com.app.dlna.dmc.processor.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.app.dlna.dmc.processor.interfaces.DMRProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;

public class LocalDMRProcessorImpl implements DMRProcessor {
	private static final String TAG = LocalDMRProcessorImpl.class.getSimpleName();
	private Context m_context;

	public LocalDMRProcessorImpl(Context context) {
		m_context = context;
	}

	// @Override
	// public void setURI(String uri) {
	//
	// }

	@Override
	public void setURIandPlay(String uri) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(uri).toLowerCase();
		if (extension.startsWith(".")) {
			extension.replaceFirst(".", "");
		}
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		Log.i(TAG, "extension = " + extension + " mimeType = " + mimeType);
		if (mimeType == null || mimeType.isEmpty()) {
			if (uri.contains("o-o.preferred"))
				mimeType = "video/mp4";
		}
		Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
		mediaIntent.setDataAndType(Uri.parse(uri), mimeType);
		mediaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		m_context.startActivity(mediaIntent);
	}

	@Override
	public void play() {

	}

	@Override
	public void pause() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void seek(String position) {

	}

	@Override
	public void setVolume(int newVolume) {

	}

	@Override
	public int getVolume() {
		return 0;
	}

	@Override
	public void addListener(DMRProcessorListner listener) {

	}

	@Override
	public void removeListener(DMRProcessorListner listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setPlaylistProcessor(PlaylistProcessor playlistProcessor) {

	}

}
