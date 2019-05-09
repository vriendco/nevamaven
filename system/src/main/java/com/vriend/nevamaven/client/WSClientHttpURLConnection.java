package com.vriend.nevamaven.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

public class WSClientHttpURLConnection {

	private static final String ACCEPT_LANGUAGE = "en-US,en;q=0.5";
	private final String USER_AGENT = "Mozilla/5.0";

	public WSClientHttpURLConnection() {
	}

	public WSReponse exec(String url) throws WSClientException {
		try {

			HttpURLConnection con = buildHttpURLConnection(url);

			// optional default is GET
			con.setRequestMethod("GET");

			addRequestHeadProperties(con);

			int responseCode = con.getResponseCode();
			String responseOutput = extractOutput(con);
			WSReponse response = new WSReponse();
			response.setCode(responseCode);
			response.setOutput(responseOutput);

			return response;

		} catch (IOException e) {
			throw new WSClientException(e);
		}
	}

	private void addRequestHeadProperties(HttpURLConnection con) {
		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);
	}

	private HttpURLConnection buildHttpURLConnection(String url) throws MalformedURLException, IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		return con;
	}

	private String extractOutput(HttpURLConnection con) throws IOException {

		InputStream input = null;

		if (con.getResponseCode() == 200) {
			input = con.getInputStream();
		} else {
			input = con.getErrorStream();
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();

	}

}
