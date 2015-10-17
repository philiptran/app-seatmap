package cl.seatmap.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cl.seatmap.R;
import cl.seatmap.domain.ExchangeContact;
import cl.seatmap.rest.oauth2.ClientCredentialsResourceDetails;
import cl.seatmap.rest.oauth2.OAuth2RestTemplate;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * 
 * @author philiptrannp
 * 
 */
public class ExchangeContactService {
	private static final String REST_FIND_CONTACT = "/find";
	private static final String REST_ACCESS_TOKEN = "/oauth/token";
	private static final String REST_CLIENT_ID = "29a58142a7fe26f2157cc2719096cd9a";
	private static final String REST_CLIENT_SECRET = "b2ab780fa4836e5663d60c4b9fa971e0";
	//
	private String baseURL;
	private OAuth2RestTemplate restTemplate;
	private boolean dummyMode;

	public ExchangeContactService(Context context) {
		this.baseURL = context.getResources()
				.getText(R.string.EWS_REST_BASEURL).toString();
		//
		ClientCredentialsResourceDetails restResource = new ClientCredentialsResourceDetails();
		restResource.setAccessTokenUri(this.baseURL + REST_ACCESS_TOKEN);
		restResource.setClientId(REST_CLIENT_ID);
		restResource.setClientSecret(REST_CLIENT_SECRET);
		//
		this.restTemplate = new OAuth2RestTemplate(context, restResource);
		this.dummyMode = "true".equalsIgnoreCase(context.getResources()
				.getText(R.string.dummyMode).toString());
	}

	/**
	 * 
	 * @param name
	 * @return list of contacts or throws Exception. Never return NULL.
	 */
	public void findContact(String name,
			final Listener<List<ExchangeContact>> listener,
			final ErrorListener errorListener) {
		Map<String, String> params = new HashMap<String, String>();
		// params.put("country", "Singapore");
		params.put("name", name);
		if (dummyMode) {
			params.put("dummy", "true");
		}
		restTemplate.getArray(this.baseURL + REST_FIND_CONTACT, params,
				new Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray jsonArray) {
						List<ExchangeContact> contacts = new ArrayList<ExchangeContact>();
						try {
							for (int i = 0; i < jsonArray.length(); i++) {
								JSONObject item = jsonArray.getJSONObject(i);
								//
								ExchangeContact contact = new ExchangeContact(
										item.getString("name"), item
												.getString("title"), item
												.getString("email"), item
												.getString("department"), item
												.getString("phone"), item
												.getString("mobile"), item
												.getString("officeLocation"),
										item.getString("country"));
								contacts.add(contact);
							}
							//
							listener.onResponse(contacts);
						} catch (JSONException ex) {
							Log.e("ExchangeContactService",
									"Could not parse JSONArray.", ex);
							errorListener.onErrorResponse(new VolleyError(ex));
						}
					}
				}, errorListener);
	}
}
