package com.app.dlna.dmc.processor.interfaces;

import java.util.List;
import java.util.Map;

import org.teleal.cling.support.model.DIDLObject;

public interface DMSProcessor {
	void browse(String objectID, int pageIndex, DMSProcessorListner listener);

	void nextPage(DMSProcessorListner listener);

	void previousPage(DMSProcessorListner listener);

	void back(DMSProcessorListner listener);

	void dispose();

	public interface DMSProcessorListner {
		void onBrowseComplete(String objectID, boolean haveNext, boolean havePrev, Map<String, List<? extends DIDLObject>> result);

		void onBrowseFail(String message);
	}
}
