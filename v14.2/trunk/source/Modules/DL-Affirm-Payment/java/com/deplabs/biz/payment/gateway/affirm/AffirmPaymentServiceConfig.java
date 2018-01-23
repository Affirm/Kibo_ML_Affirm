package com.deplabs.biz.payment.gateway.affirm;

import java.util.Properties;

/**
 * 
 * @author horacioa
 * 
 */
public class AffirmPaymentServiceConfig {

	private String publicApiKey; 			// 3JI8S4SX92K6DENK
	private String privateApiKey;			// SVBwsdxChDMbvUAE3q2uyoMtep3HJGdZ
	private String contentType;				// application/json
	private String authorization;			// Basic
	
	private String baseServiceURL;			// https://sandbox.affirm.com
	private String authorizeServiceURL; 	// /api/v2/charges
	private String captureServiceURL;		// /api/v2/charges/{0}/capture
	private String refundServiceURL;		// /api/v2/charges/{0}/refund
	private String voidServiceURL;			// /api/v2/charges/{0}/void
	private String updateServiceURL;		// /api/v2/charges/{0}/update
	private String readServiceURL;			// /api/v2/charges/{0}

	private int maxErrorRetry = 3;			// 3
	private int maxConnections = 100;		// 100
	private int connectionTimeout = 50000;	// 50000 
	private int soTimeout = 50000;			// 50000
	
	/** The name of the payment gateway. */
    private String paymentGatewayName;		// AFFIRM
    
	private Properties properties;

	public AffirmPaymentServiceConfig() {
		this.properties = null;
	}

	public AffirmPaymentServiceConfig(Properties serviceConfigProperties) throws NullPointerException {
		this.properties = serviceConfigProperties;
		if (serviceConfigProperties.isEmpty()) {
			throw new IllegalArgumentException("Properties are empty, Need required properties to proceed configuring AffirmPaymentServiceConfig");
		}
		
		if (checkProperty("affirmpayment_gateway_name")) {
			setPaymentGatewayName(this.properties.getProperty("affirmpayment_gateway_name"));
		}

		if (checkProperty("affirmpayment_public_api_key")) {
			setPublicApiKey(this.properties.getProperty("affirmpayment_public_api_key"));
		}
		
		if (checkProperty("affirmpayment_private_api_key")) {
			setPrivateApiKey(this.properties.getProperty("affirmpayment_private_api_key"));
		}
		
		if (checkProperty("affirmpayment_contentType")) {
			setContentType(this.properties.getProperty("affirmpayment_contentType"));
		}
		
		if (checkProperty("affirmpayment_authorization")) {
			setAuthorization(this.properties.getProperty("affirmpayment_authorization"));
		}
		
		if (this.properties.getProperty("affirmpayment_maxErrorRetry") != null) {
			setMaxErrorRetry(Integer.parseInt(this.properties.getProperty("affirmpayment_maxErrorRetry")));
		}
		
		if (this.properties.getProperty("affirmpayment_maxConnections") != null) {
			setMaxConnections(Integer.parseInt(this.properties.getProperty("affirmpayment_maxConnections")));
		}
		
		if (this.properties.getProperty("affirmpayment_connectionTimeout") != null) {
			setConnectionTimeout(Integer.parseInt(this.properties.getProperty("affirmpayment_connectionTimeout")));
		}
		
		if (this.properties.getProperty("affirmpayment_soTimeout") != null) {
			setSoTimeout(Integer.parseInt(this.properties.getProperty("affirmpayment_soTimeout")));
		}
		
		if (checkProperty("affirmpayment_baseServiceURL")) {
			setBaseServiceURL(this.properties.getProperty("affirmpayment_baseServiceURL"));
		}
		
		if (checkProperty("affirmpayment_authorizeServiceURL")) {
			setAuthorizeServiceURL(this.properties.getProperty("affirmpayment_authorizeServiceURL"));
		}
		
		if (checkProperty("affirmpayment_captureServiceURL")) {
			setCaptureServiceURL(this.properties.getProperty("affirmpayment_captureServiceURL"));
		}
		
		if (checkProperty("affirmpayment_refundServiceURL")) {
			setRefundServiceURL(this.properties.getProperty("affirmpayment_refundServiceURL"));
		}
		
		if (checkProperty("affirmpayment_voidServiceURL")) {
			setVoidServiceURL(this.properties.getProperty("affirmpayment_voidServiceURL"));
		}
		
		if (checkProperty("affirmpayment_updateServiceURL")) {
			setUpdateServiceURL(this.properties.getProperty("affirmpayment_updateServiceURL"));
		}
		
		if (checkProperty("affirmpayment_readServiceURL")) {
			setReadServiceURL(this.properties.getProperty("affirmpayment_readServiceURL"));
		}
	}

	private boolean checkProperty(String paramString) {
		if (this.properties.getProperty(paramString) == null) {
			throw new NullPointerException(new StringBuilder().append(paramString).append(" is not set, this is a required property to execute the action").toString());
		}
		return true;
	}

	public String getPublicApiKey() {
		return publicApiKey;
	}

	public void setPublicApiKey(String publicApiKey) {
		this.publicApiKey = publicApiKey;
	}

	public String getPrivateApiKey() {
		return privateApiKey;
	}

	public void setPrivateApiKey(String privateApiKey) {
		this.privateApiKey = privateApiKey;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getAuthorization() {
		return authorization;
	}

	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}

	public int getMaxErrorRetry() {
		return maxErrorRetry;
	}

	public void setMaxErrorRetry(int maxErrorRetry) {
		this.maxErrorRetry = maxErrorRetry;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getSoTimeout() {
		return soTimeout;
	}

	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public void setBaseServiceURL(String baseServiceURL) {
		this.baseServiceURL = baseServiceURL;
	}

	public String getBaseServiceURL() {
		return baseServiceURL;
	}

	public String getAuthorizeServiceURL() {
		return authorizeServiceURL;
	}

	public void setAuthorizeServiceURL(String authorizeServiceURL) {
		this.authorizeServiceURL = authorizeServiceURL;
	}

	public String getCaptureServiceURL() {
		return captureServiceURL;
	}

	public void setCaptureServiceURL(String captureServiceURL) {
		this.captureServiceURL = captureServiceURL;
	}

	public String getRefundServiceURL() {
		return refundServiceURL;
	}

	public void setRefundServiceURL(String refundServiceURL) {
		this.refundServiceURL = refundServiceURL;
	}

	public String getVoidServiceURL() {
		return voidServiceURL;
	}

	public void setVoidServiceURL(String voidServiceURL) {
		this.voidServiceURL = voidServiceURL;
	}

	public String getUpdateServiceURL() {
		return updateServiceURL;
	}

	public void setUpdateServiceURL(String updateServiceURL) {
		this.updateServiceURL = updateServiceURL;
	}

	public String getReadServiceURL() {
		return readServiceURL;
	}

	public void setReadServiceURL(String readServiceURL) {
		this.readServiceURL = readServiceURL;
	}
	
	public void setPaymentGatewayName(String paymentGatewayName) {
		this.paymentGatewayName = paymentGatewayName;
	}

	public String getPaymentGatewayName() {
		return paymentGatewayName;
	}
	
}
