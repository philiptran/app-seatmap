package cl.seatmap.rest.oauth2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import android.util.Log;

public abstract class URLUtils {
	public static String buildRequestURL(String url, Map<String, String> params) {
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

	public static String getQueryString(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		try {
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
		} catch (UnsupportedEncodingException ex) {
			Log.e("URLUtils", "getQueryString throws exception.", ex);
			return "";
		}
	}
}
