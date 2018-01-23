package com.deplabs.affirm.app.b2c;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.marketlive.app.b2c.WebUtil;
import com.marketlive.biz.borderfree.IBorderFreeManager;

public class AffirmWebUtil {
	
	/**
	 * Logger.
	 */
	private static Logger log = LoggerFactory.getLogger(AffirmWebUtil.class);
	
	public static boolean isBorderFreeEnabledAndNotUSSelected(HttpServletRequest request) {
		boolean isBorderFreeEnabledAndNotUSSelected = false;
		try {
			IBorderFreeManager borderFreeManager = getBorderFreeManager(request);
			isBorderFreeEnabledAndNotUSSelected = borderFreeManager.isBorderFreeEnabledAndNotUSSelected(WebUtil.getCommerceSession(request));
		} catch (Exception e) {
			log.debug("exception occured while determining that borderFreeEnabled and not us selected " + e.getMessage());
		}
		return isBorderFreeEnabledAndNotUSSelected;
		
	}
	
	public static IBorderFreeManager getBorderFreeManager(HttpServletRequest request) {
		return getBean(IBorderFreeManager.class, request);
	}
	
	public static <T> T getBean(Class<T> requiredType, HttpServletRequest request) {
		return getWebApplicationContext(request).getBean(requiredType);
	}
	
	/**
	 * Get the {@link WebApplicationContext} for the given {@link HttpServletRequest}.
	 * 
	 * @param request
	 *            the {@link HttpServletRequest}
	 * @return the {@link WebApplicationContext}
	 */
	public static WebApplicationContext getWebApplicationContext(HttpServletRequest request) {
		return getRequiredWebApplicationContext(request.getSession().getServletContext());
	}
	
	/**
	 * Get the required {@link WebApplicationContext} for the given {@link ServletContext}.
	 * 
	 * @param servletContext
	 *            the {@link ServletContext}
	 * @return the required {@link WebApplicationContext}
	 */
	public static WebApplicationContext getRequiredWebApplicationContext(ServletContext servletContext) {
		final WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		if (wc == null) {
			throw new RuntimeException("WebApplicationContext not found");
		}
		return wc;
	}
	
}
