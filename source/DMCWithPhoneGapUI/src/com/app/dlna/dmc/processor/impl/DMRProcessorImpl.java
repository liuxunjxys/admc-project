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
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;

public class DMRProcessorImpl implements DMRProcessor {
	private static final int UPDATE_INTERVAL = 1000;
	private static final String TAG = DMRProcessorImpl.class.getName();
	protected static final long SEEK_DELAY_INTERVAL = 200;
	@SuppressWarnings("rawtypes")
	private Device m_device;
	private ControlPoint m_controlPoint;
	@SuppressWarnings("rawtypes")
	private Service m_avtransportService = null;
	@SuppressWarnings("rawtypes")
	private Service m_renderingControl = null;
	private List<DMRProcessorListner> m_listeners;
	private PlaylistProcessor m_playlistProcessor;
	private boolean m_isRunning = true;
	private int m_currentVolume;
	private boolean m_isBusy = false;
	private int m_state = -1;
	private static final int PLAYING = 0;
	private static final int PAUSE = 1;
	private static final int STOP = 2;
	private boolean check1 = false;
	private boolean check2 = false;
	private boolean check3 = false;

	private Thread m_updateThread = new Thread(new Runnable() {

		@Override
		public void run() {

			while (m_isRunning) {
				if (m_avtransportService == null)
					return;
				if (!check1) {
					check1 = true;
					m_controlPoint.execute(new GetPositionInfo(m_avtransportService) {

						@SuppressWarnings("rawtypes")
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
							fireOnFailEvent(invocation.getAction(), response, defaultMsg);
							check1 = false;
						}

						@SuppressWarnings("rawtypes")
						@Override
						public void received(ActionInvocation invocation, PositionInfo positionInfo) {
							Log.v(TAG, positionInfo.toString());
							fireUpdatePositionEvent(positionInfo.getTrackElapsedSeconds(), positionInfo.getTrackDurationSeconds());
							if ((positionInfo.getTrack().getValue() == 0 || positionInfo.getElapsedPercent() == 100) && m_state == PLAYING) {
								m_state = STOP;
								new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											Thread.sleep(4000);
											if (m_state == STOP) {
												fireOnEndTrackEvent();
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}).start();
							}
							check1 = false;
						}
					});

				}

				if (!check2) {
					check2 = true;
					m_controlPoint.execute(new GetTransportInfo(m_avtransportService) {
						@SuppressWarnings("rawtypes")
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
							check2 = false;
						}

						@SuppressWarnings("rawtypes")
						@Override
						public void received(ActionInvocation invocation, TransportInfo transportInfo) {
							switch (transportInfo.getCurrentTransportState()) {
							case PLAYING:
								fireOnPlayingEvent();
								m_state = PLAYING;
								break;
							case PAUSED_PLAYBACK:
								fireOnPausedEvent();
								m_state = PAUSE;
								break;
							case STOPPED:
								fireOnStopedEvent();
								m_state = STOP;
								break;
							default:
								break;
							}
							check2 = false;
						}

					});
				}

				if (!check3) {
					check3 = true;
					m_controlPoint.execute(new GetVolume(m_renderingControl) {
						@SuppressWarnings("rawtypes")
						@Override
						public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
							fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
							check3 = false;
						}

						@SuppressWarnings("rawtypes")
						@Override
						public void received(ActionInvocation actionInvocation, int currentVolume) {
							m_currentVolume = currentVolume;
							check3 = false;
						}
					});
				}

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
		m_listeners = new ArrayList<DMRProcessor.DMRProcessorListner>();
		m_updateThread.start();
	}

//	@SuppressWarnings({ "rawtypes" })
//	@Override
//	public void setURI(final String uri) {
//		if (m_controlPoint == null || m_avtransportService == null)
//			return;
//		m_isBusy = true;
//		m_controlPoint.execute(new GetMediaInfo(m_avtransportService) {
//
//			@Override
//			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
//				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
//				m_isBusy = false;
//			}
//
//			@Override
//			public void received(ActionInvocation invocation, MediaInfo mediaInfo) {
//				if (mediaInfo != null && mediaInfo.getCurrentURIMetaData() != null)
//					Log.e(TAG, mediaInfo.getCurrentURIMetaData());
//				String current_uri = null;
//				String currentPath = null;
//				String newPath = null;
//				String currentQuery = null;
//				String newQuery = null;
//
//				try {
//					current_uri = mediaInfo.getCurrentURI();
//					if (current_uri != null) {
//						URI _uri = new URI(current_uri);
//						currentPath = _uri.getPath();
//						currentQuery = _uri.getQuery();
//					}
//					URI _uri = new URI(uri);
//					newPath = _uri.getPath();
//					newQuery = _uri.getQuery();
//				} catch (URISyntaxException e) {
//					current_uri = null;
//				}
//				if (currentPath != null && newPath != null && currentPath.equals(newPath)
//						&& (currentQuery == newQuery || (currentQuery != null && newQuery != null && currentQuery.equals(newQuery)))) {
//					play();
//				} else {
//					stop();
//					Log.e(TAG, "set AV uri = " + uri);
//					m_controlPoint.execute(new SetAVTransportURI(m_avtransportService, uri, null) {
//						@Override
//						public void success(ActionInvocation invocation) {
//							super.success(invocation);
//							m_isBusy = false;
//						}
//
//						@Override
//						public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
//							fireOnFailEvent(invocation.getAction(), response, defaultMsg);
//							m_isBusy = false;
//						}
//					});
//				}
//			}
//		});
//	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void setURIandPlay(final String uri) {
		if (m_controlPoint == null || m_avtransportService == null)
			return;
		m_isBusy = true;
		m_controlPoint.execute(new GetMediaInfo(m_avtransportService) {

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
				m_isBusy = false;
			}

			@Override
			public void received(ActionInvocation invocation, MediaInfo mediaInfo) {
				if (mediaInfo != null && mediaInfo.getCurrentURIMetaData() != null)
					Log.e(TAG, mediaInfo.getCurrentURIMetaData());
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
				if (currentPath != null && newPath != null && currentPath.equals(newPath)
						&& (currentQuery == newQuery || (currentQuery != null && newQuery != null && currentQuery.equals(newQuery)))) {
					play();
				} else {
					Log.e(TAG, "set AV uri = " + uri);
					m_controlPoint.execute(new SetAVTransportURI(m_avtransportService, uri, null) {
						@Override
						public void success(ActionInvocation invocation) {
							super.success(invocation);
							m_controlPoint.execute(new Play(m_avtransportService) {

								@Override
								public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
									Log.e(TAG, "Call fail");
									fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
									m_isBusy = false;
								}

								public void success(ActionInvocation invocation) {
									m_isBusy = false;
									m_state = PLAYING;
								};
							});
						}

						@Override
						public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
							fireOnFailEvent(invocation.getAction(), response, defaultMsg);
							m_isBusy = false;
						}
					});
				}
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void play() {
		if (m_controlPoint == null || m_avtransportService == null)
			return;
		m_isBusy = true;
		Play play = new Play(m_avtransportService) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				m_isBusy = false;
				m_state = PLAYING;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
				m_isBusy = false;
			}

		};

		m_controlPoint.execute(play);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void pause() {
		if (m_controlPoint == null || m_avtransportService == null)
			return;
		m_isBusy = true;
		Pause pause = new Pause(m_avtransportService) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				m_isBusy = false;
				m_state = PAUSE;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
				m_isBusy = false;
			}

		};
		m_controlPoint.execute(pause);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void stop() {
		if (m_controlPoint == null || m_avtransportService == null)
			return;
		m_isBusy = true;
		Stop stop = new Stop(m_avtransportService) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireUpdatePositionEvent(0, 0);
				m_isBusy = false;
				m_state = STOP;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse response, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), response, defaultMsg);
				m_isBusy = false;
			}
		};

		m_controlPoint.execute(stop);
	}

	@Override
	public void addListener(DMRProcessorListner listener) {
		synchronized (m_listeners) {
			if (!m_listeners.contains(listener))
				m_listeners.add(listener);
			if (m_avtransportService == null || m_renderingControl == null)
				fireOnErrorEvent("Cannot get service on this device");
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
		if (m_isBusy)
			return;
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onUpdatePosition(current, max);
			}
		}
	}

	private void fireOnStopedEvent() {
		if (m_isBusy)
			return;
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onStoped();
			}
		}
	}

	private void fireOnPausedEvent() {
		if (m_isBusy)
			return;
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onPaused();
			}
		}
	}

	private void fireOnPlayingEvent() {
		if (m_isBusy)
			return;
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onPlaying();
			}
		}
	}

	private void fireOnEndTrackEvent() {
		if (m_isBusy)
			return;
		synchronized (m_listeners) {
			if (m_listeners.size() > 0)
				for (DMRProcessorListner listener : m_listeners) {
					listener.onEndTrack();
				}
			else {
				m_playlistProcessor.next();
				final PlaylistItem item = m_playlistProcessor.getCurrentItem();
				if (item != null) {
					setURIandPlay(item.getUri());
				}
			}
		}
	}

	private void fireOnErrorEvent(String error) {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onErrorEvent(error);
			}
		}
	}

	@Override
	public void dispose() {
		m_isRunning = false;
		synchronized (m_listeners) {
			m_listeners.clear();
		}
	}

	public void seek(String position) {
		m_isBusy = true;
		Log.e(TAG, "Call seek");
		@SuppressWarnings("rawtypes")
		Seek seek = new Seek(m_avtransportService, SeekMode.REL_TIME, position) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				m_isBusy = false;
				Log.e(TAG, "Call seek complete");
				try {
					Thread.sleep(SEEK_DELAY_INTERVAL);
					m_isBusy = false;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse reponse, String defaultMsg) {
				Log.e(TAG, "Seek fail: " + defaultMsg);
				m_isBusy = false;
			}
		};
		m_controlPoint.execute(seek);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setVolume(int newVolume) {
		m_isBusy = true;
		m_controlPoint.execute(new SetVolume(m_renderingControl, newVolume) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				m_isBusy = false;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg) {
				fireOnFailEvent(invocation.getAction(), operation, defaultMsg);
				m_isBusy = false;
			}
		});
	}

	@Override
	public int getVolume() {
		return m_currentVolume;
	}

	@Override
	public String getName() {
		return m_device != null ? m_device.getDetails().getFriendlyName() : "NULL";
	}

	@Override
	public void setPlaylistProcessor(PlaylistProcessor playlistProcessor) {
		m_playlistProcessor = playlistProcessor;

	}

	@Override
	public void setURIandPlay(PlaylistItem item, boolean proxyMode) {
		// TODO Auto-generated method stub
		
	}

}
