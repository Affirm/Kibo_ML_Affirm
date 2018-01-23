package com.deplabs.affirm.app.b2c.checkout.impl;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.deplabs.affirm.app.b2c.checkout.IAffirmConfigCheckoutModel;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmConfigCheckoutModel.FINANCIAL_PRODUCT_KEY})
public class AffirmConfigCheckoutModel extends ModelData implements IAffirmConfigCheckoutModel {

	@Override
	@JsonProperty("financial_product_key")
	public String getFinancialProductKey() {
		Object attribute = getAttribute(FINANCIAL_PRODUCT_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("financial_product_key")
	public void setFinancialProductKey(String financialProductKey) {
		setAttribute(FINANCIAL_PRODUCT_KEY, financialProductKey);
		
	}

}
