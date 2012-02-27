package com.app.dlna.dmc.processor.localdevice.renderer;

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.AVTransportException;
import org.teleal.cling.support.avtransport.AbstractAVTransportService;
import org.teleal.cling.support.model.DeviceCapabilities;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.support.model.TransportSettings;

public class LocalAVTransportService extends AbstractAVTransportService {

	@Override
	public void setAVTransportURI(UnsignedIntegerFourBytes instanceId, String currentURI, String currentURIMetaData) throws AVTransportException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId, String nextURI, String nextURIMetaData) throws AVTransportException {
		// Not support
	}

	@Override
	public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeviceCapabilities getDeviceCapabilities(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransportSettings getTransportSettings(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stop(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void play(UnsignedIntegerFourBytes instanceId, String speed) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void record(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void next(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void previous(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPlayMode(UnsignedIntegerFourBytes instanceId, String newPlayMode) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId, String newRecordQualityMode) throws AVTransportException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCurrentTransportActions(UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		// TODO Auto-generated method stub
		return null;
	}

}
