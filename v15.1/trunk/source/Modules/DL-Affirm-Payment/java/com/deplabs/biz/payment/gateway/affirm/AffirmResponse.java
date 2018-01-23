/**
 * 
 */
package com.deplabs.biz.payment.gateway.affirm;

import org.json.simple.JSONObject;

/**
 * 
 * Represents the Affirm response to a request to process the Affirm Payment
 * transaction.
 * 
 * @author horacioa
 */
public class AffirmResponse {
	
	// --- Error response fields
	
	public static final String ERROR_STATUS_CODE = "status_code";
	public static final String ERROR_MESSAGE = "message";
	public static final String ERROR_CODE = "code";
	public static final String ERROR_TYPE = "type";
	
	// --- Authorization response fields
	public static final String CHARGE_ID = "id";
	public static final String AMOUNT = "amount";
	public static final String AUTH_HOLD = "auth_hold";
	public static final String MERCHANT_EXTERNAL_REFERENCE = "merchant_external_reference";
	public static final String STATUS = "status";
	
	// --- Capture response fields
	public static final String FEE = "fee";
	public static final String CREATED = "created";
	public static final String ORDER_ID = "order_id";
	public static final String CURRENCY = "currency";
	public static final String TYPE = "type";
	public static final String TRANSACTION_ID = "transaction_id";
	
	// --- Capture response fields
	public static final String FEE_REFUNDED = "fee_refunded";
	
	// --- Update response fields
	public static final String SHIPPING_CARRIER = "shipping_carrier";
	public static final String SHIPPING_CONFIRMATION = "shipping_confirmation";

	// --- Generic response fields
    
	// name of the payment gateway that generated this response
    private String paymentGateway;
    
    // was the requested transaction successfully processed
    private boolean success = false;
    
    private JSONObject fields;
    
	// a gateway specific code indicating the response type for a request
    private String responseCode;
    // a gateway specific code indicating the reason for a received response
    private String reasonCode;
    // a message describing the result of the requested transaction
    private String responseMessage;
	
	/**
	 * The HTTP response body
	 */
	private String responseBody;
	
	/**
     *  Returns true if the requested transaction was successfully processed
     *
     *  @return boolean
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets a boolean to indicate if the requested transaction was successfully processed
     *
     * @param   success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

	public String getPaymentGateway() {
		return paymentGateway;
	}

	public void setPaymentGateway(String paymentGateway) {
		this.paymentGateway = paymentGateway;
	}
	
	public String getChargeId() {
		return (fields != null) ? (String)this.fields.get(CHARGE_ID) : null;
	}

	public String getOrderId() {
		return (fields != null) ? (String)this.fields.get(ORDER_ID) : null;
	}

	public String getShippingCarrier() {
		return (fields != null) ? (String)this.fields.get(SHIPPING_CARRIER) : null;
	}

	public String getShippingConfirmation() {
		return (fields != null) ? (String)this.fields.get(SHIPPING_CONFIRMATION) : null;
	}

	public Long getAmount() {
		return (fields != null) ? (Long)this.fields.get(AMOUNT) : null;
	}

	public Long getAuthHold() {
		return (fields != null) ? (Long)this.fields.get(AUTH_HOLD) : null;
	}

	public Long getFee() {
		return (fields != null) ? (Long)this.fields.get(FEE) : null;
	}
	
	public String getCreated() {
		return (fields != null) ? (String)this.fields.get(CREATED) : null;
	}

	public String getCurrency() {
		return (fields != null) ? (String)this.fields.get(CURRENCY) : null;	}

	public String getType() {
		return (fields != null) ? (String)this.fields.get(TYPE) : null;
	}

	public String getTransactionId() {
		return (fields != null) ? (String)this.fields.get(TRANSACTION_ID) : null;
	}

	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getReasonCode() {
		return reasonCode;
	}
	
	public String getMerchantExternalReference() {
		return (fields != null) ? (String)this.fields.get(MERCHANT_EXTERNAL_REFERENCE) : null;
	}

	@Override
	public String toString() {
		return "AffirmResponse [paymentGateway=" + paymentGateway + ", success=" + success + ", fields=" + fields + ", responseCode=" + responseCode + ", reasonCode=" + reasonCode
				+ ", responseMessage=" + responseMessage + ", responseBody=" + responseBody + "]";
	}
	
	public void setFields(JSONObject fields) {
		this.fields = fields;
	}

	public JSONObject getFields() {
		return fields;
	}
	
	public String getField (String key) {
		return (fields != null) ? (String)this.fields.get(key) : null;
	}
	
}
