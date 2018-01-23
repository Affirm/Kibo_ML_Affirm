package com.deplabs.app.service.logger;

import com.marketlive.struts.AbstractItemForm;

public class DeplabsLoggerForm extends AbstractItemForm {
	
	private String categoriesLevel = "";
	private String error = "";

	public String getCategoriesLevel() {
		return categoriesLevel;
	}

	public void setCategoriesLevel(String categoriesLevel) {
		this.categoriesLevel = categoriesLevel;
	}

	public String getError() {
		return error;
	}

	
	public void setError(String error) {
		this.error=error; 		
	}
	


    
}
