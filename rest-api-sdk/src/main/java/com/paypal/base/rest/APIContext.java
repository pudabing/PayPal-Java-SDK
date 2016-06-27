package com.paypal.base.rest;

import java.util.Map;
import java.util.UUID;

import com.paypal.base.Constants;
import com.paypal.base.SDKVersion;

/**
 * <code>APIContext</code> wraps wire-level parameters for the API call.
 * AccessToken, which is essentially an OAuth token, is treated as a mandatory
 * parameter for (PayPal REST APIs). RequestId is generated if not supplied for
 * marking Idempotency of the API call. OAuth token can be generated using
 * {@link OAuthTokenCredential}. The Application Header property may be used by
 * clients to access application level headers. The clients are responsible to
 * cast the Application Header property to appropriate type.
 * 
 * @author kjayakumar
 * 
 */
public class APIContext {

	/**
	 * Request Id
	 */
	private String requestId;

	/**
	 * Parameter to mask RequestId
	 */
	private boolean maskRequestId;

	/**
	 * {@link SDKVersion} instance
	 */
	private SDKVersion sdkVersion;

	/**
	 * {@link OAuthTokenCredential} credential instance
	 */
	private OAuthTokenCredential credential;

	/**
	 * Default Constructor
	 * @deprecated Please use `APIContext(String clientID, String clientSecret, String mode)` instead. 
	 */
	public APIContext() {
		super();
		this.credential = new OAuthTokenCredential(null);
	}

	/**
	 * Pass the clientID, secret and mode. The easiest, and most widely used
	 * option.
	 * 
	 * @param clientID
	 * @param clientSecret
	 * @param mode
	 */
	public APIContext(String clientID, String clientSecret, String mode) {
		this(clientID, clientSecret, mode, null);
	}

	/**
	 * Pass the clientID, secret and mode along with additional configurations.
	 * 
	 * @param clientID
	 * @param clientSecret
	 * @param mode
	 * @param configurations
	 */
	public APIContext(String clientID, String clientSecret, String mode, Map<String, String> configurations) {
		this.credential = new OAuthTokenCredential(clientID, clientSecret);
		if (configurations != null && configurations.size() > 0) {
			this.credential.addConfigurations(configurations);
		}
		this.setMode(mode);
	}

	/**
	 * APIContext, requestId is auto generated, calling setMaskRequestId(true)
	 * will override the requestId getter to return null
	 * 
	 * @param accessToken
	 *            OAuthToken required for the call. OAuth token used by the REST
	 *            API service. The token should be of the form 'Bearer xxxx..'.
	 *            See {@link OAuthTokenCredential} to generate OAuthToken
	 */
	public APIContext(String accessToken) {
		super();
		if (accessToken == null || accessToken.length() <= 0) {
			throw new IllegalArgumentException("AccessToken cannot be null");
		}
		this.credential = new OAuthTokenCredential(accessToken);
	}

	/**
	 * APIContext
	 * 
	 * @param accessToken
	 *            OAuthToken required for the call. OAuth token used by the REST
	 *            API service. The token should be of the form 'Bearer xxxx..'.
	 *            See {@link OAuthTokenCredential} to generate OAuthToken
	 * @param requestId
	 *            Unique requestId required for the call. Idempotency id,
	 *            Calling setMaskRequestId(true) will override the requestId
	 *            getter to return null, which can be used by the client (null
	 *            check) to forcibly not sent requestId in the API call.
	 */
	public APIContext(String accessToken, String requestId) {
		this(accessToken);
		if (requestId == null || requestId.length() <= 0) {
			throw new IllegalArgumentException("RequestId cannot be null");
		}
		this.requestId = requestId;
	}

	/**
	 * Sets refresh token to be used for third party OAuth operations. This is commonly used for 
	 * third party invoicing and future payments.
	 * 
	 * @param refreshToken
	 * @return {@link APIContext}
	 */
	public APIContext setRefreshToken(String refreshToken) {
		if (this.credential != null && this.credential.hasCredentials()) {
			this.credential.setRefreshToken(refreshToken);
		} else {
			throw new IllegalArgumentException(
					"ClientID and Secret are required. Please use APIContext(String clientID, String clientSecret, String mode)");
		}
		return this;
	}
	
	/**
	 * Sets mode to either `live` or `sandbox`.
	 * @param mode
	 * @return {@link APIContext}
	 */
	public APIContext setMode(String mode) {
		if (mode == null || !(mode.equals(Constants.LIVE) || mode.equals(Constants.SANDBOX))) {
			throw new IllegalArgumentException("Mode needs to be either `sandbox` or `live`.");
		}
		this.credential.addConfiguration(Constants.MODE, mode);
		return this;
	}

	/**
	 * Returns HTTP Headers.
	 * 
	 * @return the hTTPHeaders
	 */
	public Map<String, String> getHTTPHeaders() {
		return this.credential.getHeaders();
	}

	/**
	 * Replaces existing headers with provided one.
	 * 
	 * @param httpHeaders
	 *            the httpHeaders to set
	 */
	public APIContext setHTTPHeaders(Map<String, String> httpHeaders) {
		this.credential.setHeaders(httpHeaders);
		return this;
	}

	/**
	 * Adds HTTP Headers to existing list
	 * 
	 * @param httpHeaders
	 *            the httpHeaders to set
	 */
	public APIContext addHTTPHeaders(Map<String, String> httpHeaders) {
		this.credential.addHeaders(httpHeaders);
		return this;
	}
	

	/**
	 * Adds HTTP Header to existing list
	 * 
	 * @param key
	 * @param value
	 */
	public APIContext addHTTPHeader(String key, String value) {
		this.credential.addHeader(key, value);
		return this;
	}

	/**
	 * Returns Configuration Map
	 * 
	 * @return {@link Map} of configurations
	 */
	public Map<String, String> getConfigurationMap() {
		return this.credential.getConfigurations();
	}

	/**
	 * Replaces the existing configurations with provided one
	 * 
	 * @param configurationMap
	 *            the configurationMap to set
	 * @return {@link APIContext}
	 */
	public APIContext setConfigurationMap(Map<String, String> configurationMap) {
		this.credential.setConfigurations(configurationMap);
		return this;
	}

	/**
	 * Adds configurations
	 * 
	 * @param configurations
	 */
	public void addConfigurations(Map<String, String> configurations) {
		this.credential.addConfigurations(configurations);
	}

	/**
	 * @deprecated Please use {@link #fetchAccessToken()} instead.
	 * Previously, this was a dumb getter method. However, we enabled the feature to re-generate the access Token if null, or expired.
	 * This required us to throw proper PayPalRESTException, with error information on failure.
	 * 
	 * @return Access Token
	 */
	public String getAccessToken() {
		try {
			return fetchAccessToken();
		} catch (PayPalRESTException ex) {
			// we should be throwing proper exception here.
			return null;
		}
	}
	
	/**
	 * Returns the access Token. Regenerates if null or expired.
	 * 
	 * @return {@link String} of AccessToken
	 * @throws PayPalRESTException
	 */
	public String fetchAccessToken() throws PayPalRESTException {
		String accessToken = null;
		if (this.credential != null) {
			accessToken = this.credential.getAccessToken();
		}
		return accessToken;
	}

	/**
	 * Returns the unique requestId set during creation, if not available and if
	 * maskRequestId is set to false returns a generated one, else returns null.
	 * 
	 * @return requestId
	 */
	public String getRequestId() {
		String reqId = null;
		if (!maskRequestId) {
			if (requestId == null || requestId.length() <= 0) {
				requestId = UUID.randomUUID().toString();
			}
			reqId = requestId;
		}
		return reqId;
	}

	/**
	 * @param maskRequestId
	 *            the maskRequestId to set
	 */
	public void setMaskRequestId(boolean maskRequestId) {
		this.maskRequestId = maskRequestId;
	}

	/**
	 * @return the sdkVersion
	 */
	public SDKVersion getSdkVersion() {
		return sdkVersion;
	}

	/**
	 * @param sdkVersion
	 *            the sdkVersion to set
	 */
	public void setSdkVersion(SDKVersion sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	/**
	 * @deprecated Use getHTTPHeaders() instead
	 * @return the headersMap
	 */
	public Map<String, String> getHeadersMap() {
		return this.getHTTPHeaders();
	}

	/**
	 * @deprecated
	 * @param headersMap
	 *            the headersMap to set
	 */
	public void setHeadersMap(Map<String, String> headersMap) {
		this.setHTTPHeaders(headersMap);
	}

	public String getClientID() {
		if (this.credential == null) {
			throw new IllegalArgumentException(
					"ClientID and Secret are required. Please use APIContext(String clientID, String clientSecret, String mode)");
		}
		return this.credential.getClientID();
	}

	public String getClientSecret() {
		if (this.credential == null) {
			throw new IllegalArgumentException(
					"ClientID and Secret are required. Please use APIContext(String clientID, String clientSecret, String mode)");
		}
		return this.credential.getClientSecret();
	}

}
