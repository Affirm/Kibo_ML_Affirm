package com.deplabs.affirm.app.b2c.checkout.shipping.impl;

import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmAddressModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmPersonModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmContactModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmContactModel.PERSON,IAffirmContactModel.ADDRESS})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
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
