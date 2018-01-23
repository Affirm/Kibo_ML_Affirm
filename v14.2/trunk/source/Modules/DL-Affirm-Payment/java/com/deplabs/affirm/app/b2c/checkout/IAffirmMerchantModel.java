package com.deplabs.affirm.app.b2c.checkout;

import org.codehaus.jackson.annotate.JsonProperty;

import com.marketlive.app.common.components.IModelData;

public interface IAffirmMerchantModel extends IModelData {
	
	@JsonProperty("public_api_key")
	String PUBLIC_API_KEY = "public_api_key";
	
	@JsonProperty("user_cancel_url")
	String USER_CANCEL_URL = "user_cancel_url";
	
	@JsonProperty("user_confirmation_url")
	String USER_CONFIRMATION_URL = "user_confirmation_url";
	
	@JsonProperty("user_confirmation_url_action")
	String USER_CONFIRMATION_URL_ACTION = "user_confirmation_url_action";

	
	String getPublicApiKey();
	void setPublicApiKey(String publicApiKey);
	
	String getUserCancelUrl();
	void setUserCancelUrl(String cancelUrl);
	
	String getUserConfirmationUrl();
	void setUserConfirmationUrl(String confirmationUrl);
	
	
	String getUserConfirmationUrlAction();
	void setUserConfirmationUrlAction(String confirmationUrlAction);
}
