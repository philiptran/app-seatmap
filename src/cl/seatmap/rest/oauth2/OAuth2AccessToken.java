package cl.seatmap.rest.oauth2;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author philiptrannp
 *
 */
public class OAuth2AccessToken implements Serializable {
	private static final long serialVersionUID = -1L;

	private String value;
	private Date expiration;

	/**
	 * Create an access token from the value provided.
	 */
	public OAuth2AccessToken(String value) {
		this.value = value;
	}

	/**
	 * Convenience method for checking expiration
	 * 
	 * @return true if the expiration is befor ethe current time
	 */
	public boolean isExpired() {
		return expiration != null && expiration.before(new Date());
	}

	public int getExpiresIn() {
		return expiration != null ? Long.valueOf(
				(expiration.getTime() - System.currentTimeMillis()) / 1000L)
				.intValue() : 0;
	}

	protected void setExpiresIn(int seconds) {
		setExpiration(new Date(System.currentTimeMillis() + seconds * 1000L));
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getExpiration() {
		return expiration;
	}

	public void setExpiration(Date expiration) {
		this.expiration = expiration;
	}
}
