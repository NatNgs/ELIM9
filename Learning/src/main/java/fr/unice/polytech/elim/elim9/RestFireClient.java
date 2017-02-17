package fr.unice.polytech.elim.elim9;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class RestFireClient {

	private final String url = "https://elim9-76267.firebaseio.com/";

	private String userID = "-1";


	public String getFromFire() {

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

			StringBuilder output = new StringBuilder();
			String temp;
			while ((temp = br.readLine()) != null) {
				output.append(temp);
			}

			conn.disconnect();

			return output.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void postToFire0(String address, String post) {
		try {
			URL url = new URL((this.url + "results/" + address + "/.json").replace(" ", "%20"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
			conn.setRequestProperty("Content-Type", "application/json");


			OutputStream os = conn.getOutputStream();
			os.write(post.getBytes());
			os.flush();

			if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				System.err.println("Failed : HTTP error code at "
						+ url + ": "
						+ conn.getResponseCode() + "\n"+conn.getResponseMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void postToFire1(String address, String post) {
		try {
			URI url = new URI((this.url + "results/" + address + "/.json").replace(" ", "%20"));

			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPatch httpPatch = new HttpPatch(url);

			InputStreamEntity reqEntity = new InputStreamEntity(
					new ByteArrayInputStream(post.getBytes()), -1, ContentType.APPLICATION_OCTET_STREAM);
			reqEntity.setChunked(true);

			httpPatch.setEntity(reqEntity);

			CloseableHttpResponse response = httpClient.execute(httpPatch);
			System.out.println(url + "\n"+response.toString());

		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void postToFire2(String address, Serializable post) {
		try {
			HttpResponse<JsonNode> jsonResponse = Unirest.patch((this.url + "results/.json").replace(" ", "%20"))
					.header("accept", "application/json")
					.body("{\""+address+"\":"+post+"}")
					.asJson();

			System.out.println("Response = "+jsonResponse.getStatus()+":"+jsonResponse.getStatusText()+" : "+jsonResponse.getBody());
		} catch (UnirestException e) {
			e.printStackTrace();
		}
	}
}
