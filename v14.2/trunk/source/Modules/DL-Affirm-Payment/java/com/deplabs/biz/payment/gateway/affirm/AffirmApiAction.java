package com.deplabs.biz.payment.gateway.affirm;


/**
 * Enum class that represents the different Affirm API actions: authorize,
 * capture, void, refund, update, read
 * 
 * @author horacioa
 * 
 */
public enum AffirmApiAction {

	AUTHORIZE("AUTH", "POST"), CAPTURE("CAPTURE", "POST"), VOID("VOID", "POST"), REFUND("REFUND", "POST"), UPDATE("UPDATE", "POST"), READ("READ", "GET");

	private String method;
	private String name;

	private AffirmApiAction(String name, String method) {
		this.setMethod(method);
		this.setName(name);
	}

	private void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public String getApiActionName() {
		return name().toLowerCase();
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
