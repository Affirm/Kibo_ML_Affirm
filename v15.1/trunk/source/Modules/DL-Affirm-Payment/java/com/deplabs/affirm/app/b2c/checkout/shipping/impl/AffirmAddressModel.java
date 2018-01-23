package com.deplabs.affirm.app.b2c.checkout.shipping.impl;

import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmAddressModel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmAddressModel.LINE1,IAffirmAddressModel.LINE2, IAffirmAddressModel.CITY, IAffirmAddressModel.STATE, IAffirmAddressModel.ZIP_CODE})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class AffirmAddressModel  extends ModelData implements IAffirmAddressModel {

	@Override
	public String getLine1() {
		Object attribute = getAttribute(LINE1);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setLine1(String line1) {
		setAttribute(LINE1, line1);
		
	}

	@Override
	public String getLine2() {
		Object attribute = getAttribute(LINE2);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setLine2(String line2) {
		setAttribute(LINE2, line2);
		
	}

	@Override
	public String getCity() {
		Object attribute = getAttribute(CITY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setCity(String city) {
		setAttribute(CITY, city);
		
	}

	@Override
	public String getState() {
		Object attribute = getAttribute(STATE);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setState(String state) {
		setAttribute(STATE, state);
		
	}

	@Override
	public String getZipcode() {
		Object attribute = getAttribute(ZIP_CODE);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setZipcode(String zipCode) {
		setAttribute(ZIP_CODE, zipCode);
		
	}
	
}
