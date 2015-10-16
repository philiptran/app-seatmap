package cl.seatmap.rest;

import android.content.SharedPreferences;
import android.content.res.Resources;
import cl.seatmap.R;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class ConnectionOptions {

	public static final String ARG_PROTOCOL = "connection.protocol";
	public static final String ARG_HOST = "connection.host";
	public static final String ARG_PORT = "connection.port";
	public static final String ARG_USERNAME = "connection.username";
	public static final String ARG_PASSWORD = "connection.password";

	public String baseUrl;
	public String username;
	public String password;

	public static ConnectionOptions fromSettings(Resources res,
			SharedPreferences settings) {
		ConnectionOptions opts = new ConnectionOptions();
		opts.baseUrl = settings.getString("base_url",
				res.getString(R.string.base_url));
		opts.username = settings.getString("username",
				res.getString(R.string.username));
		opts.password = settings.getString("password",
				res.getString(R.string.password));
		return opts;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("baseURL: ").append(baseUrl);
		builder.append(", username: ").append(username);
		builder.append(", password: ").append(password);
		return builder.toString();
	}
}
