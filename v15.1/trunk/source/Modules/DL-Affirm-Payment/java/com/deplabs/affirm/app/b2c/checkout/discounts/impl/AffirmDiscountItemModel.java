package com.deplabs.affirm.app.b2c.checkout.discounts.impl;

import com.deplabs.affirm.app.b2c.checkout.discounts.IAffirmDiscountItemModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmDiscountItemModel.DISCOUNT_AMOUNT,IAffirmDiscountItemModel.DISCOUNT_DISPLAY_NAME})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)

public class AffirmDiscountItemModel extends ModelData implements IAffirmDiscountItemModel{

	@Override
	public String getDiscountAmount() {
		Object attribute = getAttribute(DISCOUNT_AMOUNT);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setDiscountAmount(String discountAmount) {
		setAttribute(DISCOUNT_AMOUNT,discountAmount);
		
	}

	@Override
	public String getDiscountDisplayName() {
		Object attribute = getAttribute(DISCOUNT_DISPLAY_NAME);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setDiscountDisplayName(String discountDisplayName) {
		setAttribute(DISCOUNT_DISPLAY_NAME,discountDisplayName);
		
	}

}
