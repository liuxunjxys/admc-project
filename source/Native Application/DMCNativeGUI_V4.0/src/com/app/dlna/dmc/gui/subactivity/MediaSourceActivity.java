package com.app.dlna.dmc.gui.subactivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;
import com.app.dlna.dmc.gui.customview.renderer.RendererCompactView;

public class MediaSourceActivity extends Activity {
	private ViewPager m_pager;
	private HomeNetworkView m_homeNetwork;
	private View m_internet;
	private RendererCompactView m_rendererCompactView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_homeNetwork = new HomeNetworkView(this);
		m_internet = new LinearLayout(this);
		setContentView(R.layout.mediasource_activity);
		m_rendererCompactView = (RendererCompactView) findViewById(R.id.cv_compact_dmr);
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

	@Override
	protected void onResume() {
		super.onResume();
		m_homeNetwork.updateListView();
	}

	public void showRendererCompactView() {
		Animation animation = AnimationUtils.loadAnimation(MediaSourceActivity.this, R.anim.compactrenderer_slidein);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_rendererCompactView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}
		});
		m_rendererCompactView.startAnimation(animation);

	}

	public void hideRendererCompactView() {
		Animation animation = AnimationUtils.loadAnimation(MediaSourceActivity.this, R.anim.compactrenderer_slideout);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_rendererCompactView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				m_rendererCompactView.setVisibility(View.GONE);
			}
		});
		m_rendererCompactView.startAnimation(animation);
	}

	public boolean isCompactRendererShowing() {
		return m_rendererCompactView.getVisibility() == View.VISIBLE;
	}
}
