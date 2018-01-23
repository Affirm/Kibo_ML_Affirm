package com.deplabs.affirm.app.b2c.checkout.cart.impl;

import com.deplabs.affirm.app.b2c.checkout.cart.IAffirmBasketItemModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmBasketItemModel.DISPLAY_NAME,IAffirmBasketItemModel.SKU,IAffirmBasketItemModel.UNIT_PRICE,IAffirmBasketItemModel.QTY,
	IAffirmBasketItemModel.ITEM_IMAGE_URL,IAffirmBasketItemModel.ITEM_URL}
)
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class AffirmBasketItemModel extends ModelData implements IAffirmBasketItemModel{

	@Override
	public String getDisplayName() {
		Object attribute = getAttribute(DISPLAY_NAME);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setDisplayName(String displayName) {
		setAttribute(DISPLAY_NAME,displayName);
		
	}

	@Override
	public String getSku() {
		Object attribute = getAttribute(SKU);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setSku(String sku) {
		setAttribute(SKU,sku);
	}

	@Override
	public String getUnitPrice() {
		Object attribute = getAttribute(UNIT_PRICE);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setUnitPrice(String unitPrice) {
		setAttribute(UNIT_PRICE,unitPrice);
	}

	@Override
	public String getQty() {
		Object attribute = getAttribute(QTY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setQty(String qty) {
		setAttribute(QTY,qty);
		
	}

	@Override
	public String getItemImageUrl() {
		Object attribute = getAttribute(ITEM_IMAGE_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setItemImageUrl(String itemImageUrl) {
		setAttribute(ITEM_IMAGE_URL,itemImageUrl);
	}

	@Override
	public String getItemUrl() {
		Object attribute = getAttribute(ITEM_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setItemUrl(String itemUrl) {
		setAttribute(ITEM_URL,itemUrl);
	}

}
