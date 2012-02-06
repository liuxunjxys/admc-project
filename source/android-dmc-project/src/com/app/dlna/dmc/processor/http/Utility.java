package com.app.dlna.dmc.processor.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.teleal.cling.support.messagebox.model.DateTime;

public class Utility {

	private static final int BUFFERSIZE = 32768;
	public static String NEWLINE = "\r\n";

	public static String getBridgeURL(String youtubeURLstr) throws MalformedURLException, IOException {

		URL sourceURL = new URL(youtubeURLstr);
		String contentURLstr = "";

		BufferedReader br = new BufferedReader(new InputStreamReader(sourceURL.openStream()));
		String line = null;

		while ((line = br.readLine()) != null) {
			if (line.contains("img.src")) {
				contentURLstr = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"')).replace("\\u0026", "&").replace("\\", "")
						.replace("generate_204", "videoplayback");
				break;
			}
		}

		br.close();

		return contentURLstr;
	}

	public static String makeGETrequest(String url) throws MalformedURLException {
		String result = "";
		URL requestURL = new URL(url);
		result += "GET " + requestURL.getFile() + " HTTP/1.1";
		result += NEWLINE;
		result += "Host: " + requestURL.getHost() + ":" + (requestURL.getPort() > 0 ? requestURL.getPort() : 80);
		result += NEWLINE;
		result += "Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
		result += NEWLINE;
		result += "Connection: close";
		result += NEWLINE;
		result += NEWLINE;

		return result;
	}

	public static String makeHttp200Reponse(String filename) {
		String result = "";

		File f = new File(filename);

		result += "HTTP/1.1 200 OK";
		result += NEWLINE;
		result += "Content-Type: audio/x-ms-wma";
		result += NEWLINE;
		result += "Content-Length: " + f.length();
		result += NEWLINE;
		result += "Accept-Ranges: bytes";
		result += NEWLINE;
		result += "Date: " + DateTime.getCurrentDate().toString();
		result += NEWLINE;
		result += "Last-Modified: " + new Date(f.lastModified()).toString();
		result += NEWLINE;
		result += "EXT:";
		result += NEWLINE;
		result += "Connection: close";
		result += NEWLINE;
		result += "Server: SimpleDMS";
		result += NEWLINE;
		result += NEWLINE;

		return result;
	}

	public static String makeHttp206Reponse(String filename, long range) {
		String result = "";
		File f = new File(filename);

		result += "HTTP/1.1 206 Partial Content";
		result += NEWLINE;
		result += "Content-Type: audio/mpeg";
		result += NEWLINE;
		result += "Content-Length: " + (f.length() - range);
		result += NEWLINE;
		result += "Content-Range: bytes " + (range + "-" + (f.length() - 1 + "-" + f.length()));
		result += NEWLINE;
		result += "Accept-Ranges: bytes";
		result += NEWLINE;
		result += "Date: " + DateTime.getCurrentDate().toString();
		result += NEWLINE;
		result += "Last-Modified: " + new Date(f.lastModified()).toString();
		result += NEWLINE;
		result += "EXT:";
		result += NEWLINE;
		result += "Connection: close";
		result += NEWLINE;
		result += "Server: SimpleDMS";
		result += NEWLINE;
		result += NEWLINE;

		return result;
	}

	public static void handleClientRequest(final Socket client, String requesttype, long range, String filename) {

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			if (range == 0) {
				bw.write(Utility.makeHttp200Reponse(filename));
				bw.flush();
			} else {
				bw.write(Utility.makeHttp206Reponse(filename, range));
				bw.flush();
			}

			if (requesttype.compareTo("GET") == 0) {

				DataInputStream dis = new DataInputStream(new FileInputStream(filename));
				System.out.println("Skip " + range + " bytes");
				long skippedbytes = dis.skip(range);
				System.out.println(skippedbytes + " bytes is skipped");
				DataOutputStream dos = new DataOutputStream(client.getOutputStream());

				byte[] b = new byte[BUFFERSIZE];
				long count = range;
				int bytesread = 0;
				while (true) {
					bytesread = dis.read(b);
					count += bytesread;
					dos.write(b, 0, bytesread);
					// System.out.println(String.valueOf(count * 100 / new File(filename).length()) + "%");
					// System.out.println(bytesread);
					// System.out.println(count);
					if (count == new File(filename).length())
						break;
				}
				dos.flush();
				System.out.println(count);

			}

		} catch (IOException e) {
			System.out.println("Client close connection");
		}
	}

	public static List<String> getYoutubeReponse(String youtubeURLstr, List<String> request) {
		List<String> reponse = new ArrayList<String>();
		try {
			String contentURLstr = getTrueURLString(youtubeURLstr);
			URL bridgeURL = new URL(contentURLstr);

			Socket socket = new Socket(bridgeURL.getHost(), bridgeURL.getPort() > 0 ? bridgeURL.getPort() : 80);
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			System.out.println("Bridge URL: " + bridgeURL.toString());
			for (String requestString : request) {
				if (requestString.contains("GET")) {
					requestString = "GET " + bridgeURL.getFile() + " HTTP/1.1";
				} else if (requestString.contains("HEAD")) {
					requestString = "HEAD " + bridgeURL.getFile() + " HTTP/1.1";
				}
				if (requestString.contains("Host")) {
					requestString = "Host: " + bridgeURL.getHost() + ":" + (bridgeURL.getPort() > 0 ? bridgeURL.getPort() : 80);
				}
				bw.write(requestString);
				bw.newLine();
				System.out.println("Request:" + requestString);
			}

			bw.newLine();
			bw.flush();

			String line = null;
			String reallocation = "";
			while ((line = br.readLine()) != null && line.length() != 0) {
				System.out.println("Youtube reponse:" + line);
				if (line.contains("Location")) {
					reallocation = line.substring(line.indexOf(' ') + 1);
				}
				reponse.add(line);
			}
			System.out.println(reallocation);
			br.close();
			bw.close();
			socket.close();
		} catch (Exception ex) {

		}
		return reponse;
	}

	public static DataInputStream getYoutubeStream(String youtubeURLstr, List<String> request) {
		DataInputStream reponse = null;
		// try {
		// String contentURLstr = getTrueURLString(youtubeURLstr);
		// URL bridgeURL = new URL(contentURLstr);
		//
		// Socket socket = new Socket(bridgeURL.getHost(), bridgeURL.getPort() > 0 ? bridgeURL.getPort() : 80);
		// BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		// System.out.println("Bridge URL: " + bridgeURL.toString());
		// for (String requestString : request) {
		// if (requestString.contains("GET")) {
		// requestString = "GET " + bridgeURL.getFile() + " HTTP/1.1";
		// } else if (requestString.contains("HEAD")) {
		// requestString = "HEAD " + bridgeURL.getFile() + " HTTP/1.1";
		// }
		// if (requestString.contains("Host")) {
		// requestString = "Host: " + bridgeURL.getHost() + ":" + (bridgeURL.getPort() > 0 ? bridgeURL.getPort() : 80);
		// }
		// bw.write(requestString);
		// bw.newLine();
		// System.out.println("Request:" + requestString);
		// }
		//
		// bw.newLine();
		// bw.flush();
		// reponse = new DataInputStream(socket.getInputStream());
		//
		// String line = null;
		// String reallocation = "";
		// while ((line = br.readLine()) != null && line.length() != 0) {
		// System.out.println("Youtube reponse:" + line);
		// if (line.contains("Location")) {
		// reallocation = line.substring(line.indexOf(' ') + 1);
		// }
		// reponse.add(line);
		// }
		// System.out.println(reallocation);
		// br.close();
		// bw.close();
		// socket.close();
		//
		// } catch (Exception ex) {
		//
		// }
		return reponse;
	}

	public static String getTrueURLString(String youtubeURLstr) throws MalformedURLException, IOException {
		URL sourceURL = new URL(youtubeURLstr);
		String contentURLstr = "";

		BufferedReader br = new BufferedReader(new InputStreamReader(sourceURL.openStream()));
		String line = null;

		while ((line = br.readLine()) != null) {
			if (line.contains("img.src")) {
				contentURLstr = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"')).replace("\\u0026", "&").replace("\\", "")
						.replace("generate_204", "videoplayback");
				break;
			}
		}

		br.close();
		return contentURLstr;
	}

	public static DataInputStream getStreamFromYoutube(String youtubeURLstr, List<String> request) {
		DataInputStream result = null;
		try {
			String contentURLstr = getBridgeURL(youtubeURLstr);
			URL bridgeURL = new URL(contentURLstr);

			Socket socket = new Socket(bridgeURL.getHost(), bridgeURL.getPort() > 0 ? bridgeURL.getPort() : 80);

			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			if (request.get(0).contains("GET")) {
				bw.write("GET " + bridgeURL.getFile() + " HTTP/1.1");
			} else {
				bw.write("HEAD " + bridgeURL.getFile() + " HTTP/1.1");
			}
			bw.newLine();
			bw.write("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			bw.newLine();
			bw.write("Connection: close");
			bw.newLine();
			bw.newLine();

			bw.flush();

			// String line = null;
			String reallocation = "";
			/*
			 * while ((line = br.readLine()) != null && line.length() != 0) { System.out.println("Youtube reponse:" + line); if (line.contains("Location")) {
			 * reallocation = line.substring(line.indexOf(' ') + 1); } }
			 */
			if (reallocation == null || reallocation.length() == 0) {
				return new DataInputStream(socket.getInputStream());
			}
			br.close();
			bw.close();
			socket.close();

			URL reallocationURL = new URL(reallocation);

			socket = new Socket(reallocationURL.getHost(), reallocationURL.getPort() > 0 ? reallocationURL.getPort() : 80);

			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			// bw.write("GET " + reallocationURL.getFile() + " HTTP/1.1");
			// bw.newLine();
			// bw.write("Accept: text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2");
			// bw.newLine();
			// bw.write("Connection: close");
			// bw.newLine();
			// if (range != 0) {
			// bw.write("Range: bytes=" + range + "-");
			// bw.newLine();
			// }
			// bw.newLine();

			for (String requestline : request) {
				if (requestline.contains("GET")) {
					bw.write("GET " + reallocationURL.getFile() + " HTTP/1.1");
				} else {
					bw.write(requestline);
				}
				bw.newLine();
			}
			bw.newLine();
			bw.flush();

			/*
			 * line = null; while ((line = br.readLine()) != null && line.length() != 0) { System.out.println(line); }
			 */

			result = new DataInputStream(socket.getInputStream());

		} catch (IOException e) {

		}
		return result;
	}

}
