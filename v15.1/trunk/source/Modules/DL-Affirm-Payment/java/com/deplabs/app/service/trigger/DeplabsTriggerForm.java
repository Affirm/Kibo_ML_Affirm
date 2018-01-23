package com.deplabs.app.service.trigger;

import com.marketlive.struts.AbstractItemForm;

public class DeplabsTriggerForm extends AbstractItemForm {
	
	private String result= "";
	private String error = "";

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result= result;
	}

	public String getError() {
		return error;
	}

	
	public void setError(String error) {
		this.error=error; 		
	}
	


    
}
