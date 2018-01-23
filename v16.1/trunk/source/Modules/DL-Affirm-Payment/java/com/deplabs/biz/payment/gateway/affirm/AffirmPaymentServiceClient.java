/**
 * 
 */
package com.deplabs.biz.payment.gateway.affirm;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ibm.icu.text.MessageFormat;

/**
 * @author horacioa
 * 
 */
public class AffirmPaymentServiceClient implements IAffirmPaymentService {
	
	private final Log log = LogFactory.getLog(AffirmPaymentServiceClient.class);
	
	private static String DEFAULT_ENCODING = "UTF-8";
	
	private AffirmPaymentServiceConfig config;
	
	private final int maxHTTPRetries = 3;
	
	private HttpClient httpClient = null;
	
	public AffirmPaymentServiceClient(AffirmPaymentServiceConfig serviceConfig) {
		config = serviceConfig;
		//this.httpClient = configureHttpClient();
		this.httpClient = new HttpClient();
	}

	@Override
	public AffirmResponse authorize(AffirmRequest affirmRequest) {
		return invoke(AffirmApiAction.AUTHORIZE, affirmRequest, this.getAuthorizeURL(affirmRequest));
	}
	
	@Override
	public AffirmResponse capture(AffirmRequest affirmRequest) throws AffirmPaymentServiceException {
		return invoke(AffirmApiAction.CAPTURE, affirmRequest, this.getCaptureURL(affirmRequest));
	}

	@Override
	public AffirmResponse voidPayment(AffirmRequest affirmRequest) throws AffirmPaymentServiceException {
		return invoke(AffirmApiAction.VOID, affirmRequest, this.getVoidURL(affirmRequest));
	}

	@Override
	public AffirmResponse refundPayment(AffirmRequest affirmRequest) throws AffirmPaymentServiceException {
		return invoke(AffirmApiAction.REFUND, affirmRequest, this.getRefundURL(affirmRequest));
	}
	
	public AffirmResponse update(AffirmRequest affirmRequest) throws AffirmPaymentServiceException {
		return invoke(AffirmApiAction.UPDATE, affirmRequest, this.getUpdateURL(affirmRequest));
	}
	
	@Override
	public AffirmResponse read(AffirmRequest affirmRequest) throws AffirmPaymentServiceException {
		return invoke(AffirmApiAction.READ, affirmRequest, this.getReadURL(affirmRequest));
	}


	/**
	 * Performs the invokation of the service action based on the incoming
	 * request
	 * 
	 * @param action
	 * @param affirmRequest
	 * @param serviceURL
	 * @return the service Response
	 */
	protected AffirmResponse invoke(AffirmApiAction action, AffirmRequest affirmRequest, String serviceURL) {
		AffirmResponse affirmResponse = new AffirmResponse();
		
	    String response = null;
	    HttpMethod method = null;

	    if (action.getMethod().equals("POST")) {
	    	method = this.createPostMethod(serviceURL, affirmRequest);
	    } else if (action.getMethod().equals("GET")) {
	    	method = this.createGetMethod(serviceURL, affirmRequest);
	    }
	    
	    int responseCode = -1;
	    
	    log.debug(new StringBuilder().append("Invoking").append(action.name()).append(" request. Current parameters: ").append(affirmRequest).toString());
	    
		try {
			log.debug(new StringBuilder().append("Setting content-type to ").append(config.getContentType()).toString());
	      
	      // Base64 encode API keys for basic authentication
	      String encodedAuth = org.apache.commons.codec.binary.Base64.encodeBase64String((config.getPublicApiKey() + ":" + config.getPrivateApiKey()).getBytes());
	      //String encodedAuth2 = java.util.Base64.getEncoder().encodeToString((config.getPublicApiKey() + ":" + config.getPrivateApiKey()).getBytes());
	      method.addRequestHeader("Authorization", "Basic " + encodedAuth.trim());
	      method.addRequestHeader("Content-Type", config.getContentType());
	      
	      int continueTrying = 1;
	      int retries = 0;
			do {
				log.debug(new StringBuilder().append("Sending Request to host:  ").append(serviceURL).toString());

				try {
					responseCode = this.httpClient.executeMethod(method);
					affirmResponse.setPaymentGateway(this.config.getPaymentGatewayName());

					response = method.getResponseBodyAsString();
					affirmResponse.setResponseBody(response);
					affirmResponse.setFields(this.getResponseFieldsAsJson(response));
					affirmResponse.setResponseCode(responseCode + "");
					
					log.debug(new StringBuilder().append("Received Response. Status: ").append(responseCode).append(". ").append("Response Body: ").append(response).toString());

					if (responseCode == 200) {
						continueTrying = 0;
						affirmResponse.setSuccess(true);
						
					} else {
						if ((responseCode == 500) && (pauseIfRetryNeeded(++retries))) {
							continueTrying = 1;
						} else {
							// any other error (including 400, 401, etc) will be treated as an API error
							continueTrying = 0;
							affirmResponse.setSuccess(false);
						}
					}
				} catch (IOException ioe) {
					throw new AffirmPaymentServiceException("Internal IO Error", ioe);
				} catch (Exception e) {
					throw new AffirmPaymentServiceException(e);
				} finally {
					method.releaseConnection();
				}
			} while (continueTrying != 0);
		} catch (AffirmPaymentServiceException apse) {
			log.error("Caught AffirmPaymentServiceException", apse);
			throw apse;
		} catch (Throwable t) {
			log.error("Caught Exception", t);
			throw new AffirmPaymentServiceException(t);
		}
		return affirmResponse;
	}
	
	protected HttpMethod createPostMethod(String serviceURL, AffirmRequest affirmRequest) {
		HttpMethod method = new PostMethod(serviceURL);
		
		String requestBody = affirmRequest.getContentAsJsonString();
		if (null != requestBody) {
			StringRequestEntity requestEntity;
			try {
				requestEntity = new StringRequestEntity(requestBody, config.getContentType(), null);
				((PostMethod)method).setRequestEntity(requestEntity);
			} catch (UnsupportedEncodingException e) {
				log.error(e);
			}
		}

		return method;
	}
	
	protected HttpMethod createGetMethod(String serviceURL, AffirmRequest affirmRequest) {
		return new GetMethod(serviceURL);
	}

	/**
	 * Performs the HTTP Client configuration based on configured properties
	 * 
	 * @return a configured instance of {@code HttpClient}
	 */
	protected HttpClient configureHttpClient() {
		HttpClientParams localHttpClientParams = new HttpClientParams();
		
		/* RETRIES CONFIG */
		localHttpClientParams.setParameter("http.method.retry-handler", new HttpMethodRetryHandler() {
			public boolean retryMethod(HttpMethod paramHttpMethod, IOException paramIOException, int paramInt) {
				if (paramInt > maxHTTPRetries) {
					log.debug("Maximum Number of Retry attempts reached, will not retry");
					return false;
				}
				log.debug("Retrying request. Attempt " + paramInt);
				if (paramIOException instanceof NoHttpResponseException) {
					log.debug("Retrying on NoHttpResponseException");
					return true;
				}
				if (paramIOException instanceof InterruptedIOException) {
					log.debug("Will not retry on InterruptedIOException", paramIOException);
					return false;
				}
				if (paramIOException instanceof UnknownHostException) {
					log.debug("Will not retry on UnknownHostException", paramIOException);
					return false;
				}
				if (!(paramHttpMethod.isRequestSent())) {
					log.debug("Retrying on failed sent request");
					return true;
				}
				return false;
			}
		});
		
		HostConfiguration localHostConfiguration = new HostConfiguration();
		HttpConnectionManagerParams localHttpConnectionManagerParams = new HttpConnectionManagerParams();
		localHttpConnectionManagerParams.setConnectionTimeout(config.getConnectionTimeout());
		localHttpConnectionManagerParams.setSoTimeout(config.getSoTimeout());
		localHttpConnectionManagerParams.setStaleCheckingEnabled(true);
		localHttpConnectionManagerParams.setTcpNoDelay(true);
		localHttpConnectionManagerParams.setMaxTotalConnections(config.getMaxConnections());
		localHttpConnectionManagerParams.setMaxConnectionsPerHost(localHostConfiguration, config.getMaxConnections());
		MultiThreadedHttpConnectionManager localMultiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
		localMultiThreadedHttpConnectionManager.setParams(localHttpConnectionManagerParams);
		
		this.httpClient = new HttpClient(localHttpClientParams, localMultiThreadedHttpConnectionManager);
		this.httpClient.setHostConfiguration(localHostConfiguration);
		
		return this.httpClient;
	}
	
	@SuppressWarnings("rawtypes")
	protected void addRequiredParametersToPostRequest(PostMethod paramPostMethod, Map<String, String> paramMap) {
		//paramMap.put("Version", "2013-01-01");
		
		Iterator localIterator = paramMap.entrySet().iterator();
		while (localIterator.hasNext()) {
			Map.Entry localEntry = (Map.Entry) localIterator.next();
			paramPostMethod.addParameter((String) localEntry.getKey(), (String) localEntry.getValue());
		}
	}

	protected String getResponsBodyAsString(InputStream paramInputStream) throws IOException {
		String str = null;
		try {
			InputStreamReader localInputStreamReader = new InputStreamReader(paramInputStream, DEFAULT_ENCODING);
			StringBuilder localStringBuilder = new StringBuilder();
			char[] arrayOfChar = new char[1024];
			int i;
			while (0 < (i = localInputStreamReader.read(arrayOfChar))) {
				localStringBuilder.append(arrayOfChar, 0, i);
			}
			str = localStringBuilder.toString();
		} finally {
			paramInputStream.close();
		}
		return str;
	}
	
	protected boolean pauseIfRetryNeeded(int paramInt) throws InterruptedException {
		if (paramInt <= config.getMaxErrorRetry()) {
			long l = Math.round((Math.pow(4.0D, paramInt) * 100.0D));

			log.debug(new StringBuilder().append("Retriable error detected, will retry in ").append(l).append("ms, attempt number: ").append(paramInt).toString());

			Thread.sleep(l);
			return true;
		}
		return false;
	}
	
	protected JSONObject getResponseFieldsAsJson(String response) {
		JSONObject json = null;
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(response);
		} catch (Exception e) {
			log.error("Unable to get a json object from response body: " + response, e);
		}
		
		return json;
	}
	
	private String getAuthorizeURL(AffirmRequest affirmRequest) {
		String[] tokens = null;
		return getFullActionServiceURL(config.getAuthorizeServiceURL(), tokens);
	}
	
	private String getCaptureURL(AffirmRequest affirmRequest) {
		String[] tokens = { affirmRequest.getChargeId() };
		return getFullActionServiceURL(config.getCaptureServiceURL(), tokens);
	}
	
	private String getVoidURL(AffirmRequest affirmRequest) {
		String[] tokens = { affirmRequest.getChargeId() };
		return getFullActionServiceURL(config.getVoidServiceURL(), tokens);
	}
	
	private String getRefundURL(AffirmRequest affirmRequest) {
		String[] tokens = { affirmRequest.getChargeId() };
		return getFullActionServiceURL(config.getRefundServiceURL(), tokens);
	}
	
	private String getUpdateURL(AffirmRequest affirmRequest) {
		String[] tokens = { affirmRequest.getChargeId() };
		return getFullActionServiceURL(config.getUpdateServiceURL(), tokens);
	}
	
	private String getReadURL(AffirmRequest affirmRequest) {
		String[] tokens = { affirmRequest.getChargeId() };
		return getFullActionServiceURL(config.getReadServiceURL(), tokens);
	}
	
	private String getFullActionServiceURL(String actionURL, String... tokens) {
		String baseURL = config.getBaseServiceURL();
		URL url = null;
		try {
			if (tokens != null) {
				actionURL = MessageFormat.format(actionURL, (Object[])tokens);
			}
			url = new URL(new URL(baseURL), actionURL);
			return url.toString();
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (Exception e) {
			log.error(e);
		}
		return "";
	}

}
