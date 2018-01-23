package com.deplabs.affirm.app.b2c.checkout.shipping;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmAddressModel extends IModelData {
	
	String LINE1 = "line1";
	String LINE2 = "line2";
	String CITY = "city";
	String STATE = "state";
	String ZIP_CODE = "zipcode";
	
	String getLine1();
	void setLine1(String line1);
	
	String getLine2();
	void setLine2(String line2);
	
	String getCity();
	void setCity(String city);
	
	String getState();
	void setState(String state);
	
	String getZipcode();
	void setZipcode(String zipCode);

}
