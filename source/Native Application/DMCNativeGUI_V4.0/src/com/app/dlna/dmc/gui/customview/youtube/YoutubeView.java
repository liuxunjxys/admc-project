package com.app.dlna.dmc.gui.customview.youtube;

import java.util.List;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.gui.customview.adapter.CustomArrayAdapter;
import com.app.dlna.dmc.processor.impl.YoutubeProcessorImpl;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor;
import com.app.dlna.dmc.processor.interfaces.YoutubeProcessor.IYoutubeProcessorListener;
import com.app.dlna.dmc.processor.youtube.YoutubeItem;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import app.dlna.controller.v4.R;

public class YoutubeView extends LinearLayout {

	protected static final String TAG = YoutubeView.class.getName();
	private ListView m_listView;
	private EditText m_ed_query;
	private ImageView m_btn_search;
	private CustomArrayAdapter m_adapter;
	private YoutubeProcessor m_youtubeProcessor;

	public YoutubeView(Context context) {
		super(context);
		((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.cv_youtube, this);
		m_listView = (ListView) findViewById(R.id.listView);
		m_adapter = new CustomArrayAdapter(context, 0);
		m_listView.setAdapter(m_adapter);
		m_listView.setOnItemClickListener(m_itemClick);
		m_listView.setOnItemLongClickListener(m_itemLongClick);
		m_ed_query = (EditText) findViewById(R.id.ed_query);
		m_btn_search = (ImageView) findViewById(R.id.btn_search);
		m_btn_search.setOnClickListener(m_onSearchClick);

		m_youtubeProcessor = new YoutubeProcessorImpl();
	}

	private OnClickListener m_onSearchClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String query = m_ed_query.getText().toString();
			m_youtubeProcessor.executeQuery(query, m_youtubeListener);
		}
	};

	private OnItemClickListener m_itemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
			m_youtubeProcessor.getDirectLink(((YoutubeItem) m_adapter.getItem(position).getData()).getId(), m_youtubeListener);
		}
	};
	private OnItemLongClickListener m_itemLongClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg3) {
			return false;
		}
	};

	private IYoutubeProcessorListener m_youtubeListener = new IYoutubeProcessorListener() {

		@Override
		public void onStartPorcess() {

		}

		@Override
		public void onSearchComplete(final List<YoutubeItem> result) {
			Log.i(TAG, "result count = " + result);
			MainActivity.INSTANCE.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_adapter.clear();
					for (YoutubeItem item : result)
						m_adapter.add(new AdapterItem(item));
				}
			});

		}

		@Override
		public void onGetDirectLinkComplete(String result) {

		}

		@Override
		public void onFail(Exception ex) {

		}
	};

}
