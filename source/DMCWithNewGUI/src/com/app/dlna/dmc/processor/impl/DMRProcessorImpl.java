package com.app.dlna.dmc.processor.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;

import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.DMRProcessor;

public class DMRProcessorImpl implements DMRProcessor {

	private static final int SEEK_DELAY_INTERVAL = 1000;
	private static final int UPDATE_INTERVAL = 500;
	private static final String TAG = DMRProcessorImpl.class.getName();
	@SuppressWarnings("rawtypes")
	private Device m_device;
	private ControlPoint m_controlPoint;
	@SuppressWarnings("rawtypes")
	private Service m_avtransportService = null;
	@SuppressWarnings("rawtypes")
	private Service m_renderingControl = null;
	private List<DMRProcessorListner> m_listeners;
	private boolean m_isRunning = true;
	private boolean m_canUpdatePosition = true;
	private int m_currentVolume;
	private Thread m_updateThread = new Thread(new Runnable() {

		@Override
		public void run() {

			while (m_isRunning) {
				if (m_avtransportService == null)
					return;
				m_controlPoint.execute(new GetPositionInfo(m_avtransportService) {

					@SuppressWarnings("rawtypes")
					@Override
					public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
						fireOnFailEvent(invocation.getAction(), response, defaultMsg);
					}

					@SuppressWarnings("rawtypes")
					@Override
					public void received(ActionInvocation invocation, PositionInfo positionInfo) {
						fireUpdatePositionEvent(positionInfo.getTrackElapsedSeconds(), positionInfo.getTrackDurationSeconds());
					}
				});

				m_controlPoint.execute(new GetTransportInfo(m_avtransportService) {
					@SuppressWarnings("rawtypes")
					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
						fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
					}

					@SuppressWarnings("rawtypes")
					@Override
					public void received(ActionInvocation invocation, TransportInfo transportInfo) {
						switch (transportInfo.getCurrentTransportState()) {
						case PLAYING:
							fireOnPlayingEvent();
							break;
						case PAUSED_PLAYBACK:
							fireOnPausedEvent();
							break;
						case STOPPED:
							fireOnStopedEvent();
							break;
						default:
							break;
						}
					}

				});
				m_controlPoint.execute(new GetVolume(m_renderingControl) {
					@SuppressWarnings("rawtypes")
					@Override
					public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
						fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
					}

					@SuppressWarnings("rawtypes")
					@Override
					public void received(ActionInvocation actionInvocation, int currentVolume) {
						m_currentVolume = currentVolume;
					}
				});

				try {
					Thread.sleep(UPDATE_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	});

	@SuppressWarnings("rawtypes")
	public DMRProcessorImpl(Device dmr, ControlPoint controlPoint) {
		m_device = dmr;
		m_controlPoint = controlPoint;
		m_avtransportService = m_device.findService(new ServiceType("schemas-upnp-org", "AVTransport"));
		m_renderingControl = m_device.findService(new ServiceType("schemas-upnp-org", "RenderingControl"));
		if (m_avtransportService == null)
			Log.e(TAG, "service = null");
		else
			Log.e(TAG, "Service != null");

		m_listeners = new ArrayList<DMRProcessor.DMRProcessorListner>();
		m_updateThread.start();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void setURI(final String uri) {

		m_controlPoint.execute(new GetMediaInfo(m_avtransportService) {

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
			}

			@Override
			public void received(ActionInvocation invocation, MediaInfo mediaInfo) {
				String current_uri = null;
				String currentPath = null;
				String newPath = null;
				String currentQuery = null;
				String newQuery = null;

				try {
					current_uri = mediaInfo.getCurrentURI();
					if (current_uri != null) {
						URI _uri = new URI(current_uri);
						currentPath = _uri.getPath();
						currentQuery = _uri.getQuery();
					}
					URI _uri = new URI(uri);
					newPath = _uri.getPath();
					newQuery = _uri.getQuery();
				} catch (URISyntaxException e) {
					current_uri = null;
				}
				if (currentPath != null) {
					System.out.println(currentPath);
				} else {
					System.out.println("null");
				}
				if (newPath != null) {
					System.out.println(newPath);
				} else {
					System.out.println("null");
				}
				if (currentQuery != null) {
					System.out.println(currentQuery);
				} else {
					System.out.println("null");
				}
				if (newQuery != null) {
					System.out.println(newQuery);
				} else {
					System.out.println("null");
				}
				if (currentPath != null
						&& newPath != null
						&& currentPath.equals(newPath)
						&& (currentQuery == newQuery || (currentQuery != null && newQuery != null && currentQuery
								.equals(newQuery)))) {
					play();
				} else {
					stop();
					Log.e(TAG, "set AV uri = " + uri);
					m_controlPoint.execute(new SetAVTransportURI(m_avtransportService, uri, null) {
						@Override
						public void success(ActionInvocation invocation) {
							super.success(invocation);
							play();
						}

						@Override
						public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
							fireOnFailEvent(invocation.getAction(), response, defaultMsg);
						}
					});
				}
			}
		});

	}

	@SuppressWarnings("rawtypes")
	@Override
	public void play() {
		Play play = new Play(m_avtransportService) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
			}

		};

		m_controlPoint.execute(play);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void pause() {
		Pause pause = new Pause(m_avtransportService) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
			}

		};
		m_controlPoint.execute(pause);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void stop() {
		Stop stop = new Stop(m_avtransportService) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireUpdatePositionEvent(0, 0);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
			}
		};

		m_controlPoint.execute(stop);
	}

	@Override
	public void addListener(DMRProcessorListner listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener))
				m_listeners.add(listener);
		}
	}

	@Override
	public void removeListener(DMRProcessorListner listener) {
		synchronized (m_listeners) {
			m_listeners.remove(listener);
		}
	}

	@SuppressWarnings("rawtypes")
	private void fireOnFailEvent(Action action, UpnpResponse response, String message) {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onActionFail(action, response, message);
			}
		}
	}

	private void fireUpdatePositionEvent(long current, long max) {
		if (m_canUpdatePosition) {
			synchronized (m_listeners) {
				for (DMRProcessorListner listener : m_listeners) {
					listener.onUpdatePosition(current, max);
				}
			}
		}
	}

	private void fireOnStopedEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onStoped();
			}
		}
	}

	private void fireOnPausedEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onPaused();
			}
		}
	}

	private void fireOnPlayingEvent() {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onPlaying();
			}
		}
	}

	@Override
	public void dispose() {
		m_isRunning = false;
	}

	public void seek(String position) {
		Log.e(TAG, "Call seek");
		m_canUpdatePosition = false;
		@SuppressWarnings("rawtypes")
		Seek seek = new Seek(m_avtransportService, SeekMode.REL_TIME, position) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				Log.e(TAG, "Call seek complete");
				try {
					Thread.sleep(SEEK_DELAY_INTERVAL);
					m_canUpdatePosition = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse reponse, String defaultMsg) {
				Log.e(TAG, "Seek fail: " + defaultMsg);
			}
		};
		m_controlPoint.execute(seek);
	}

	@Override
	public void setVolume(int newVolume) {
		m_controlPoint.execute(new SetVolume(m_renderingControl, newVolume) {

			@SuppressWarnings("rawtypes")
			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
			}
		});
	}

	@Override
	public int getVolume() {
		return m_currentVolume;
	}

}
