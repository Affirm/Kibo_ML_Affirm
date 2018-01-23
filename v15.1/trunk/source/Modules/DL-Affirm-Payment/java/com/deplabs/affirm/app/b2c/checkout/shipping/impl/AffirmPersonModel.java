package com.deplabs.affirm.app.b2c.checkout.shipping.impl;

import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmPersonModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmPersonModel.FIRST_NAME,IAffirmPersonModel.LAST_NAME})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class AffirmPersonModel extends ModelData implements IAffirmPersonModel {

	@Override
	public String getFirst() {
		Object attribute = getAttribute(FIRST_NAME);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setFirst(String firstName) {
		setAttribute(FIRST_NAME, firstName);
	}

	@Override
	public String getLast() {
		Object attribute = getAttribute(LAST_NAME);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setLast(String lastName) {
		setAttribute(LAST_NAME, lastName);
	}

}
