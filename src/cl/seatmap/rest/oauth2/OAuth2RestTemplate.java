package cl.seatmap.rest.oauth2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;

import android.util.Base64;

/**
 * 
 * @author philiptrannp
 * 
 */
public class OAuth2RestTemplate {
	private static final int CONNECTION_TIMEOUT_MS = 2 * 1000;

	private ClientCredentialsResourceDetails resource;
	private OAuth2AccessToken accessToken;

	//
	public OAuth2RestTemplate(ClientCredentialsResourceDetails resource) {
		this.resource = resource;

		// trust all certs
		// @formatter:off
		TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				@Override
				public void checkClientTrusted(X509Certificate[] certs,
					String authType) {}
				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {}
			}
		};
		// @formatter:on
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection
					.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			throw new RestServiceException("Fail to init OAuth2RestTemplate.",
					e);
		}
		// turn off host name verifier
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
	}

	public String get(String url) {
		return get(url, null);
	}

	public String get(String url, Map<String, String> params) {
		HttpURLConnection connection = null;
		try {
			OAuth2AccessToken accessToken = getAccessToken();
			//
			String requestURL = buildRequestURL(url, params);
			connection = (HttpURLConnection) new URL(requestURL)
					.openConnection();
			connection.setRequestProperty("Authorization", "Bearer "
					+ accessToken.getValue());
			connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
			connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(false);
			connection.setDoInput(true);
			//
			connection.connect();

			// read the response
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					// access token may have been invalidated
					this.accessToken = null;
				}
				//
				throw new RestServiceException("HTTP " + responseCode + ": "
						+ connection.getResponseMessage());
			}
			//
			return readInputStream(connection.getInputStream());
		} catch (RestServiceException re) {
			throw re;
		} catch (Exception e) {
			throw new RestServiceException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public String post(String url, String jsonBody) {
		HttpURLConnection connection = null;
		try {
			OAuth2AccessToken accessToken = getAccessToken();
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestProperty("Authorization", "Bearer "
					+ accessToken.getValue());
			connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
			connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			if (jsonBody == null || jsonBody.isEmpty()) {
				connection.setDoOutput(false);
				connection.connect();
			} else {
				connection.setDoOutput(true);
				connection.connect();

				// write request body
				DataOutputStream wr = new DataOutputStream(
						connection.getOutputStream());
				wr.writeBytes(jsonBody);
				wr.flush();
				wr.close();
			}
			// read the response
			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new RestServiceException("HTTP " + responseCode + ": "
						+ connection.getResponseMessage());
			}
			//
			return readInputStream(connection.getInputStream());
		} catch (RestServiceException re) {
			throw re;
		} catch (Exception e) {
			throw new RestServiceException(e);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String readInputStream(InputStream is) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				response.append(line);
			}
			return response.toString();
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}

	protected String buildRequestURL(String url, Map<String, String> params)
			throws UnsupportedEncodingException {
		if (params == null || params.isEmpty()) {
			return url;
		}
		//
		String queryString = getQueryString(params);
		StringBuilder sb = new StringBuilder(url);
		if (url.contains("?")) {
			// url already have some params, append using &
			sb.append("&").append(queryString);
		} else {
			sb.append("?").append(queryString);
		}
		return sb.toString();
	}

	protected OAuth2AccessToken getAccessToken() {
		if (accessToken == null || accessToken.isExpired()) {
			HttpURLConnection connection = null;
			try {
				// acquire new access token
				connection = (HttpURLConnection) new URL(
						resource.getAccessTokenUri()).openConnection();
				String credentials = Base64.encodeToString(
						String.format("%s:%s", resource.getClientId(),
								resource.getClientSecret()).getBytes("UTF-8"),
						Base64.NO_WRAP);
				connection.setRequestProperty("Authorization", "Basic "
						+ credentials);
				connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);
				connection.setReadTimeout(CONNECTION_TIMEOUT_MS);
				//
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type",
						"application/x-www-form-urlencoded");
				connection.setUseCaches(false);
				connection.setDoOutput(true);
				connection.setDoInput(true);
				connection.connect();
				//
				Map<String, String> params = new HashMap<String, String>();
				params.put("grant_type", "client_credentials");
				//
				DataOutputStream wr = new DataOutputStream(
						connection.getOutputStream());
				wr.writeBytes(getQueryString(params));
				wr.flush();
				wr.close();

				// read the response
				int responseCode = connection.getResponseCode();
				if (responseCode != HttpURLConnection.HTTP_OK) {
					throw new RestServiceException("HTTP " + responseCode
							+ ": " + connection.getResponseMessage());
				}
				String response = readInputStream(connection.getInputStream());
				// extract access token
				JSONObject jsonObject = new JSONObject(response);
				String accessTokenValue = null;
				int expiresIn = 0;
				if (jsonObject.has("access_token")) {
					accessTokenValue = jsonObject.getString("access_token");
					expiresIn = jsonObject.getInt("expires_in");
					//
					this.accessToken = new OAuth2AccessToken(accessTokenValue);
					this.accessToken.setExpiresIn(expiresIn);
				} else {
					throw new RestServiceException(
							"Token endpoint did not return an access_token");
				}
			} catch (RestServiceException re) {
				throw re;
			} catch (Exception e) {
				throw new RestServiceException(e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}
		return this.accessToken;
	}

	private String getQueryString(Map<String, String> params)
			throws UnsupportedEncodingException {
		if (params == null || params.isEmpty()) {
			return "";
		}
		//
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append("&");
			}
			sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}
		return sb.toString();
	}

	public enum HttpMethod {
		GET, POST;
	}
}
