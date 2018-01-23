package com.deplabs.biz.payment.gateway.affirm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

/**
 * Represents a request to process the Affirm transaction
 * 
 * @author horacioa
 * 
 */
public class AffirmRequest implements Serializable {

	private static final long serialVersionUID = 7907264366302460941L;

	// --- Generic request fields
	/**
	 * The Affirm Request type
	 */
	private AffirmApiAction requestType;

	private Map<String, String> fields = new HashMap<String, String>();

	public static final String CHECKOUT_TOKEN = "checkout_token";
	public static final String ORDER_ID = "order_id";
	
	public static final String CHARGE_ID = "id";

	public static final String SHIPPING_CARRIER = "shipping_carrier";
	public static final String SHIPPING_CONFIRMATION = "shipping_confirmation";

	public static final String AMOUNT = "amount";

	/*
	 * GETTERS & SETTERS
	 */
	
	public String getChargeId() {
		return (fields != null) ? (String)this.fields.get(CHARGE_ID) : null;
	}
	
	public void setChargeId(String id) {
		this.fields.put(CHARGE_ID, id);
	}

	public String getOrderId() {
		return this.fields.get(ORDER_ID);
	}

	public void setOrderId(String orderId) {
		this.fields.put(ORDER_ID, orderId);
	}

	public String getCheckoutToken() {
		return this.fields.get(CHECKOUT_TOKEN);
	}

	public void setCheckoutToken(String checkoutToken) {
		this.fields.put(CHECKOUT_TOKEN, checkoutToken);
	}

	public String getShippingCarrier() {
		return this.fields.get(SHIPPING_CARRIER);
	}

	public void setShippingCarrier(String shipping_carrier) {
		this.fields.put(SHIPPING_CARRIER, shipping_carrier);
	}

	public String getShippingConfirmation() {
		return this.fields.get(SHIPPING_CONFIRMATION);
	}

	public void setShippingConfirmation(String shipping_confirmation) {
		this.fields.put(SHIPPING_CONFIRMATION, shipping_confirmation);
	}

	public String getAmount() {
		return this.fields.get(AMOUNT);
	}

	public void setAmount(String amount) {
		this.fields.put(AMOUNT, amount);
	}

	/**
	 * Converts this request to a JSON String Representation
	 * 
	 * @return
	 */
	public String getContentAsJsonString() {
		if (fields != null)
			return JSONObject.toJSONString(fields);
		else 
			return null;
	}

	public String toString() {
		return getContentAsJsonString();
	}

	public void setRequestType(AffirmApiAction requestType) {
		this.requestType = requestType;
	}

	public AffirmApiAction getRequestType() {
		return requestType;
	}

}
