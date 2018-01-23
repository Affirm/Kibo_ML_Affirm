package com.deplabs.affirm.app.b2c.checkout.shipping;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmContactModel extends IModelData {
	
	String PERSON = "name";
    String ADDRESS = "address";
    
    IAffirmPersonModel getName();
    void setName(IAffirmPersonModel name);
    
    IAffirmAddressModel getAddress();
    void setAddress(IAffirmAddressModel address);
 }
