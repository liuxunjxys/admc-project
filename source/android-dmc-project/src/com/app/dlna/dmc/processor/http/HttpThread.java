package com.app.dlna.dmc.processor.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class HttpThread extends Thread {
	private static final String TAG = "HttpThreadLog";
	private int m_port = -1;

	public HttpThread(int port) {
		m_port = port;
	}

	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(m_port);
			while (true) {
				final Socket client = server.accept();
				System.out.println(client.getInetAddress().getHostAddress());
				System.out.println(client.getRemoteSocketAddress().toString());
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							final BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
							List<String> rawrequest = new ArrayList<String>();
							String line = null;
							String request = null;
							String requesttype = null;
							long range = 0;
							Log.i(TAG, "<--START HEADER-->");
							while ((line = br.readLine()) != null && (line.length() != 0)) {
								Log.i(TAG, line);
								rawrequest.add(line);
								if (line.contains("GET")) {
									requesttype = "GET";
									request = getRequestFilePath(line);
								} else if (line.contains("HEAD")) {
									requesttype = "HEAD";
									request = getRequestFilePath(line);
								} else if (line.contains("Range")) {
									String strrange = line.substring(13, line.lastIndexOf('-'));
									range = Long.valueOf(strrange);
								}
							}
							Log.i(TAG, "<--END HEADER-->");
							String filename = null;
							if (request != null) {
								filename = URLDecoder.decode(request, "ASCII");
								Log.e(TAG, "file name = " + filename);
								if (filename != null && filename.length() != 0) {
									Utility.handleClientRequest(client, requesttype, range, filename);
								}
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					private String getRequestFilePath(String line) {
						String request;
						String[] linepatthern = line.split(" ");
						request = linepatthern[1];
						return request;
					}

				}).start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
