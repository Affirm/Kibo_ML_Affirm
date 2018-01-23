package com.deplabs.affirm.app.b2c.checkout.cart;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmBasketItemModel extends IModelData {
	
	String DISPLAY_NAME = "display_name";
	String SKU = "sku";
	String UNIT_PRICE = "unit_price";
	String QTY = "qty";
	String ITEM_IMAGE_URL = "item_image_url";
	String ITEM_URL = "item_url";	
	
	String getDisplayName();
	void setDisplayName(String displayName);
	
	String getSku();
	void setSku(String sku);
	
	String getUnitPrice();
	void setUnitPrice(String unitPrice);
	
	String getQty();
	void setQty(String qty);
	
	String getItemImageUrl();
	void setItemImageUrl(String itemImageUrl);
	
	String getItemUrl();
	void setItemUrl(String itemUrl);

}
