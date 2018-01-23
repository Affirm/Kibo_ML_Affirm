package com.deplabs.affirm.app.b2c.checkout.impl;

import com.deplabs.affirm.app.b2c.checkout.IAffirmMerchantModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmMerchantModel.PUBLIC_API_KEY,IAffirmMerchantModel.USER_CANCEL_URL,IAffirmMerchantModel.USER_CONFIRMATION_URL,IAffirmMerchantModel.USER_CONFIRMATION_URL_ACTION})
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class) 
@JsonIgnoreProperties({"attribute_map"})
public class AffirmMerchantModel extends ModelData implements IAffirmMerchantModel {

	@Override
	public String getUserCancelUrl() {
		Object attribute = getAttribute(USER_CANCEL_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setUserCancelUrl(String cancelUrl) {
		setAttribute(USER_CANCEL_URL,cancelUrl);
		
	}

	@Override
	public String getUserConfirmationUrl() {
		Object attribute = getAttribute(USER_CONFIRMATION_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setUserConfirmationUrl(String confirmationUrl) {
		setAttribute(USER_CONFIRMATION_URL,confirmationUrl);
		
	}

	@Override
	public String getUserConfirmationUrlAction() {
		Object attribute = getAttribute(USER_CONFIRMATION_URL_ACTION);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setUserConfirmationUrlAction(String confirmationUrlAction) {
		setAttribute(USER_CONFIRMATION_URL_ACTION,confirmationUrlAction);
		
	}

	@Override
	public String getPublicApiKey() {
		Object attribute = getAttribute(PUBLIC_API_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	public void setPublicApiKey(String publicApiKey) {
		setAttribute(PUBLIC_API_KEY,publicApiKey);
		
	}

}
