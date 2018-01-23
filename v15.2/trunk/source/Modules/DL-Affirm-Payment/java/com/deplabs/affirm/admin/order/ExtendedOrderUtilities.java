package com.deplabs.affirm.admin.order;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderPayment;

import com.marketlive.admin.AdminComponentMgr;
import com.marketlive.admin.order.OrderUtilities;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.system.text.AmountFormat;

public class ExtendedOrderUtilities extends OrderUtilities {
	
	
	public static String getPaymentMethodText(IOrder order) {
			StringBuffer paymentItemDisplay = new StringBuffer("");
		try {
            AdminComponentMgr componentMgr = AdminComponentMgr.getInstance();
            IOrderManager orderManager = componentMgr.getOrderManager();
            paymentItemDisplay = new StringBuffer(OrderUtilities.getPaymentMethodText(order));
			
			for (Iterator i = order.getPayments().iterator(); i.hasNext();) {
				IOrderPayment payment = (IOrderPayment) i.next();
				if (payment.getDescription().equals("AFFIRM")) {
					paymentItemDisplay
							.append(" Affirm for "
									+ AmountFormat.format(payment.getAmount(),
											order.getLocale(), order.getSite()
													.getCurrency()) + "<br>");
				}
			}
		} catch (Exception e) {
			Log log = LogFactory.getLog(ExtendedOrderUtilities.class);
			log.error("ExtendedOrderUtilities.java:getPaymentMethodText - Could not get payment info "
					+ e);
		}
		return paymentItemDisplay.toString();
	}
	
	public static String getPaymentAuthorizationRequestId(IOrder order){
		try {
		 AdminComponentMgr componentMgr = AdminComponentMgr.getInstance();
         IAuthorizationDAO authorizationDAO = componentMgr.getBean(IAuthorizationDAO.class);
         IOrderManager orderManager = componentMgr.getOrderManager();
			
			for (Iterator i = order.getPayments().iterator(); i.hasNext();) {
				IOrderPayment payment = (IOrderPayment) i.next();
				if (payment.getDescription().equals("AFFIRM")) {
					List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));
					if(authorizations != null && !authorizations.isEmpty()){
						return authorizations.get(0).getRequestID();
					}
					else return "";
				}
				else return "";
			}
		} catch (Exception e) {
			Log log = LogFactory.getLog(ExtendedOrderUtilities.class);
			log.error("ExtendedOrderUtilities.java:getPaymentAuthorizationRequestId"
					+ e);
		}
			return "";         
	}

}
