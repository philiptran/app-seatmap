package cl.seatmap.rest.oauth2;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public final class TokenRequest extends StringRequest {
	private ClientCredentialsResourceDetails resource;

	public TokenRequest(ClientCredentialsResourceDetails resources,
			Listener<String> listener, ErrorListener errorListener) {
		super(Request.Method.POST, resources.getAccessTokenUri(), listener,
				errorListener);
		this.resource = resources;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Map<String, String> params = new HashMap<String, String>();
		params.put("grant_type", "client_credentials");
		return params;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			String credentials = Base64.encodeToString(
					String.format("%s:%s", resource.getClientId(),
							resource.getClientSecret()).getBytes("UTF-8"),
					Base64.NO_WRAP);
			String auth = "Basic " + credentials;

			headers.put("Authorization", auth);
			return headers;
		} catch (UnsupportedEncodingException ex) {
			throw new AuthFailureError("Could not prepare headers", ex);
		}
	}
}