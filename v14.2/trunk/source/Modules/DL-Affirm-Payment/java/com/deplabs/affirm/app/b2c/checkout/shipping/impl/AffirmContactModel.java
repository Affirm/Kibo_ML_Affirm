package com.deplabs.affirm.app.b2c.checkout.shipping.impl;

import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmAddressModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmContactModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmPersonModel;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmContactModel.PERSON,IAffirmContactModel.ADDRESS})
public class AffirmContactModel extends ModelData  implements IAffirmContactModel {

	@Override
	public IAffirmPersonModel getName() {
		return getAttribute(PERSON);
	}

	@Override
	public void setName(IAffirmPersonModel name) {
		setAttribute(PERSON, name);
	}

	@Override
	public IAffirmAddressModel getAddress() {
		return getAttribute(ADDRESS);
	}

	@Override
	public void setAddress(IAffirmAddressModel address) {
		setAttribute(ADDRESS,address);
	}

}
