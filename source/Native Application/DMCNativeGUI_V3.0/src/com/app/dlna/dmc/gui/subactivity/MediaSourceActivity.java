package com.app.dlna.dmc.gui.subactivity;

import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import app.dlna.controller.R;

public class MediaSourceActivity extends Activity {
	private ViewPager m_pager;
	private View m_homeNetwork;
	private View m_internet;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_homeNetwork = new HomeNetworkView(this);
		m_internet = new LinearLayout(this);
		setContentView(R.layout.mediasource_activity);
		m_pager = (ViewPager) findViewById(R.id.viewPager);

		m_pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		PagerAdapter adapter = new PagerAdapter() {
			@Override
			public void destroyItem(ViewGroup container, int position, Object view) {
				((ViewPager) container).removeView((View) view);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				if (position == 0) {
					((ViewPager) container).addView(m_homeNetwork);
					return m_homeNetwork;
				} else if (position == 1) {
					((ViewPager) container).addView(m_internet);
					return m_internet;
				}
				return null;
			}

			@Override
			public boolean isViewFromObject(View view, Object key) {
				return view == key;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return new String[] { "Home Network", "Internet" }[position];
			}

			@Override
			public int getCount() {
				return 2;
			}
		};
		m_pager.setAdapter(adapter);
	}
}
