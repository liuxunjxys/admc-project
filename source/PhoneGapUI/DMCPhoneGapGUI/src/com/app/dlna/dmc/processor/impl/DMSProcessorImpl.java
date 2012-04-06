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
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.VideoItem;

import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.DMSProcessor;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.processor.playlist.PlaylistItem.Type;

public class DMSProcessorImpl implements DMSProcessor {

	private static final String TAG = DMSProcessorImpl.class.getName();
	@SuppressWarnings("rawtypes")
	private Device m_server;
	private ControlPoint m_controlPoint;
	private List<String> m_traceID;
	private int m_currentPageIndex;
	public static int ITEM_PER_PAGE = 25;
	private List<DIDLObject> m_DIDLObjectList;

	@SuppressWarnings("rawtypes")
	public DMSProcessorImpl(Device device, ControlPoint controlPoint) {
		m_server = device;
		m_controlPoint = controlPoint;
		m_traceID = new ArrayList<String>();
		m_traceID.add("-1");
		m_DIDLObjectList = new ArrayList<DIDLObject>();
	}

	// @SuppressWarnings({ "rawtypes", "unchecked" })
	// public void browse(String objectID) {
	// result = new HashMap<String, List<? extends DIDLObject>>();
	// Service cds = m_server.findService(new ServiceType("schemas-upnp-org",
	// "ContentDirectory"));
	// if (cds != null) {
	// Action action = cds.getAction("Browse");
	// ActionInvocation actionInvocation = new ActionInvocation(action);
	// actionInvocation.setInput("ObjectID", objectID);
	// actionInvocation.setInput("BrowseFlag", "BrowseDirectChildren");
	// actionInvocation.setInput("Filter", "*");
	// actionInvocation.setInput("StartingIndex", new
	// UnsignedIntegerFourBytes(0));
	// actionInvocation.setInput("RequestedCount", new
	// UnsignedIntegerFourBytes(999));
	// actionInvocation.setInput("SortCriteria", null);
	// ActionCallback actionCallback = new ActionCallback(actionInvocation) {
	//
	// @Override
	// public void success(ActionInvocation invocation) {
	// try {
	// DIDLParser parser = new DIDLParser();
	// DIDLContent content =
	// parser.parse(invocation.getOutput("Result").toString());
	// result.put("Containers", content.getContainers());
	// result.put("Items", content.getItems());
	// fireOnBrowseCompleteEvent();
	// } catch (Exception e) {
	// Log.e(TAG, e.getMessage());
	// }
	// }
	//
	// @Override
	// public void failure(ActionInvocation invocation, UpnpResponse operation,
	// String defaultMsg) {
	// Log.e(TAG, defaultMsg);
	// fireOnBrowseFailEvent(defaultMsg);
	// }
	// };
	// m_controlPoint.execute(actionCallback);
	// }
	// }

	public void dispose() {

	}

	// private void fireOnBrowseCompleteEvent() {
	// synchronized (m_listeners) {
	// for (DMSProcessorListner listener : m_listeners) {
	// listener.onBrowseComplete(result);
	// }
	// }
	// }
	//
	// private void fireOnBrowseFailEvent(String message) {
	// synchronized (m_listeners) {
	// for (DMSProcessorListner listener : m_listeners) {
	// listener.onBrowseFail(message);
	// }
	// }
	// }

	@Override
	public void browse(String objectID, int pageIndex, final DMSProcessorListner listener) {
		m_traceID.add(objectID);
		m_currentPageIndex = 0;
		executeBrowse(objectID, pageIndex, listener);
		for (String _id : m_traceID) {
			Log.e(TAG, _id);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executeBrowse(final String objectID, final int pageIndex, final DMSProcessorListner listener) {
		Service cds = m_server.findService(new ServiceType("schemas-upnp-org", "ContentDirectory"));
		if (cds != null) {
			m_currentPageIndex = pageIndex;
			int startIndex = pageIndex * ITEM_PER_PAGE;
			Action action = cds.getAction("Browse");
			ActionInvocation actionInvocation = new ActionInvocation(action);
			actionInvocation.setInput("ObjectID", objectID);
			actionInvocation.setInput("BrowseFlag", "BrowseDirectChildren");
			actionInvocation.setInput("Filter", "*");
			actionInvocation.setInput("StartingIndex", new UnsignedIntegerFourBytes(startIndex));
			actionInvocation.setInput("RequestedCount", new UnsignedIntegerFourBytes(ITEM_PER_PAGE + 1));
			actionInvocation.setInput("SortCriteria", null);
			ActionCallback actionCallback = new ActionCallback(actionInvocation) {

				@Override
				public void success(ActionInvocation invocation) {
					try {
						DIDLParser parser = new DIDLParser();
						DIDLContent content = parser.parse(invocation.getOutput("Result").toString());
						Log.i(TAG, "Container = " + content.getContainers().size() + " Item = "
								+ content.getItems().size());
						Map<String, List<? extends DIDLObject>> result = new HashMap<String, List<? extends DIDLObject>>();
						List<Container> containers = content.getContainers();
						List<Item> items = content.getItems();
						boolean haveNext = false;
						if (containers.size() > ITEM_PER_PAGE) {
							haveNext = true;
							containers.remove(containers.size() - 1);
						}
						if (items.size() > ITEM_PER_PAGE) {
							haveNext = true;
							items.remove(items.size() - 1);
						}
						m_DIDLObjectList.clear();
						m_DIDLObjectList.addAll(containers);
						m_DIDLObjectList.addAll(items);
						boolean havePrev = m_currentPageIndex > 0;
						result.put("Containers", containers);
						result.put("Items", items);
						listener.onBrowseComplete(objectID, haveNext, havePrev, result);
					} catch (Exception e) {
						e.printStackTrace();
						listener.onBrowseFail(e.getMessage());
						m_traceID.remove(m_traceID.size() - 1);
					}
				}

				@Override
				public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
					Log.e(TAG, defaultMsg);
					listener.onBrowseFail(defaultMsg);
				}
			};
			m_controlPoint.execute(actionCallback);
		}
	}

	@Override
	public void back(DMSProcessorListner listener) {
		m_currentPageIndex = 0;
		int traceSize = m_traceID.size();
		if (traceSize > 2) {
			String parentID = m_traceID.get(traceSize - 2);
			browse(parentID, 0, listener);
			m_traceID.remove(m_traceID.size() - 1);
			m_traceID.remove(m_traceID.size() - 1);
		} else {
		}
	}

	@Override
	public void nextPage(DMSProcessorListner listener) {
		executeBrowse(m_traceID.get(m_traceID.size() - 1), m_currentPageIndex + 1, listener);
	}

	@Override
	public void previousPage(DMSProcessorListner listener) {
		if (m_currentPageIndex > 0)
			executeBrowse(m_traceID.get(m_traceID.size() - 1), m_currentPageIndex - 1, listener);
	}

	@Override
	public DIDLObject getDIDLObject(String objectID) {
		for (DIDLObject object : m_DIDLObjectList) {
			if (object.getId().equals(objectID))
				return object;
		}
		return null;
	}

	@Override
	public void removeAllFromPlaylist(PlaylistProcessor playlistProcessor) {
		if (playlistProcessor == null) {
			Log.w(TAG, "Playlist processor = null");
			return;
		}
		int count = m_DIDLObjectList.size();
		for (int i = 0; i < count; ++i) {
			DIDLObject object = m_DIDLObjectList.get(i);
			if (object instanceof Item) {
				PlaylistItem item = new PlaylistItem();
				item.setTitle(object.getTitle());
				item.setUrl(object.getResources().get(0).getValue());
				if (object instanceof AudioItem) {
					item.setType(Type.AUDIO);
				} else if (object instanceof VideoItem) {
					item.setType(Type.VIDEO);
				} else {
					item.setType(Type.IMAGE);
				}
				playlistProcessor.removeItem(item);
			}
		}
	}

	@Override
	public void addAllToPlaylist(PlaylistProcessor playlistProcessor) {
		if (playlistProcessor == null) {
			Log.w(TAG, "Playlist processor = null");
			return;
		}
		int count = m_DIDLObjectList.size();
		for (int i = 0; i < count; ++i) {
			DIDLObject object = m_DIDLObjectList.get(i);
			if (object instanceof Item) {
				PlaylistItem item = new PlaylistItem();
				item.setTitle(object.getTitle());
				item.setUrl(object.getResources().get(0).getValue());
				if (object instanceof AudioItem) {
					item.setType(Type.AUDIO);
				} else if (object instanceof VideoItem) {
					item.setType(Type.VIDEO);
				} else {
					item.setType(Type.IMAGE);
				}
				playlistProcessor.addItem(item);
			}
		}
	}

	@Override
	public List<DIDLObject> getAllObjects() {
		return m_DIDLObjectList;
	}

}
