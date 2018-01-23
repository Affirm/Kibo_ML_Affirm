package com.deplabs.affirm.app.b2c.checkout.shipping;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmPersonModel extends IModelData {

	String FIRST_NAME = "first";
	String LAST_NAME = "last";

	String getFirst();
	void setFirst(String firstName);
	
	String getLast();
	void setLast(String lastName);
	

}
