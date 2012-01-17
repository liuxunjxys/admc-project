package com.app.dlna.dmc.processor.interfaces;

import org.teleal.cling.model.meta.Action;

public interface IDMRProcessor {
	void setURI(String uri);

	void play();

	void pause();

	void stop();

	void seek(int position);

	void seek(String position);

	void addListener(DMRProcessorListner listener);

	void removeListener(DMRProcessorListner listener);

	void dispose();

	@SuppressWarnings("rawtypes")
	public interface DMRProcessorListner {

		void onActionComplete(Action actionCallback);

		void onActionFail(Action actionCallback, String cause);

		void onUpdatePosition(long current, long max);
	}
}
