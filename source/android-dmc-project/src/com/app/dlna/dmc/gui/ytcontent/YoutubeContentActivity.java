package com.app.dlna.dmc.gui.ytcontent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.app.dlna.dmc.R;
import com.app.dlna.dmc.gui.devices.DMRListActivity;

public class YoutubeContentActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ytcontent_activitiy);
	}

	public void onLinkClick(View view) {
		String link = ((Button) view).getText().toString();
		try {
			URL youtube = new URL(link);
			Socket socket = new Socket(youtube.getHost(), 80);
			PrintStream ps = new PrintStream(socket.getOutputStream());
			ps.println("GET " + youtube.getFile() + " HTTP/1.1");
			ps.println("Host: " + youtube.getAuthority());
			ps.println("Connection: close");
			ps.println("User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1");
			ps.println("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			ps.println();
			ps.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String inputLine;
			String directlink = null;
			while ((inputLine = in.readLine()) != null) {
				System.out.println(inputLine);
				if (inputLine.contains("img.src")) {
					Toast.makeText(YoutubeContentActivity.this, "Get Source Success", Toast.LENGTH_SHORT).show();
					System.out.println(" Line = " + inputLine);
					directlink = inputLine.substring(inputLine.indexOf('"') + 1, inputLine.lastIndexOf('"'))
							.replace("\\u0026", "&").replace("\\", "").replace("generate_204", "videoplayback");
					System.out.println(" Direct Link = " + directlink);
					break;
				}
			}
			in.close();
			ps.close();
			Intent intent = new Intent(YoutubeContentActivity.this, DMRListActivity.class);
			intent.putExtra("URL", directlink);
			intent.putExtra("Title", link);
			YoutubeContentActivity.this.startActivity(intent);
		} catch (Exception ex) {
			Toast.makeText(YoutubeContentActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
