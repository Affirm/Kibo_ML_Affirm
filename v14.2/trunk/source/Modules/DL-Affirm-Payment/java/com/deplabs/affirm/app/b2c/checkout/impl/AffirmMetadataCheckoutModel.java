package com.deplabs.affirm.app.b2c.checkout.impl;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.deplabs.affirm.app.b2c.checkout.IAffirmMetadataCheckoutModel;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmMetadataCheckoutModel.SHIPPING_TYPE})
public class AffirmMetadataCheckoutModel extends ModelData implements IAffirmMetadataCheckoutModel {

	@Override
	@JsonProperty("shipping_type")
	public void setShippingType(String shippingType) {
		setAttribute(SHIPPING_TYPE, shippingType);
	}

	@Override
	@JsonProperty("shipping_type")
	public String getShippingType() {
		Object attribute = getAttribute(SHIPPING_TYPE);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("platform_version")
	public void setPlatformVersion(String platformVersion) {
		setAttribute(PLATFORM_VERSION, platformVersion);
	}

	@Override
	@JsonProperty("platform_version")
	public String getPlatformVersion() {
		Object attribute = getAttribute(PLATFORM_VERSION);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("order_id")
	public void setOrderId(String orderId) {
		setAttribute(ORDER_ID, orderId);
	}

	@Override
	@JsonProperty("order_id")
	public String getOrderId() {
		Object attribute = getAttribute(ORDER_ID);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("platform_affirm")
	public void setPlatformAffirm(String platformAffirm) {
		setAttribute(PLATFORM_AFFIRM, platformAffirm);
	}

	@Override
	@JsonProperty("platform_affirm")
	public String getPlatformAffirm() {
		Object attribute = getAttribute(PLATFORM_AFFIRM);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("order_key")
	public void setOrderKey(String orderKey) {
		setAttribute(ORDER_KEY, orderKey);
	}

	@Override
	@JsonProperty("order_key")
	public String getOrderKey() {
		Object attribute = getAttribute(ORDER_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("platform_type")
	public void setPlatformType(String platformType) {
		setAttribute(PLATFORM_TYPE, platformType);
	}

	@Override
	@JsonProperty("platform_type")
	public String getPlatformType() {
		Object attribute = getAttribute(PLATFORM_TYPE);
		return (attribute != null) ? ((String) attribute) : null;
	}

}
