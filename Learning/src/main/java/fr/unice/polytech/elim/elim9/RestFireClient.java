package fr.unice.polytech.elim.elim9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class RestFireClient {

	private final String url = "https://elim9-76267.firebaseio.com/";
		
	private String userID = "-1";
	
	public RestFireClient(){

	}

	public Map<String, String> getFromFire() {

		try {
			URL url = new URL(this.url + "users.json");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output="", temp;
			while ((temp = br.readLine()) != null) {
				output += temp;
			}

			conn.disconnect();

			return jsonToMap(output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> jsonToMap(String in){
		Map<String,String>



		return out;
	}

	public String postToFire(String post){
		try {
			URL url = new URL(this.url + "results.json");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");


			OutputStream os = conn.getOutputStream();
			os.write(post.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output="", temp;
			while ((temp = br.readLine()) != null) {
				output += temp;
			}

			conn.disconnect();
			return output;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}