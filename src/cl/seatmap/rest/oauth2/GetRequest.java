package cl.seatmap.rest.oauth2;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

public abstract class GetRequest {
	private static final RetryPolicy RETRY_POLICY = new DefaultRetryPolicy(
			2000, 0, 1);

	private GetRequest() {
	};

	public static JsonArrayRequest createJsonArrayRequest(String url,
			Map<String, String> params, OAuth2AccessToken accessToken,
			Listener<JSONArray> listener, ErrorListener errorListener) {
		return new GetJSONArrayRequest(url, params, accessToken, listener,
				errorListener);
	}

	public static JsonObjectRequest createJsonObjectRequest(String url,
			Map<String, String> params, OAuth2AccessToken accessToken,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		return new GetJSONObjectRequest(url, params, accessToken, listener,
				errorListener);
	}

	static class GetJSONObjectRequest extends JsonObjectRequest {
		private WeakReference<OAuth2AccessToken> accessToken;

		public GetJSONObjectRequest(String url, Map<String, String> params,
				OAuth2AccessToken accessToken, Listener<JSONObject> listener,
				ErrorListener errorListener) {
			super(URLUtils.buildRequestURL(url, params), null, listener,
					errorListener);
			this.accessToken = new WeakReference<OAuth2AccessToken>(accessToken);
			setRetryPolicy(RETRY_POLICY);
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> headers = new HashMap<String, String>();
			String auth = "Bearer " + accessToken.get().getValue();
			headers.put("Authorization", auth);
			return headers;
		}

	}

	static class GetJSONArrayRequest extends JsonArrayRequest {
		private WeakReference<OAuth2AccessToken> accessToken;

		public GetJSONArrayRequest(String url, Map<String, String> params,
				OAuth2AccessToken accessToken, Listener<JSONArray> listener,
				ErrorListener errorListener) {
			super(URLUtils.buildRequestURL(url, params), listener,
					errorListener);
			this.accessToken = new WeakReference<OAuth2AccessToken>(accessToken);
			setRetryPolicy(RETRY_POLICY);
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> headers = new HashMap<String, String>();
			String auth = "Bearer " + accessToken.get().getValue();
			headers.put("Authorization", auth);
			return headers;
		}

	}
}
