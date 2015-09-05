package cl.seatmap.rest.oauth2;

/**
 * 
 * @author philiptrannp
 *
 */
public class ClientCredentialsResourceDetails {
	private String clientId;
	private String clientSecret;
	private String grantType;
	private String accessTokenUri;

	public ClientCredentialsResourceDetails() {
		this.grantType = "client_credentials";
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getAccessTokenUri() {
		return accessTokenUri;
	}

	public void setAccessTokenUri(String accessTokenUri) {
		this.accessTokenUri = accessTokenUri;
	}
}
