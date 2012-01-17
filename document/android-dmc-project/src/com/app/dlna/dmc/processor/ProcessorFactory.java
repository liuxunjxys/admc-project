package com.app.dlna.dmc.processor;

import org.teleal.cling.model.meta.RemoteDevice;

import android.app.Activity;

import com.app.dlna.dmc.processor.impl.DMRProcessorImpl;
import com.app.dlna.dmc.processor.impl.DMSProcessorImpl;
import com.app.dlna.dmc.processor.impl.DevicesProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.IDMRProcessor;
import com.app.dlna.dmc.processor.interfaces.IDMSProcessor;
import com.app.dlna.dmc.processor.interfaces.IDevicesProcessor;

public class ProcessorFactory {
	private static IDevicesProcessor PROCESSOR = null;
	private static IDMSProcessor DMS_PROCESSOR;
	private static IDMRProcessor DMR_PROCESSOR;

	public static IDevicesProcessor getProcessorInstance(Activity activity) {
		if (PROCESSOR == null) {
			PROCESSOR = new DevicesProcessorImpl();
		}

		if (activity != null) {
			PROCESSOR.setActivity(activity);
		}

		return PROCESSOR;
	}

	public static IDMSProcessor getDMSProcessorInstance(RemoteDevice device) {
		if (DMS_PROCESSOR != null) {
			DMS_PROCESSOR.dispose();
		}
		DMS_PROCESSOR = new DMSProcessorImpl(device, PROCESSOR.getControlPoint());
		return DMS_PROCESSOR;
	}

	public static IDMRProcessor getDMRProcessorInstance(RemoteDevice device) {
		DMR_PROCESSOR = new DMRProcessorImpl(device, PROCESSOR.getControlPoint());
		return DMR_PROCESSOR;
	}
}
