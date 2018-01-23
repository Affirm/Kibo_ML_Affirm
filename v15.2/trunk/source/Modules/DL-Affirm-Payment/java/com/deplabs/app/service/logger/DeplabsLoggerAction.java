package com.deplabs.app.service.logger;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.marketlive.struts.MMLLookupDispatchAction;

import freemarker.template.utility.StringUtil;

public class DeplabsLoggerAction extends MMLLookupDispatchAction {
	
	private static Log log = LogFactory.getLog(DeplabsLoggerAction.class);
	
	public static final String LIST_FORM = "listAll";
	public static final String UPDATE_FORM = "updateLogger";
	public static final String REMOVE_FORM = "removeLogger";
	
	
	public ActionForward listAll(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {	
		DeplabsLoggerForm loggerForm = (DeplabsLoggerForm)form;
		validateList(loggerForm);
		
		loggerForm.setCategoriesLevel(listAllLoggers(null));
		printSamples();
		
		return (new ActionForward(mapping.getInput()));
    }
	
	
	public ActionForward updateLogger(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		DeplabsLoggerForm loggerForm = (DeplabsLoggerForm)form;
		String logger=request.getParameter("class");  
		String level = request.getParameter("level");
		if(validateChange(logger,level,loggerForm)){
		    Logger.getLogger(logger).setLevel(Level.toLevel(level, Level.INFO));
		}
		
		loggerForm.setCategoriesLevel(listAllLoggers(logger));
		printSamples();	
		
		return (new ActionForward(mapping.getInput()));
    }
	
   


	public ActionForward removeLogger(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		DeplabsLoggerForm loggerForm = (DeplabsLoggerForm)form;
		String logger=request.getParameter("class");  
	
		if(validateRemove(logger,loggerForm)){
		    Logger logClass = Logger.getLogger(logger);
		    if(logClass!=null) logClass.setLevel(null);
		} 
		loggerForm.setCategoriesLevel(listAllLoggers(logger));
		printSamples();		
		
		return (new ActionForward(mapping.getInput()));
    }	
	
	
	private String listAllLoggers(String currentChanged) {

    	StringBuilder categoriesOut = new StringBuilder();
    	Enumeration categories = Logger.getRootLogger().getLoggerRepository().getCurrentCategories();
    	categoriesOut.append("<table>");		
    	while(categories.hasMoreElements()){
			Category cat=(Category) categories.nextElement();
			if(cat.getLevel()!=null){
				categoriesOut.append("<tr");
				if(currentChanged!=null && cat.getName().equals(currentChanged)){
					categoriesOut.append(" style=\"color:blue;font-weight: bold;\" ");
				}
				categoriesOut.append("><td>");
				categoriesOut.append(cat.getName());
				categoriesOut.append("</b></td><td>=</td><td>");
				categoriesOut.append(cat.getLevel());
				categoriesOut.append("</td></tr>");
			}
		}
		return categoriesOut.append("</table>").toString();
	}

	
	private boolean validateList(DeplabsLoggerForm form) {
		form.setError("");
		return true;
	}
	
	private boolean validateChange(String logger, String level,DeplabsLoggerForm form) {
		if(logger==null ||logger.trim().equals("")){
			form.setError("You must pass parameter class");
			return false;
		}
		if(level==null ||level.trim().equals("")){
			form.setError("You must pass parameter level");
			return false;
		}
		
		if(!(logger.startsWith("com.deplabs")||logger.startsWith("com.marketlive"))){
			form.setError("Your package or class needs to start with com.deplabs or com.marketlive");
			return false;
		}
		if(StringUtil.split(logger, '.').length < 3){
			form.setError("You must provide at least 3 packages");
			return false;
		}
		if(logger.endsWith(".")){
			form.setError("Your package/class can not ends with a DOT");
			return false;
		}
		
		form.setError("");
		return true;
	}
	
	private boolean validateRemove(String logger, DeplabsLoggerForm form) {
		if(logger==null ||logger.trim().equals("")){
			form.setError("You must pass parameter class");
			return false;
		}
		if(!(logger.startsWith("com.deplabs")||logger.startsWith("com.marketlive"))){
			form.setError("Your package or class needs to start with com.deplabs or com.marketlive");
			return false;
		}
		if(StringUtil.split(logger, '.').length < 3){
			form.setError("You must provide at least 3 packages");
			return false;
		}
		if(logger.endsWith(".")){
			form.setError("Your package/class can not ends with a DOT");
			return false;
		}
		
		form.setError("");
		return true;

	}
	
	
	
	private void printSamples(){
		log.trace("LOG TRACE");
		log.debug("LOG DEBUG");
		log.info("LOG INFO");
		log.warn("LOG WARN");
		log.error("LOG ERROR");
		log.fatal("LOG FATAL");
	}
    
	@Override
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("form.list", LIST_FORM);
        map.put("form.update", UPDATE_FORM);
        map.put("form.remove", REMOVE_FORM);
        return map;
    }
}
