package com.deplabs.app.service.trigger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import org.hibernate.Query;
import org.marketlive.entity.site.ISite;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.marketlive.admin.AdminComponentMgr;
import com.marketlive.admin.order.AdminOrder;
import com.marketlive.struts.MMLLookupDispatchAction;
import com.marketlive.system.locale.LocaleManager;
import com.mmlive.db.MMLConnection;

import freemarker.template.utility.StringUtil;

public class DeplabsTriggerAction extends MMLLookupDispatchAction {
	
	private static Log log = LogFactory.getLog(DeplabsTriggerAction.class);
	
	public static final String LIST_FORM = "listAll";
	public static final String RUN_FORM = "runTrigger";
	
	
	public ActionForward listAll(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {	
		DeplabsTriggerForm triggerForm = (DeplabsTriggerForm)form;
		validateList(triggerForm);
		triggerForm.setResult("");
		return (new ActionForward(mapping.getInput()));
    } 
	
	
	public ActionForward runTrigger(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		DeplabsTriggerForm triggerForm = (DeplabsTriggerForm)form;
		String jobName=request.getParameter("job");  
		String delay = request.getParameter("delay");
		
		if(validateJob(jobName,delay,triggerForm)){
			
			MMLConnection connection = null;
			Statement statement = null;
			ResultSet rs = null;
			
			StringBuffer querySB = new StringBuffer();
			querySB.append("UPDATE QRTZ_TRIGGERS SET next_fire_time=(ml_date_to_ms(sysdate)) + ");
			if(delay!=null && !delay.trim().equals("")){
				querySB.append( delay+"000 WHERE job_name='");
			}else{
				querySB.append("10000 WHERE job_name='");	
			}
			querySB.append(jobName+"'");

			
			try {
				connection = com.mmlive.db.DataAccess.getDataAccess().borrowConnSuper();
				statement = connection.createStatement();
				rs = statement.executeQuery(querySB.toString());
				connection.commit();
			} catch (Exception e1) {
				triggerForm.setResult("Execution Failed: "+e1.getCause()+" /n Message"+e1.getMessage());
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (statement != null)
						statement.close();
					if (connection != null)
						com.mmlive.db.DataAccess.getDataAccess().returnConnSuper(connection);
				} catch (Exception e2) {
					triggerForm.setResult("Execution Failed: "+e2.getCause()+" /n Message"+e2.getMessage());
				}
			}				
		}
		triggerForm.setResult("Execution Finished Successfully");
		return (new ActionForward(mapping.getInput()));
    }
	

	private boolean validateList(DeplabsTriggerForm form) {
		form.setError("");
		return true;
	}
	
	private boolean validateJob(String jobName, String delay, DeplabsTriggerForm form) {
		if(jobName==null ||jobName.trim().equals("")){
			form.setError("You must pass parameter jobName");
			return false;
		}
		if(!(delay==null ||delay.trim().equals(""))){
			try{
			    Integer delayInt= new Integer(delay);
			    if(delayInt >86400){
			    	form.setError("Delay can not be higher than 1 day");
					return false;
			    }
			}catch (Exception e) {
			    form.setError("Delay must be an Integer Number");
				return false;
			}
		}
		
		form.setError("");
		return true;
	}
	
    
	@Override
    protected Map getKeyMethodMap() {
        Map map = new HashMap();
        map.put("form.list", LIST_FORM);
        map.put("form.submit", RUN_FORM);
        return map;
    }
}
