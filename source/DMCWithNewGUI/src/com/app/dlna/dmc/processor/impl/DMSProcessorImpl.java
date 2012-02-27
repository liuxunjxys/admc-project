package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.UpnpProcessor;

public class DMSProcessorImpl implements DMSProcessor {

	private static final String TAG = DMSProcessorImpl.class.getName();
	@SuppressWarnings("rawtypes")
	private Device m_server;
	private ControlPoint m_controlPoint;
	private Map<String, List<? extends DIDLObject>> result;
	private List<DMSProcessorListner> m_listeners;

	public DMSProcessorImpl(UpnpProcessor upnpProcessor) {
		m_server = upnpProcessor.getCurrentDMS();
		m_controlPoint = upnpProcessor.getControlPoint();
		m_listeners = new ArrayList<DMSProcessor.DMSProcessorListner>();
	}

	@SuppressWarnings("rawtypes")
	public DMSProcessorImpl(ControlPoint controlPoint, Device device) {
		m_server = device;
		m_controlPoint = controlPoint;
		m_listeners = new ArrayList<DMSProcessor.DMSProcessorListner>();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void browse(String objectID) {
		result = new HashMap<String, List<? extends DIDLObject>>();
		Service cds = m_server.findService(new ServiceType("schemas-upnp-org", "ContentDirectory"));

		if (cds != null) {
			Action action = cds.getAction("Browse");
			ActionInvocation actionInvocation = new ActionInvocation(action);
			actionInvocation.setInput("ObjectID", objectID);
			actionInvocation.setInput("BrowseFlag", "BrowseDirectChildren");
			actionInvocation.setInput("Filter", "*");
			actionInvocation.setInput("StartingIndex", new UnsignedIntegerFourBytes(0));
			actionInvocation.setInput("RequestedCount", new UnsignedIntegerFourBytes(999));
			actionInvocation.setInput("SortCriteria", null);
			ActionCallback actionCallback = new ActionCallback(actionInvocation) {

				@Override
				public void success(ActionInvocation invocation) {
					Log.e(TAG, invocation.getOutput("Result").toString());

					try {
						DIDLParser parser = new DIDLParser();
						DIDLContent content = parser.parse(invocation.getOutput("Result").toString());
						for (Container container : content.getContainers()) {
							Log.e(TAG, "Container = " + container.getTitle());
						}
						result.put("Containers", content.getContainers());

						for (Item item : content.getItems()) {
							Log.e(TAG, "Item = " + item.getTitle());
						}

						result.put("Items", content.getItems());

						fireOnBrowseCompleteEvent();

					} catch (Exception e) {
						Log.e(TAG, e.getMessage());
						m_listeners.remove("Error: " + e.getMessage());
					}
				}

				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
					Log.e(TAG, defaultMsg);
					fireOnBrowseFailEvent(defaultMsg);
				}

			};

			m_controlPoint.execute(actionCallback);
		}
	}

	public void dispose() {

	}

	@Override
	public void addListener(DMSProcessorListner listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener))
				m_listeners.add(listener);
		}

	}

	@Override
	public void removeListener(DMSProcessorListner listener) {
		synchronized (m_listeners) {
			m_listeners.remove(listener);
		}
	}

	private void fireOnBrowseCompleteEvent() {
		synchronized (m_listeners) {
			for (DMSProcessorListner listener : m_listeners) {
				listener.onBrowseComplete(result);
			}
		}
	}

	private void fireOnBrowseFailEvent(String message) {
		synchronized (m_listeners) {
			for (DMSProcessorListner listener : m_listeners) {
				listener.onBrowseFail(message);
			}
		}
	}
}
