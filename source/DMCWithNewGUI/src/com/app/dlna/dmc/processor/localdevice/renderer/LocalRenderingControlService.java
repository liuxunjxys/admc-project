package com.app.dlna.dmc.processor.localdevice.renderer;

import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;

public class LocalRenderingControlService extends AbstractAudioRenderingControl {

	@Override
	public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
		// TODO Auto-generated method stub
		return new UnsignedIntegerTwoBytes(100);
	}

	@Override
	public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
		// TODO Auto-generated method stub

	}

}
