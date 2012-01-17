package com.app.dlna.dmc.processor.impl;

import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.SeekMode;

import android.util.Log;

import com.app.dlna.dmc.processor.interfaces.IDMRProcessor;

public class DMRProcessorImpl implements IDMRProcessor {

	private static final int SEEK_DELAY_INTERVAL = 1000;
	private static final int UPDATE_INTERVAL = 500;
	private static final String TAG = DMRProcessorImpl.class.getName();
	private RemoteDevice m_device;
	private ControlPoint m_controlPoint;
	@SuppressWarnings("rawtypes")
	private Service m_service = null;
	private List<DMRProcessorListner> m_listeners;
	private boolean m_isRunning = true;
	private boolean m_isPlaying = false;
	private boolean m_canUpdatePosition = true;
	private Thread m_updateThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (m_isRunning) {
				if (m_isPlaying) {
					@SuppressWarnings("rawtypes")
					GetPositionInfo getPositionInfo = new GetPositionInfo(m_service) {

						@Override
						public void failure(ActionInvocation invocation, UpnpResponse response, String cause) {
							m_isRunning = false;
						}

						@Override
						public void received(ActionInvocation invocation, PositionInfo positionInfo) {
							Log.e(TAG, "PositionInfo = " + positionInfo.toString() + ";Percent = " + positionInfo.getElapsedPercent());
							fireUpdatePositionEvent(positionInfo.getTrackElapsedSeconds(), positionInfo.getTrackDurationSeconds());
						}
					};
					m_controlPoint.execute(getPositionInfo);
					try {
						Thread.sleep(UPDATE_INTERVAL);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	});

	public DMRProcessorImpl(RemoteDevice dmr, ControlPoint controlPoint) {
		m_device = dmr;
		m_controlPoint = controlPoint;
		m_service = m_device.findService(new ServiceType("schemas-upnp-org", "AVTransport"));
		if (m_service == null)
			Log.e(TAG, "service = null");
		else
			Log.e(TAG, "Service != null");

		m_listeners = new ArrayList<IDMRProcessor.DMRProcessorListner>();
		m_updateThread.start();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void setURI(String uri) {
		SetAVTransportURI setAVTransportURI = new SetAVTransportURI(m_service, uri) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireOnCompleteEvent(invocation.getAction());
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse arg1, String cause) {
				fireOnFailEvent(invocation.getAction(), cause);
			}
		};

		m_controlPoint.execute(setAVTransportURI);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void play() {
		Play play = new Play(m_service) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireOnCompleteEvent(invocation.getAction());
				m_isPlaying = true;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse arg1, String cause) {
				fireOnFailEvent(invocation.getAction(), cause);
			}

		};

		m_controlPoint.execute(play);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void pause() {
		Pause pause = new Pause(m_service) {

			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireOnCompleteEvent(invocation.getAction());
				m_isPlaying = false;
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse arg1, String cause) {
				fireOnFailEvent(invocation.getAction(), cause);
			}

		};
		m_controlPoint.execute(pause);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void stop() {
		Stop stop = new Stop(m_service) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				fireOnCompleteEvent(invocation.getAction());
				m_isPlaying = false;
				fireUpdatePositionEvent(0, 0);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse arg1, String cause) {
				fireOnFailEvent(invocation.getAction(), cause);
			}
		};

		m_controlPoint.execute(stop);
	}

	@Override
	public void addListener(DMRProcessorListner listener) {
		synchronized (m_listeners) {
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
	private void fireOnCompleteEvent(Action action) {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onActionComplete(action);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void fireOnFailEvent(Action action, String message) {
		synchronized (m_listeners) {
			for (DMRProcessorListner listener : m_listeners) {
				listener.onActionFail(action, message);
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

	@Override
	public void dispose() {
		m_isRunning = false;
		m_updateThread.interrupt();
	}

	@Override
	public void seek(int position) {
		@SuppressWarnings("rawtypes")
		Seek seek = new Seek(m_service, SeekMode.ABS_COUNT, String.valueOf(position)) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse reponse, String cause) {
				Log.e(TAG, "Seek fail: " + cause);
			}
		};
		m_controlPoint.execute(seek);
	}

	public void seek(String position) {
		Log.e(TAG, "Call seek");
		m_canUpdatePosition = false;
		@SuppressWarnings("rawtypes")
		Seek seek = new Seek(m_service, SeekMode.REL_TIME, position) {
			@Override
			public void success(ActionInvocation invocation) {
				super.success(invocation);
				Log.e(TAG, "Call seek complete");
				// for update realtime
				try {
					Thread.sleep(SEEK_DELAY_INTERVAL);
					m_canUpdatePosition = true;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void failure(ActionInvocation invocation, UpnpResponse reponse, String cause) {
				Log.e(TAG, "Seek fail: " + cause);
				m_isPlaying = true;
			}
		};
		m_controlPoint.execute(seek);
	}

}
