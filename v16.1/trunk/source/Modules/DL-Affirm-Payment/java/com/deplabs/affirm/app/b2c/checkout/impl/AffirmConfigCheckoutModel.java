package com.deplabs.affirm.app.b2c.checkout.impl;

import com.deplabs.affirm.app.b2c.checkout.IAffirmConfigCheckoutModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmConfigCheckoutModel.FINANCIAL_PRODUCT_KEY})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
@JsonIgnoreProperties({"attribute_map"})
public class AffirmConfigCheckoutModel extends ModelData implements IAffirmConfigCheckoutModel {

	@Override
	public String getFinancialProductKey() {
		Object attribute = getAttribute(FINANCIAL_PRODUCT_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setFinancialProductKey(String financialProductKey) {
		setAttribute(FINANCIAL_PRODUCT_KEY, financialProductKey);
		
	}

}
