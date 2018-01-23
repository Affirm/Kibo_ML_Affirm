package com.deplabs.affirm.app.b2c.checkout;

import com.marketlive.app.common.components.IModelData;


public interface IAffirmMetadataCheckoutModel extends IModelData {
	
	String SHIPPING_TYPE = "shipping_type";
	String PLATFORM_VERSION = "platform_version";
	String ORDER_ID = "order_id";
	String PLATFORM_AFFIRM = "platform_affirm";
	String ORDER_KEY = "order_key";
	String PLATFORM_TYPE = "platform_type";	
	
	void setShippingType(String shippingType);
	String getShippingType();
	
	void setPlatformVersion(String platformVersion);
	String getPlatformVersion();
	
	void setOrderId(String orderId);
	String getOrderId();
	
	void setPlatformAffirm(String platformAffirm);
	String getPlatformAffirm();
	
	void setOrderKey(String orderKey);
	String getOrderKey();
	
	void setPlatformType(String platformType);
	String getPlatformType();
}
