package com.app.dlna.dmc.gui.customview.localnetwork;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import app.dlna.controller.R;

import com.app.dlna.dmc.gui.MainActivity;
import com.app.dlna.dmc.gui.customview.adapter.AdapterItem;
import com.app.dlna.dmc.utility.Utility;

public class HomeNetworkArrayAdapter extends ArrayAdapter<AdapterItem> {
	protected static final int IMAGE_MAX_SIZE = 48;
	protected static final String TAG = HomeNetworkArrayAdapter.class.getName();
	private LayoutInflater m_inflater = null;
	private Map<String, Bitmap> m_cacheDMSIcon;
	private Map<String, Bitmap> m_cacheImageItem;
	private boolean m_cancelPreparing;

	public HomeNetworkArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		m_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		m_cacheDMSIcon = new HashMap<String, Bitmap>();
		m_cacheImageItem = new HashMap<String, Bitmap>();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null || convertView instanceof ProgressBar) {
			convertView = m_inflater.inflate(R.layout.lvitem_localnetwork, null, false);
		}
		if (convertView.getTag() == null) {
			setViewHolder(convertView);
		}
		final AdapterItem object = getItem(position);

		final ViewHolder holder = (ViewHolder) convertView.getTag();
		if (object.getData() instanceof Device)
			initDeviceItem((Device) object.getData(), holder);
		else if (object.getData() instanceof DIDLObject) {
			DIDLObject didlObject = (DIDLObject) object.getData();
			if (didlObject.getId().equals("-1")) {
				return m_inflater.inflate(R.layout.lvitem_loadmoreitem, null);
			} else {
				initDIDLObject(didlObject, holder);
			}
		}

		return convertView;
	}

	@SuppressWarnings("rawtypes")
	private void initDeviceItem(final Device device, final ViewHolder holder) {
		holder.checked.setVisibility(View.GONE);
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
							HomeNetworkArrayAdapter.this.notifyDataSetChanged();
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

	private void loadImageItemThumbnail(final ImageView image, final String imageUrl) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (m_cacheImageItem.containsKey(imageUrl)) {
						MainActivity.INSTANCE.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								try {
									synchronized (m_cacheImageItem) {
										image.setImageBitmap(m_cacheImageItem.get(imageUrl));
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
					} else {
						final Bitmap bm = getBitmapFromURL(imageUrl);
						synchronized (m_cacheImageItem) {
							m_cacheImageItem.put(imageUrl, bm);
						}
						MainActivity.INSTANCE.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								try {
									image.setImageBitmap(bm);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private Bitmap getBitmapFromURL(final String imageUrl) throws IOException, MalformedURLException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		byte[] buffer = IOUtils.toByteArray((InputStream) new URL(imageUrl).getContent());
		BitmapFactory.decodeByteArray(buffer, 0, buffer.length, o);
		int scale = 1;
		if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
			scale = (int) Math.pow(2,
					(int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
		}

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;

		return BitmapFactory.decodeByteArray(buffer, 0, buffer.length, o2);
	}

	private void initDIDLObject(DIDLObject object, ViewHolder holder) {
		holder.name.setText(object.getTitle());
		if (object.getId().equals("-1")) {
			holder.icon.setVisibility(View.GONE);
			holder.desc.setText("");
			holder.checked.setVisibility(View.GONE);
			return;
		}
		holder.icon.setVisibility(View.VISIBLE);
		if (object instanceof Container) {
			holder.icon.setImageResource(R.drawable.ic_didlobject_container);
			holder.checked.setVisibility(View.GONE);
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
				holder.icon.setImageResource(R.drawable.ic_didlobject_image);
				loadImageItemThumbnail(holder.icon, object.getResources().get(0).getValue());
			} else {
				holder.icon.setImageResource(R.drawable.ic_didlobject_unknow);
			}
			if (object.getResources().size() > 0) {
				if (object.getResources().get(0) != null && object.getResources().get(0).getSize() != null)
					holder.desc.setText(Utility.convertSizeToString(object.getResources().get(0).getSize()));
			}
			if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor() != null)
				if (object instanceof Item)
					if (MainActivity.UPNP_PROCESSOR.getPlaylistProcessor().containsUrl(object.getResources().get(0).getValue())) {
						holder.checked.setVisibility(View.VISIBLE);
					} else {
						holder.checked.setVisibility(View.GONE);
					}
				else {
					holder.checked.setVisibility(View.GONE);
				}
		}
	}

	public void setViewHolder(View view) {
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.name = (TextView) view.findViewById(R.id.name);
		viewHolder.desc = (TextView) view.findViewById(R.id.desc);
		viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
		viewHolder.checked = (ImageView) view.findViewById(R.id.checked);
		view.setTag(viewHolder);
	}

	private class ViewHolder {
		TextView desc;
		TextView name;
		ImageView icon;
		ImageView checked;
	}

	@Override
	public void clear() {
		super.clear();
		synchronized (m_cacheImageItem) {
			m_cacheImageItem.clear();
		}
	}

	public void prepareImageItemCache(final List<? extends DIDLObject> objects) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				m_cancelPreparing = false;
				for (DIDLObject didlObject : objects) {

					if (m_cancelPreparing) {
						Log.e(TAG, "Cancel preparing");
						break;
					}

					if (didlObject instanceof ImageItem) {
						synchronized (m_cacheImageItem) {
							String imageUrl = didlObject.getResources().get(0).getValue();
							try {
								m_cacheImageItem.put(imageUrl, getBitmapFromURL(imageUrl));
							} catch (MalformedURLException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}).start();
	}

	public void cancelPrepareImageCache() {
		m_cancelPreparing = true;
	}
}
