package com.deplabs.affirm.app.b2c.checkout.impl;

import com.deplabs.affirm.app.b2c.checkout.IAffirmMetadataCheckoutModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({ IAffirmMetadataCheckoutModel.SHIPPING_TYPE, IAffirmMetadataCheckoutModel.PLATFORM_VERSION, IAffirmMetadataCheckoutModel.ORDER_ID,
		IAffirmMetadataCheckoutModel.PLATFORM_AFFIRM, IAffirmMetadataCheckoutModel.ORDER_KEY, IAffirmMetadataCheckoutModel.PLATFORM_TYPE })
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
@JsonIgnoreProperties({"attribute_map"})
public class AffirmMetadataCheckoutModel extends ModelData implements IAffirmMetadataCheckoutModel {

	@Override
	public void setShippingType(String shippingType) {
		setAttribute(SHIPPING_TYPE, shippingType);
	}

	@Override
	public String getShippingType() {
		Object attribute = getAttribute(SHIPPING_TYPE);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setPlatformVersion(String platformVersion) {
		setAttribute(PLATFORM_VERSION, platformVersion);
	}

	@Override
	public String getPlatformVersion() {
		Object attribute = getAttribute(PLATFORM_VERSION);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setOrderId(String orderId) {
		setAttribute(ORDER_ID, orderId);
	}

	@Override
	public String getOrderId() {
		Object attribute = getAttribute(ORDER_ID);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setPlatformAffirm(String platformAffirm) {
		setAttribute(PLATFORM_AFFIRM, platformAffirm);
	}

	@Override
	public String getPlatformAffirm() {
		Object attribute = getAttribute(PLATFORM_AFFIRM);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setOrderKey(String orderKey) {
		setAttribute(ORDER_KEY, orderKey);
	}

	@Override
	public String getOrderKey() {
		Object attribute = getAttribute(ORDER_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setPlatformType(String platformType) {
		setAttribute(PLATFORM_TYPE, platformType);
	}

	@Override
	public String getPlatformType() {
		Object attribute = getAttribute(PLATFORM_TYPE);
		return (attribute != null) ? ((String) attribute) : null;
	}

}
