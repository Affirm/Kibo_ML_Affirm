package com.deplabs.affirm.app.b2c.checkout;

import java.util.List;
import java.util.Map;

import com.deplabs.affirm.app.b2c.checkout.cart.IAffirmBasketItemModel;
import com.deplabs.affirm.app.b2c.checkout.discounts.IAffirmDiscountItemModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmContactModel;
import com.marketlive.app.common.components.IModelData;

public interface IAffirmCheckoutModel extends IModelData {
	
	String CONFIG = "config";
	String MERCHANT = "merchant";
	String SHIPPING_INFO = "shipping";
	String BILLING_INFO = "billing";
	String ITEMS = "items";
	String CURRENCY = "currency";
	String DISCOUNTS = "discounts";
	String TAX_AMOUNT = "tax_amount";
	String SHIPPING_AMOUNT = "shipping_amount";
	String TOTAL = "total";
	String ORDER_ID = "order_id";
	String METADATA = "metadata";
	String FINANCING_PROGRAM = "financing_program";
	
	IAffirmConfigCheckoutModel getConfig();
	void setConfig(IAffirmConfigCheckoutModel config);
	
	IAffirmMerchantModel getMerchant();
	void setMerchant(IAffirmMerchantModel merchant);
	
	IAffirmContactModel getShipping();
	void setShipping(IAffirmContactModel shippingInfo);
	
	IAffirmContactModel getBilling();
	void setBilling(IAffirmContactModel shippingInfo);
	
	List<IAffirmBasketItemModel> getItems();
	void setItems(List<IAffirmBasketItemModel> basketItems);
	
	IAffirmMetadataCheckoutModel getMetadata();
	void setMetadata(IAffirmMetadataCheckoutModel metadata);
	
	String getCurrency();
	void setCurrency(String currency);
	
	Map<String, IAffirmDiscountItemModel> getDiscounts();
	void setDiscounts(Map<String, IAffirmDiscountItemModel> discounts);
	
	String getTaxAmount();
	void setTaxAmount(String taxAmount);
	
	String getShippingAmount();
	void setShippingAmount(String shippingAmount);
	
	String getTotal();
	void setTotal(String totalAmount);

	String getOrderId();
	void setOrderId(String orderId);
	
	String getFinancingProgram();
	void setFinancingProgram(String financingProgram);

}

