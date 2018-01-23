package com.deplabs.affirm.app.b2c.checkout.discounts;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmDiscountItemModel extends IModelData {

	String DISCOUNT_AMOUNT = "discount_amount";
	String DISCOUNT_DISPLAY_NAME = "discount_display_name";
	
	String getDiscountAmount();
	void setDiscountAmount(String discountAmount);
	
	String getDiscountDisplayName();
	void setDiscountDisplayName(String discountDisplayName);
}
