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
import android.widget.ImageView;
import android.widget.LinearLayout;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.customview.localnetwork.HomeNetworkView;
import com.app.dlna.dmc.gui.customview.playlist.PlaylistView;
import com.app.dlna.dmc.gui.customview.renderer.RendererCompactView;

public class LibraryActivity extends Activity {
	private ViewPager m_pager;
	private HomeNetworkView m_homeNetwork;
	private View m_internet;
	private PlaylistView m_playlist;
	private RendererCompactView m_rendererCompactView;
	private String[] m_pagerTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		m_homeNetwork = new HomeNetworkView(this);
		m_internet = new LinearLayout(this);
		m_playlist = new PlaylistView(this);

		setContentView(R.layout.activity_library);
		m_pagerTitle = getResources().getStringArray(R.array.libray_pager_list);
		m_rendererCompactView = (RendererCompactView) findViewById(R.id.cv_compact_dmr);

		m_pager = (ViewPager) findViewById(R.id.viewPager);
		m_pager.setOnPageChangeListener(m_onPageChangeListener);
		m_pager.setAdapter(m_pagerAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		m_homeNetwork.updateListView();
	}

	public void showRendererCompactView() {
		Animation animation = AnimationUtils.loadAnimation(LibraryActivity.this, R.anim.compactrenderer_slidein);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				m_rendererCompactView.setVisibility(View.VISIBLE);
				m_rendererCompactView.updateListRenderer();
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
		Animation animation = AnimationUtils.loadAnimation(LibraryActivity.this, R.anim.compactrenderer_slideout);
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

	public void onShowHideClick(View view) {
		if (isCompactRendererShowing()) {
			hideRendererCompactView();
			((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_navigate_up));
		} else {
			showRendererCompactView();
			((ImageView) view).setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_navigate_down));
		}
	}

	private OnPageChangeListener m_onPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			switch (position) {
			case 0:
				m_homeNetwork.updateListView();
				break;
			case 1:
				m_playlist.preparePlaylist();
				break;
			case 2:
				break;

			default:
				break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	PagerAdapter m_pagerAdapter = new PagerAdapter() {
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
				((ViewPager) container).addView(m_playlist);
				return m_playlist;
			} else if (position == 2) {
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
			return m_pagerTitle[position];
		}

		@Override
		public int getCount() {
			return m_pagerTitle.length;
		}
	};
}
