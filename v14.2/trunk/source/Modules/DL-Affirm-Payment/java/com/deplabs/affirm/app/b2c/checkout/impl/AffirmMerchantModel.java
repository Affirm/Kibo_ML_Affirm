package com.deplabs.affirm.app.b2c.checkout.impl;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

import com.deplabs.affirm.app.b2c.checkout.IAffirmMerchantModel;
import com.marketlive.app.common.components.ModelData;

@JsonPropertyOrder({IAffirmMerchantModel.PUBLIC_API_KEY,IAffirmMerchantModel.USER_CANCEL_URL,IAffirmMerchantModel.USER_CONFIRMATION_URL,IAffirmMerchantModel.USER_CONFIRMATION_URL_ACTION})
public class AffirmMerchantModel extends ModelData implements IAffirmMerchantModel {

	@Override
	@JsonProperty("user_cancel_url")
	public String getUserCancelUrl() {
		Object attribute = getAttribute(USER_CANCEL_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("user_cancel_url")
	public void setUserCancelUrl(String cancelUrl) {
		setAttribute(USER_CANCEL_URL,cancelUrl);
		
	}

	@Override
	@JsonProperty("user_confirmation_url")
	public String getUserConfirmationUrl() {
		Object attribute = getAttribute(USER_CONFIRMATION_URL);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("user_confirmation_url")
	public void setUserConfirmationUrl(String confirmationUrl) {
		setAttribute(USER_CONFIRMATION_URL,confirmationUrl);
		
	}

	@Override
	@JsonProperty("user_confirmation_url_action")
	public String getUserConfirmationUrlAction() {
		Object attribute = getAttribute(USER_CONFIRMATION_URL_ACTION);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("user_confirmation_url_action")
	public void setUserConfirmationUrlAction(String confirmationUrlAction) {
		setAttribute(USER_CONFIRMATION_URL_ACTION,confirmationUrlAction);
		
	}

	@Override
	@JsonProperty("public_api_key")
	public String getPublicApiKey() {
		Object attribute = getAttribute(PUBLIC_API_KEY);
		return (attribute != null) ? ((String) attribute) : null;
	}

	@Override
	@JsonProperty("public_api_key")
	public void setPublicApiKey(String publicApiKey) {
		setAttribute(PUBLIC_API_KEY,publicApiKey);
		
	}

}
