package cl.seatmap.rest.oauth2;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * 
 * @author philiptrannp
 * 
 */
public class OAuth2RestTemplate {
	private ClientCredentialsResourceDetails resource;
	private OAuth2AccessToken accessToken;
	//
	private final RequestQueue requestQueue;

	//
	public OAuth2RestTemplate(Context context,
			ClientCredentialsResourceDetails resource) {
		this.resource = resource;
		this.requestQueue = Volley.newRequestQueue(context);
		this.accessToken = new OAuth2AccessToken();
		//
		validateAccessToken();
	}

	public void get(String url, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		get(url, null, listener, errorListener);
	}

	public void get(String url, Map<String, String> params,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		validateAccessToken();
		JsonObjectRequest request = GetRequest.createJsonObjectRequest(url,
				params, accessToken, listener, errorListener);
		requestQueue.add(request);
	}

	public void getArray(String url, Listener<JSONArray> listener,
			ErrorListener errorListener) {
		getArray(url, null, listener, errorListener);
	}

	public void getArray(String url, Map<String, String> params,
			Listener<JSONArray> listener, ErrorListener errorListener) {
		validateAccessToken();
		JsonArrayRequest request = GetRequest.createJsonArrayRequest(url,
				params, accessToken, listener, errorListener);
		requestQueue.add(request);
	}

	protected void validateAccessToken() {
		if (accessToken.isExpired()) {
			StringRequest request = new TokenRequest(resource,
					new Listener<String>() {
						@Override
						public void onResponse(String response) {
							try {
								// extract access token
								JSONObject jsonObject = new JSONObject(response);
								String accessTokenValue = null;
								int expiresIn = 0;
								if (jsonObject.has("access_token")) {
									accessTokenValue = jsonObject
											.getString("access_token");
									expiresIn = jsonObject.getInt("expires_in");
									//
									accessToken.setValue(accessTokenValue);
									accessToken.setExpiresIn(expiresIn);
								} else {
									throw new JSONException(
											"Token endpoint did not return an access_token");
								}
							} catch (JSONException ex) {
								throw new RestServiceException(ex);
							}
						}

					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("RestTemplate", "Error during Token request",
									error);
						}
					});
			requestQueue.add(request);
		}
	}
}
