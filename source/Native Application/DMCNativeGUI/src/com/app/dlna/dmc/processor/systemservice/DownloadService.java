package com.app.dlna.dmc.processor.systemservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DownloadService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

}
