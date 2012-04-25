package com.app.dlna.dmc.processor.async;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.app.dlna.dmc.gui.MainActivity;

public abstract class ProgressDialogAsyncTask<T, U, V> extends AsyncTask<T, U, V> {

	private ProgressDialog m_dlg;

	public ProgressDialogAsyncTask(String message) {
		m_dlg = new ProgressDialog(MainActivity.INSTANCE);
		m_dlg.setTitle(message);
		m_dlg.setCancelable(false);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		m_dlg.show();
	}

	@Override
	protected abstract V doInBackground(T... params);

	protected void onPostExecute(V result) {
		m_dlg.dismiss();
	};

}
