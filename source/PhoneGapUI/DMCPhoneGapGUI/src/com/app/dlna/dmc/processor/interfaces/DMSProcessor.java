package com.app.dlna.dmc.processor.interfaces;

import java.util.List;
import java.util.Map;

import org.teleal.cling.support.model.DIDLObject;

public interface DMSProcessor {
	void browse(String objectID);

	void browse(String objectID, DMSProcessorListner listener);

	void back(DMSProcessorListner listener);

	void dispose();

	void addListener(DMSProcessorListner listener);

	void removeListener(DMSProcessorListner listener);

	public interface DMSProcessorListner {
		void onBrowseComplete(Map<String, List<? extends DIDLObject>> result);

		void onBrowseFail(String message);
	}
}
