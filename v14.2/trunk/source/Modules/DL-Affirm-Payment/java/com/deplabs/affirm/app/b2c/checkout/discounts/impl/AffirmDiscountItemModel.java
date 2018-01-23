package com.deplabs.affirm.app.b2c.checkout.discounts.impl;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.PropertyNamingStrategy;

import com.deplabs.affirm.app.b2c.checkout.discounts.IAffirmDiscountItemModel;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmDiscountItemModel.DISCOUNT_AMOUNT,IAffirmDiscountItemModel.DISCOUNT_DISPLAY_NAME})
public class AffirmDiscountItemModel extends ModelData implements IAffirmDiscountItemModel{
	
	@Override
	@JsonProperty("discount_amount")
	public String getDiscountAmount() {
		Object attribute = getAttribute(DISCOUNT_AMOUNT);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("discount_amount")
	public void setDiscountAmount(String discountAmount) {
		setAttribute(DISCOUNT_AMOUNT,discountAmount);
		
	}

	@Override
	@JsonProperty("discount_display_name")
	public String getDiscountDisplayName() {
		Object attribute = getAttribute(DISCOUNT_DISPLAY_NAME);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("discount_display_name")
	public void setDiscountDisplayName(String discountDisplayName) {
		setAttribute(DISCOUNT_DISPLAY_NAME,discountDisplayName);
	}

}
