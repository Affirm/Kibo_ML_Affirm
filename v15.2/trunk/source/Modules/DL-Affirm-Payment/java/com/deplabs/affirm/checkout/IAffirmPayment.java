package com.deplabs.affirm.checkout;

public abstract interface IAffirmPayment {

	  public abstract String getToken();

	  public abstract void setToken(String paramString);
}
