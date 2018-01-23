package com.deplabs.affirm.app.b2c.checkout.impl;

import java.util.List;
import java.util.Map;

import com.deplabs.affirm.app.b2c.checkout.IAffirmCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmConfigCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmMerchantModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmMetadataCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.cart.IAffirmBasketItemModel;
import com.deplabs.affirm.app.b2c.checkout.discounts.IAffirmDiscountItemModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmContactModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmCheckoutModel.CONFIG, IAffirmCheckoutModel.MERCHANT, IAffirmCheckoutModel.SHIPPING_INFO, IAffirmCheckoutModel.BILLING_INFO,IAffirmCheckoutModel.ITEMS,IAffirmCheckoutModel.CURRENCY, 
	IAffirmCheckoutModel.DISCOUNTS,IAffirmCheckoutModel.METADATA,IAffirmCheckoutModel.ORDER_ID,IAffirmCheckoutModel.TAX_AMOUNT, IAffirmCheckoutModel.SHIPPING_AMOUNT, IAffirmCheckoutModel.TOTAL, IAffirmCheckoutModel.FINANCING_PROGRAM})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)	
public class AffirmCheckoutModel extends ModelData implements IAffirmCheckoutModel {

	@Override
	public IAffirmConfigCheckoutModel getConfig() {
		return getAttribute(CONFIG);
	}

	@Override
	public void setConfig(IAffirmConfigCheckoutModel config) {
		setAttribute(CONFIG,config);
		
	}

	@Override
	public IAffirmMerchantModel getMerchant() {
		return getAttribute(MERCHANT);
	}

	@Override
	public void setMerchant(IAffirmMerchantModel merchant) {
		setAttribute(MERCHANT,merchant);
		
	}

	@Override
	public IAffirmContactModel getShipping() {
		return getAttribute(SHIPPING_INFO);
	}

	@Override
	public void setBilling(IAffirmContactModel billingInfo) {
		setAttribute(BILLING_INFO,billingInfo);
		
	}

	@Override
	public IAffirmContactModel getBilling() {
		return getAttribute(BILLING_INFO);
	}

	@Override
	public void setShipping(IAffirmContactModel shippingInfo) {
		setAttribute(SHIPPING_INFO,shippingInfo);
		
	}
	@Override
	public List<IAffirmBasketItemModel> getItems() {
		return getAttribute(ITEMS);
	}

	@Override
	public void setItems(List<IAffirmBasketItemModel> basketItems) {
		setAttribute(ITEMS,basketItems);
		
	}

	@Override
	public String getCurrency() {
		Object attribute = getAttribute(CURRENCY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setCurrency(String currency) {
		setAttribute(CURRENCY,currency);
		
	}

	@Override
	public Map<String, IAffirmDiscountItemModel> getDiscounts() {
		return getAttribute(DISCOUNTS);
	}

	@Override
	public void setDiscounts(Map<String, IAffirmDiscountItemModel> discounts) {
		setAttribute(DISCOUNTS,discounts);
		
	}

	@Override
	public String getTaxAmount() {
		Object attribute = getAttribute(TAX_AMOUNT);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setTaxAmount(String taxAmount) {
		setAttribute(TAX_AMOUNT,taxAmount);
		
	}

	@Override
	public String getShippingAmount() {
		Object attribute = getAttribute(SHIPPING_AMOUNT);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setShippingAmount(String shippingAmount) {
		setAttribute(SHIPPING_AMOUNT,shippingAmount);
		
	}

	@Override
	public String getTotal() {
		Object attribute = getAttribute(TOTAL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setTotal(String totalAmount) {
		setAttribute(TOTAL,totalAmount);
	}

	@Override
	public IAffirmMetadataCheckoutModel getMetadata() {
		return getAttribute(METADATA);
	}

	@Override
	public void setMetadata(IAffirmMetadataCheckoutModel metadata) {
		setAttribute(METADATA, metadata);
		
	}

	@Override
	public String getOrderId() {
		Object attribute = getAttribute(ORDER_ID);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setOrderId(String orderId) {
		setAttribute(ORDER_ID,orderId);
	}

	@Override
	public String getFinancingProgram() {
		Object attribute = getAttribute(FINANCING_PROGRAM);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setFinancingProgram(String financingProgram) {
		setAttribute(FINANCING_PROGRAM, financingProgram);
	}
}
