package com.app.dlna.dmc.utility;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;

import com.app.dlna.dmc.processor.http.HTTPServerData;

public class Utility {
	public static String intToIp(int i) {
		String result = "";
		result = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
		return result;
	}

	public static String getMD5(String input) {
		// try {
		// byte[] bytesOfMessage = input.getBytes("UTF-8");
		// MessageDigest md = MessageDigest.getInstance("MD5");
		// byte[] disgest = md.digest(bytesOfMessage);
		// return Base64.encodeToString(disgest, Base64.CRLF);
		// } catch (Exception ex) {
		// Log.e(TAG, "Create hash fail. input = " + input);
		// return null;
		// }
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			md.update(input.getBytes());
			StringBuffer hexString = new StringBuffer();
			byte[] mdbytes = md.digest();
			for (int i = 0; i < mdbytes.length; i++) {
				String hex = Integer.toHexString(0xff & mdbytes[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			return null;
		}
	}

	public static String createLink(File file) {
		try {
			return new URI("http", HTTPServerData.HOST + ":" + HTTPServerData.PORT, file.getAbsolutePath(), null, null).toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String converTimeToString(int time) {
		String format = String.format("%%0%dd", 2);
		String seconds = String.format(format, time % 60);
		String minutes = String.format(format, (time % 3600) / 60);
		String hours = String.format(format, time / 3600);
		return hours + ":" + minutes + ":" + seconds;
	}
}
