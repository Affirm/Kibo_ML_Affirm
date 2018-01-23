package com.deplabs.biz.checkout;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

import java.util.Iterator;

import org.marketlive.biz.checkout.IFinalizeOrderInfo;
import org.marketlive.biz.checkout.IPostFinalizeCheck;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.system.config.multisite.ISiteAwareConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.deplabs.biz.payment.IAffirmPaymentCaptureManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.marketlive.biz.checkout.FinalizeOrderInfo;
import com.marketlive.biz.payment.IPaymentCaptureManager;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * A post finalize check that links affirm Payment authorization and Order.
 *
 * @author horacioa
 */
@PlatformComponent
public class PostFinalizeAffirmAuthorizationCheck implements IPostFinalizeCheck {

    /**
     * Logger for this class.
     */
    private static Logger log = LoggerFactory.getLogger(PostFinalizeAffirmAuthorizationCheck.class);

    /**
     * Constant representing the name of the key whose value tells whether to capture affirm payment in real time or not.
     */
    private static final String REAL_TIME_AFFIRM_CAPTURE_CALL = "custom.affirmpayment_real_time_capture_call";

    /**
     * Constant for the request ID of an affirm transaction.
     */
    public static final String AFFIRM_TRANSACTION_REQUEST_ID = "AFFIRM_REQUEST_ID";

    /**
     * Constant that defines the success status for post-finalize credit card authorization check.
     */
    public static final String POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS = "POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS";

    /**
     * Constant that defines the failed status for post-finalize credit card authorization check.
     */
    public static final String POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_FAIL = "POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_FAIL";

    /**
     * Reference to the IConfigurationManager.
     */
    @Autowired
    private ISiteAwareConfigurationManager configurationManager;
    
    /** Reference to {@link IPaymentCaptureManager}. */
    @Autowired
	private IPaymentCaptureManager paymentCaptureManager;

    /**
     * Reference to the AuthorizationDAO.
     */
    @Autowired
    private IAuthorizationDAO authorizationDAO;

    /**
     * Set the Authorization Code and Order link.
     *
     * @param order           instance of <code>Order</code> that needs to be evaluated by the routine after
     *                        the order has been finalized.
     * @param commerceSession instance of <code>ICommerceSession</code>.
     * @param infoItem        instance of the <code>IFinalizeOrderInfo</code> returned by the previous post-finalize routine.
     *                        Can be used by this routine to alter it's processes.
     * @return instance of <code>IFinalizeOrderInfo</code> containing the results of the
     * execution of this check.
     * @throws Exception
     */
    @Override
    public IFinalizeOrderInfo evaluate(IOrder order, ICommerceSession commerceSession, IFinalizeOrderInfo infoItem) throws Exception {

        IFinalizeOrderInfo finalizeOrderInfo = new FinalizeOrderInfo();
        finalizeOrderInfo.setResult(POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS);

        if (commerceSession.getAttribute("RETURN_ORDER") != null) {
            return finalizeOrderInfo;
        }

        try {
            String requestId = (String) commerceSession.getAttribute(AFFIRM_TRANSACTION_REQUEST_ID);

            if (requestId != null) {
                // Check if the Affirm capture process will be real time OR not (by Job)
                // This action will update also the orderId on the authorization in order to avoid StaleExceptions
                if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments()) && configurationManager.getAsBoolean(REAL_TIME_AFFIRM_CAPTURE_CALL, false)) {
                	for (Iterator it = order.getShipments().iterator(); it.hasNext();) {
                		IOrderShipment orderShipment = (IOrderShipment) it.next();
                		((IAffirmPaymentCaptureManager)paymentCaptureManager).capturePayment(orderShipment, false, true, requestId);
                	}
                } else {
                	Long orderId = new Long(order.getPk().getAsString());
	                Authorization authorization = authorizationDAO.findCreditCardAuthorizationByRequestId(requestId);
	                authorization.setOrderId(orderId);
	                authorizationDAO.update(authorization);
                }

                commerceSession.removeAttribute(AFFIRM_TRANSACTION_REQUEST_ID);
                
                
            }
        } catch (Exception e) {
            finalizeOrderInfo.setResult(POST_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_FAIL);
            log.error("Post Finalize Credit Card Authorization Check failed for Order: " + order.getPk(), e);
        }

        return finalizeOrderInfo;
    }
}