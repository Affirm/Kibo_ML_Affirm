package com.deplabs.affirm.app.b2c.checkout;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmConfigCheckoutModel extends IModelData {

	String FINANCIAL_PRODUCT_KEY = "financial_product_key";
	
	String getFinancialProductKey();
	void setFinancialProductKey(String financialProductKey);
}
