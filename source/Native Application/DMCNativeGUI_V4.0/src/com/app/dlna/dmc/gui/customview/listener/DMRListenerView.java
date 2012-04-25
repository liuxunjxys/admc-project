package com.app.dlna.dmc.gui.customview.listener;

import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.CustomArrayAdapter;
import com.app.dlna.dmc.processor.interfaces.DMRProcessor.DMRProcessorListner;

public class DMRListenerView extends LinearLayout {
	protected ListView m_listView;
	protected CustomArrayAdapter m_adapter;
	protected String m_currentURI = "";

	public DMRListenerView(Context context) {
		super(context);
	}

	DMRProcessorListner m_dmrListner = new DMRProcessorListner() {

		@Override
		public void onUpdatePosition(long current, long max) {
			if (MainActivity.UPNP_PROCESSOR == null)
				return;
			if (!m_currentURI.equals(MainActivity.UPNP_PROCESSOR.getDMRProcessor().getCurrentTrackURI())) {
				int start = m_listView.getFirstVisiblePosition();
				int end = m_listView.getLastVisiblePosition();
				for (int i = start, j = end; i <= j; i++) {
					final int position = i;
					MainActivity.INSTANCE.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							m_adapter.updateSingleView(m_listView, position);
						}
					});
				}
			}
			m_currentURI = MainActivity.UPNP_PROCESSOR.getDMRProcessor().getCurrentTrackURI();

		}

		@Override
		public void onStoped() {

		}

		@Override
		public void onPlaying() {

		}

		@Override
		public void onPaused() {

		}

		@Override
		public void onErrorEvent(String error) {

		}

		@Override
		public void onEndTrack() {

		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onActionFail(Action actionCallback, UpnpResponse response, String cause) {

		}
	};

	public void updateDMRListener() {
		MainActivity.UPNP_PROCESSOR.getDMRProcessor().addListener(m_dmrListner);
	}

}
