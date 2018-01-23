package com.deplabs.affirm.checkout.impl;

import com.deplabs.affirm.checkout.IAffirmPayment;

public class AffirmPayment implements IAffirmPayment {

	private String token;
	
	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void setToken(String paramString) {
		this.token = paramString;
	}
	

}
