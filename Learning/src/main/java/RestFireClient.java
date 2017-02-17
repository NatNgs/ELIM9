public class RestFireClient

	private const string URL = "https://elim9-76267.firebaseio.com/";
		
	private string userID = "-1";
	
	public RestFireClient(){

	}

	public String getFromFire(){

		try {

			URL url = new URL(URL + "users.json");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output, temp;
			while ((output = br.readLine()) != null); {
				output += temp;
			}

			conn.disconnect();

			return stringToMap(output);
		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}

	public Map<> stringToMap(String in){
		Map<> out = new HashMap<>();

		return out;
	}

	public String postToFire(String post){

		try {

			URL url = new URL(URL + "results.json");
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

			String output, temp;
			while ((output = br.readLine()) != null); {
				output += temp;
			}

			conn.disconnect();
			return output;

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
	}
}