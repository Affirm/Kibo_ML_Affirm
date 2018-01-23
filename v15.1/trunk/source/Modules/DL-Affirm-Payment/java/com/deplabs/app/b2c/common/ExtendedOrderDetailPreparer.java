package com.deplabs.app.b2c.common;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.request.Request;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.marketlive.app.b2c.account.AccountRegisterForm;
import com.marketlive.app.b2c.common.OrderDetailModel;
import com.marketlive.app.b2c.common.constants.RequestParams;
import com.marketlive.app.service.account.IOrderDetailComponentService;
import com.marketlive.app.service.account.impl.OrderDetailComponentService;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.system.annotation.ApplicationController;
import com.marketlive.web.tiles.AbstractViewPreparer;

/**
 * Created with IntelliJ IDEA.
 * User: peters
 * Date: 1/14/14
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
@ApplicationController
public class ExtendedOrderDetailPreparer extends AbstractViewPreparer {

    /**
     * Logger for this class.
     */
    private static Logger logger = LoggerFactory.getLogger(ExtendedOrderDetailPreparer.class);

    /**
     * The name of this component.
     */
    public static final String NAME = "orderDetailPreparer";

	private static final String AFFIRM_REQUEST_ID = "affirmRequestId";

	private static final String AFFIRM_PAYMENT_PUBLIC_KEY = "affirmPaymentPublicKey";

	private static final String AFFIRM_PAYMENT_CUSTOMER_DETAIL = "affirmPaymentCustomerDetail";

    @Autowired
    private IOrderDetailComponentService orderDetailComponentService;
    
    @Autowired
    IAuthorizationDAO authorizationDAO;

    public void execute(Request viewRequest, AttributeContext attributeContext) {

        logger.debug("OrderDetailController.execute");
        HttpServletRequest request = getServletRequest(viewRequest);
        HttpServletResponse response = getServletResponse(viewRequest);

        try {
            // get model
            OrderDetailModel model = getModel(request);

            AccountRegisterForm form = (AccountRegisterForm) request.getSession().getAttribute(RequestParams.ACCOUNT_FORM);
            model.setAttribute(OrderDetailComponentService.ACCOUNT_REGISTER_FORM_MAP, form.getAttributeMap());

            orderDetailComponentService.processExecute(request, response, model);

            Map<String, Object> accountRegisterFormMap = (Map)model.getAttribute(OrderDetailComponentService.ACCOUNT_REGISTER_FORM_MAP);
            form.setAttributeMap(accountRegisterFormMap);
            String affirmPaymentCustomerDetail = configurationManager.getAsString("custom.affirmpayment_customer_detail_url");
            String affirmPaymentPublicKey = configurationManager.getAsString("custom.affirmpayment_public_api_key");
            String affirmRequestId = getAffirmRequestId(form, model);
            if(logger.isDebugEnabled()){
            	logger.debug("affirmRequestId: "+affirmRequestId);
            	logger.debug("affirmPaymentPublicKey: "+affirmPaymentPublicKey);
            	logger.debug("affirmPaymentCustomerDetail: "+affirmPaymentCustomerDetail);
            }
            model.setAttribute(AFFIRM_REQUEST_ID, affirmRequestId);
            model.setAttribute(AFFIRM_PAYMENT_PUBLIC_KEY, affirmPaymentPublicKey);
            model.setAttribute(AFFIRM_PAYMENT_CUSTOMER_DETAIL, affirmPaymentCustomerDetail);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception" + e);
        }
    }

    /**
     * Return the BasketTableModel, instantiating it if required.
     *
     * @param request HttpServletRequest.
     * @return model BasketTableModel.
     * @throws Exception Runtime exception.
     */
    protected OrderDetailModel getModel(HttpServletRequest request) throws Exception {
        OrderDetailModel model = (OrderDetailModel) request.getAttribute(OrderDetailModel.NAME);
        if (model == null) {
            model = new OrderDetailModel();
            request.setAttribute(OrderDetailModel.NAME, model);
        }
        return model;
    }
    
    public String getAffirmRequestId(AccountRegisterForm form, OrderDetailModel model){
    	IOrder order = form.getOrder();
    	for (Iterator iterator = order.getPayments().iterator(); iterator.hasNext();) {
			IOrderPayment payment = (IOrderPayment) iterator.next();
			if (payment.getDescription().equals("AFFIRM")) {
				List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));
				if(authorizations != null && !authorizations.isEmpty()){
					return authorizations.get(0).getRequestID();
				}
				else return "";
			}
			
		}
    	return "";
    }

}
