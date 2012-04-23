package com.app.dlna.dmc.gui.customview.adapter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.ImageItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.cling.support.model.item.VideoItem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import app.dlna.controller.v4.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.processor.cache.Cache;
import com.app.dlna.dmc.processor.interfaces.PlaylistProcessor;
import com.app.dlna.dmc.processor.playlist.PlaylistItem;
import com.app.dlna.dmc.utility.Utility;

public class CustomArrayAdapter extends ArrayAdapter<AdapterItem> {
	private static final String TAG = CustomArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private Map<String, Bitmap> m_cacheDMSIcon;
	private boolean m_cancelPreparing;
	public static final int MAX_SIZE = 48;

	public CustomArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheDMSIcon = new HashMap<String, Bitmap>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView instanceof ProgressBar) {
			convertView = m_inflater.inflate(R.layout.lvitem_generic_item, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final AdapterItem object = getItem(position);

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		holder.action.setTag(new Integer(position));
		if (object.getData() instanceof Device)
			initDeviceItem((Device) object.getData(), holder);
		else if (object.getData() instanceof DIDLObject) {
			DIDLObject didlObject = (DIDLObject) object.getData();
			if (didlObject.getId().equals("-1")) {
				return m_inflater.inflate(R.layout.lvitem_loadmoreitem, null);
			} else {
				initDIDLObject(didlObject, holder);
			}
		} else if (object.getData() instanceof PlaylistItem) {
			initPlaylistItem((PlaylistItem) object.getData(), holder, position);
		} else if (object.getData() instanceof PlaylistProcessor) {
			initPlaylistProcessorItem((PlaylistProcessor) object.getData(), holder, position);
		}

		return convertView;
	}

	private void initPlaylistProcessorItem(PlaylistProcessor data, ViewHolder holder, int position) {
		holder.action.setVisibility(View.GONE);
		holder.name.setText(data.getPlaylistName());
		holder.desc.setText("");
		holder.icon.setImageResource(R.drawable.ic_playlist);
		holder.playing.setVisibility(View.GONE);
	}

	@SuppressWarnings("rawtypes")
	private void initDeviceItem(final Device device, final ViewHolder holder) {
		holder.action.setVisibility(View.GONE);
		holder.playing.setVisibility(View.GONE);
		if (device instanceof LocalDevice)
			holder.name.setText("Local Device");
		else
			holder.name.setText(device.getDetails().getFriendlyName());
		final String udn = device.getIdentity().getUdn().getIdentifierString();

		if (device instanceof RemoteDevice) {
			holder.desc.setText(((RemoteDevice) device).getIdentity().getDescriptorURL().getAuthority());
		} else {
			holder.desc.setText("Local device");
		}

		if (m_cacheDMSIcon.containsKey(udn)) {
			holder.icon.setImageBitmap(m_cacheDMSIcon.get(udn));
		} else {
			if (device instanceof RemoteDevice) {
				try {
					final Icon[] icons = device.getIcons();
					if (icons != null && icons.length > 0 && icons[0] != null && icons[0].getUri() != null) {
						loadDeviceIcon(device, holder, udn, icons);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {
				holder.icon.setImageResource(R.drawable.ic_launcher);
			}
		}

	}

	@SuppressWarnings("rawtypes")
	private void loadDeviceIcon(final Device device, final ViewHolder holder, final String udn, final Icon[] icons) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final RemoteDevice remoteDevice = (RemoteDevice) device;

					String urlString = remoteDevice.getIdentity().getDescriptorURL().getProtocol() + "://"
							+ remoteDevice.getIdentity().getDescriptorURL().getAuthority() + icons[0].getUri().toString();
					URL url = new URL(urlString);
					final Bitmap icon = BitmapFactory.decodeStream(url.openConnection().getInputStream());
					m_cacheDMSIcon.put(udn, icon);
					MainActivity.INSTANCE.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							CustomArrayAdapter.this.notifyDataSetChanged();
						}
					});

				} catch (Exception ex) {
					MainActivity.INSTANCE.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							holder.icon.setImageBitmap(null);
						}
					});
					ex.printStackTrace();
				}
			}
		}).start();
	}

	private void initDIDLObject(DIDLObject object, ViewHolder holder) {
		holder.name.setText(object.getTitle());
		if (object.getId().equals("-1")) {
			holder.icon.setVisibility(View.GONE);
			holder.desc.setText("");
			holder.action.setVisibility(View.GONE);
			holder.playing.setVisibility(View.GONE);
			return;
		}
		holder.icon.setVisibility(View.VISIBLE);
		if (object instanceof Container) {
			holder.icon.setImageResource(R.drawable.ic_didlobject_container);
			holder.action.setVisibility(View.GONE);
			holder.playing.setVisibility(View.GONE);
			int childCount = ((Container) object).getChildCount() != null ? ((Container) object).getChildCount() : 1;
			String childCountStr = "";
			if (childCount == 0)
				childCountStr = "empty";
			else if (childCount == 1)
				childCountStr = "1 child";
			else
				childCountStr = childCount + " childs";
			holder.desc.setText(childCountStr);
		} else {
			if (object instanceof MusicTrack) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
			} else if (object instanceof VideoItem) {
				holder.icon.setImageResource(R.drawable.ic_didlobject_video);
			} else if (object instanceof ImageItem) {
				Utility.loadImageItemThumbnail(holder.icon, object.getResources().get(0).getValue(), Cache.getBitmapCache(),
						MAX_SIZE);
			} else {
				holder.icon.setImageResource(R.drawable.ic_didlobject_unknow);
			}
			if (object.getResources().size() > 0) {
				if (object.getResources().get(0) != null && object.getResources().get(0).getSize() != null)
					holder.desc.setText(Utility.convertSizeToString(object.getResources().get(0).getSize()));
			}
			if (object instanceof Item) {
				holder.action.setVisibility(View.VISIBLE);
				if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().containsUrl(object.getResources().get(0).getValue())) {
					holder.action.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_remove));
				} else {
					holder.action.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_add));
				}
				if (object.getResources().get(0).getValue()
						.equals(MainActivity.UPNP_PROCESSOR.getDMRProcessor().getCurrentTrackURI())) {
					holder.playing.setVisibility(View.VISIBLE);
				} else {
					holder.playing.setVisibility(View.GONE);
				}
			} else {
				holder.action.setVisibility(View.GONE);
			}
		}
	}

	private void initPlaylistItem(PlaylistItem object, ViewHolder holder, int position) {
		holder.name.setText(object.getTitle());
		switch (object.getType()) {
		case AUDIO:
			holder.icon.setImageResource(R.drawable.ic_didlobject_audio);
			break;
		case VIDEO:
			holder.icon.setImageResource(R.drawable.ic_didlobject_video);
			break;
		case IMAGE:
			Utility.loadImageItemThumbnail(holder.icon, object.getUri(), Cache.getBitmapCache(), MAX_SIZE);
			break;
		default:
			holder.icon.setImageResource(R.drawable.ic_didlobject_unknow);
			break;
		}
		holder.action.setVisibility(View.VISIBLE);
		holder.action.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_btn_remove));
		if (object.getUri().equals(MainActivity.UPNP_PROCESSOR.getDMRProcessor().getCurrentTrackURI())) {
			holder.playing.setVisibility(View.VISIBLE);
		} else {
			holder.playing.setVisibility(View.GONE);
		}
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.name = (TextView) view.findViewById(R.id.name);
		viewHolder.desc = (TextView) view.findViewById(R.id.desc);
		viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
		viewHolder.action = (ImageView) view.findViewById(R.id.action);
		viewHolder.action.setOnClickListener(m_actionClick);
		viewHolder.playing = (ImageView) view.findViewById(R.id.playing);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		TextView desc;
		TextView name;
		ImageView icon;
		ImageView action;
		ImageView playing;
	}

	@Override
	public void clear() {
		super.clear();
	}

	public void prepareImageItemCache(final List<? extends DIDLObject> objects) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				m_cancelPreparing = false;
//				for (DIDLObject didlObject : objects) {
//
//					if (m_cancelPreparing) {
//						Log.e(TAG, "Cancel preparing");
//						break;
//					}
//
//					if (didlObject instanceof ImageItem) {
//						synchronized (Cache.getBitmapCache()) {
//							String imageUrl = didlObject.getResources().get(0).getValue();
//							try {
//								Cache.getBitmapCache().put(imageUrl, Utility.getBitmapFromURL(imageUrl, MAX_SIZE));
//							} catch (MalformedURLException e) {
//								e.printStackTrace();
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//			}
//		}).start();
	}

	public void cancelPrepareImageCache() {
		m_cancelPreparing = true;
	}

	private OnClickListener m_actionClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() == null)
				return;
			int position = (Integer) v.getTag();
			Object item = getItem(position).getData();
			if (item instanceof PlaylistItem) {
				MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().removeItem((PlaylistItem) item);
				remove(getItem(position));
			} else if (item instanceof DIDLObject) {
				final DIDLObject object = (DIDLObject) item;
				if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor() != null)
					if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().containsUrl(object.getResources().get(0).getValue())) {
						if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().removeDIDLObject(object) != null)
							updateSingleView(v, position);
					} else {
						if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().addDIDLObject(object) != null)
							updateSingleView(v, position);
					}
			}

		}
	};

	public void updateSingleView(View v, int position) {
		if (v.getParent().getParent().getParent() instanceof ListView) {
			ListView list = (ListView) v.getParent().getParent().getParent();
			int start = list.getFirstVisiblePosition();
			int end = list.getLastVisiblePosition();
			for (int i = start, j = end; i <= j; i++)
				if (position == i) {
					View view = list.getChildAt(i - start);
					list.getAdapter().getView(i, view, list);
					break;
				}
		}
	}

	public void updateSingleView(ListView list, int position) {
		int start = list.getFirstVisiblePosition();
		int end = list.getLastVisiblePosition();
		for (int i = start, j = end; i <= j; i++)
			if (position == i) {
				View view = list.getChildAt(i - start);
				list.getAdapter().getView(i, view, list);
				break;
			}
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	public void notifyVisibleItemChanged(ListView list) {
		int start = list.getFirstVisiblePosition() < getCount() ? list.getFirstVisiblePosition() : getCount() - 1;
		int end = list.getLastVisiblePosition() < getCount() ? list.getLastVisiblePosition() : getCount() - 1;
		for (int i = start, j = end; i <= j; i++) {
			View view = list.getChildAt(i - start);
			if (0 <= i && i < getCount())
				list.getAdapter().getView(i, view, list);
		}
	}
}
