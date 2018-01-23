package com.deplabs.biz.checkout;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.marketlive.biz.cart.basket.IManagedBasket;
import org.marketlive.biz.checkout.IFinalizeOrderInfo;
import org.marketlive.biz.checkout.IPreFinalizeCheck;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.entity.IPrimaryKey;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.system.config.multisite.ISiteAwareConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmApiAction;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.borderfree.IBorderFreeManager;
import com.marketlive.biz.checkout.FinalizeOrderInfo;
import com.marketlive.entity.currency.Amount;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * A pre finalize check that does authorization of the affirm payment
 *
 * @author horacioa
 */
@PlatformComponent
public class PreFinalizeAffirmAuthorizationCheck implements IPreFinalizeCheck {

    /**
     * Logger for this class.
     */
    private static Logger log = LoggerFactory.getLogger(PreFinalizeAffirmAuthorizationCheck.class);

    
    /**
     * Constant for the request ID of a affirm payment transaction.
     */
    public static final String AFFIRM_CHECKOUT_TOKEN = "checkout_token";
    
    /**
     * Constant for the request ID of a affirm payment transaction.
     */
    public static final String AFFIRM_TRANSACTION_REQUEST_ID = "AFFIRM_REQUEST_ID";

    /**
     * Constant that defines the success status for pre-finalize affirm payment authorization check.
     */
    public static final String PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS = "SUCCESS_AFFIRM_AUTH";

    /**
     * Constant that defines the failed status for pre-finalize affirm authorization check.
     */
    public static final String PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_FAIL = "FAILED_AFFIRM_AUTH";

    /**
     * Constant that defines the response for pre-finalize affirm authorization check.
     */
    public static final String PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_RESPONSE = "PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_RESPONSE";

    /**
     * Reference to the IConfigurationManager.
     */
    @Autowired
    private ISiteAwareConfigurationManager configurationManager;

    /**
     * Reference to the AffirmPaymentTransactionManager.
     */
    @Autowired
    private IAffirmPaymentTransactionManager affirmPaymentTransactionManager;

    /**
     * Reference to the IBorderManager.
     */
    @Autowired
    IBorderFreeManager borderFreeManager;

    /**
     * Gets affirm authorization before the Order is finalized.
     *
     * @param managedBasket    instance of Basket that needs to be evaluated/altered by the routine prior to the Order
     *                         finalization.
     * @param orderPaymentList list of payment entities that might need to be evaluated by the routine prior to the
     *                         Order finalization.
     * @param commerceSession  instance of <code>ICommerceSession</code>.
     * @param infoItem         instance of the IFinalizeOrderInfo returned by the previous pre-finalize routine. Can be used by
     *                         this routine to alter it's processes.
     * @return instance of <code>IFinalizeOrderInfo</code> containing the results of the execution of this check.
     * @throws Exception for unrecoverable errors.
     */
    @Override
    @SuppressWarnings({ "rawtypes" })
    public IFinalizeOrderInfo evaluate(IManagedBasket managedBasket, List orderPaymentList,
                                       ICommerceSession commerceSession, IFinalizeOrderInfo infoItem) throws Exception {

        IFinalizeOrderInfo finalizeOrderInfo = new FinalizeOrderInfo();
        finalizeOrderInfo.setResult(PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS);

        if (commerceSession.getAttribute("RETURN_ORDER") != null) {
            return finalizeOrderInfo;
        }

        try {
            boolean isBorderFreeOrder = borderFreeManager.isBorderFreeOrder(managedBasket);
            if (isBorderFreeOrder) {
                if (log.isDebugEnabled()) {
                    log.debug("Border free order so authorization not required. " + managedBasket.getCustomer().getPk() + " Basket: " + managedBasket.getPk());
                }
                return sucessValues(finalizeOrderInfo);
            } else {
                boolean affirmPayment = AffirmPaymentUtil.isPaymentThroughAffirm((orderPaymentList));
                if (affirmPayment) {
                	String checkoutToken = (String) commerceSession.getAttribute(AFFIRM_CHECKOUT_TOKEN);
                	
                	if (StringUtils.isNotEmpty(checkoutToken)) {
                		IOrderPaymentAffirm orderPayment = AffirmPaymentUtil.getAffirmPayment(orderPaymentList);

                		String formattedAmount = null;
                    	IAmount amount = new Amount();
                        amount = orderPayment.getAmount();
                        if (amount != null) {
                            NumberFormat format = NumberFormat.getInstance();
                            format.setMinimumFractionDigits(2);
                            format.setMaximumFractionDigits(2);
                            format.setGroupingUsed(false);
                            formattedAmount = format.format(amount.toBigDecimal());
                        }
                        amount = new Amount(formattedAmount);
                        // only process a affirm payment authorization transaction if the effective authorization amount is greater than zero
                        if (formattedAmount != null && amount != null && (new Amount().compareTo(amount) == -1)) {
                        	return processAffirmPaymentAuthorization(managedBasket, commerceSession, finalizeOrderInfo, checkoutToken, formattedAmount, orderPayment);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Not processsing a payment authorization transaction because"
                                        + " the auth amount is zero. Customer id: "
                                        + managedBasket.getCustomer().getPk().getAsString() + " Basket id: "
                                        + managedBasket.getPk().getAsString());
                                log.debug("A Merchandise Subtotal override may have been applied to this basket, bringing the affirm payment total to zero.");
                            }
                            return sucessValues(finalizeOrderInfo);
                        }
                	} else {
                         log.error("Not processsing the affirm authorization transaction because"
                                     + " the checkout token is null on the commercesession of customer id: "
                                     + managedBasket.getCustomer().getPk().getAsString() + " Basket id: " + managedBasket.getPk().getAsString());
                         
                         return failureValues(finalizeOrderInfo);
                	}
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Cannot authorize because affirm payment information is not available for: " + managedBasket.getCustomer().getPk() + " Basket: " + managedBasket.getPk());
                    }
                    return sucessValues(finalizeOrderInfo);
                }
            }
        } catch (Exception e) {
            log.error("Failed to authorize payment for Customer: " + managedBasket.getCustomer().getPk() + " Basket: " + managedBasket.getPk(), e);
            failureValues(finalizeOrderInfo);
        }

        return finalizeOrderInfo;
    }

	protected IFinalizeOrderInfo processAffirmPaymentAuthorization(IManagedBasket managedBasket, ICommerceSession commerceSession, IFinalizeOrderInfo finalizeOrderInfo, String checkoutToken, String amount, IOrderPaymentAffirm orderPayment) {
		AffirmRequest request = new AffirmRequest();
        IPrimaryKey customerPk = managedBasket.getCustomer().getPk();
        
        if (checkoutToken != null) {
            populateAffirmAuthRequest(request, managedBasket, checkoutToken, amount);
            int customerId = Integer.parseInt(customerPk.getAsString());
            AffirmResponse response = affirmPaymentTransactionManager.authorizePayment(request, customerId);
            if (response.isSuccess()) { // Successful affirm payment authorization.
                if (log.isDebugEnabled()) {
                    log.debug("Affirm payment authorization is successful for Customer: " + customerPk + " Basket: " + managedBasket.getPk());
                }
                String requestId = response.getChargeId();
                // Set authorization code in commerce session for later use by
                // PostFinalizeAffirmAuthorizationCheck.java
                commerceSession.setAttribute(AFFIRM_TRANSACTION_REQUEST_ID, requestId);
                
                // reset the AFFIRM_CHECKOUT_TOKEN token 
                commerceSession.removeAttribute(AFFIRM_CHECKOUT_TOKEN);
                
                return sucessValues(finalizeOrderInfo);
            } else { // Affirm payment authorization failed.
                log.error("Failed to authorize affirm payment for Customer: " + customerPk + " Basket: " + managedBasket.getPk() + " with failure message: " + response.getResponseMessage());
                commerceSession.setAttribute(PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_RESPONSE, response);
                return failureValues(finalizeOrderInfo);
            }
        } else {
            log.error("Failed to authorize affirm payment for Customer: " + customerPk + " Basket: " + managedBasket.getPk());
            return failureValues(finalizeOrderInfo);
        }
	}

    private void populateAffirmAuthRequest(AffirmRequest affirmRequest, IManagedBasket managedBasket, String checkoutToken, String amount) {
    	affirmRequest.setRequestType(AffirmApiAction.AUTHORIZE);
    	affirmRequest.setOrderId(managedBasket.getBasketEntity().getPk().getAsString());
    	affirmRequest.setCheckoutToken(checkoutToken);
    }

    private IFinalizeOrderInfo sucessValues(IFinalizeOrderInfo finalizeOrderInfo) {
        finalizeOrderInfo.setResult(PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_SUCCESS);
        finalizeOrderInfo.setCanContinue(true);
        finalizeOrderInfo.setFinalizeAllowed(true);
        return finalizeOrderInfo;
    }

    private IFinalizeOrderInfo failureValues(IFinalizeOrderInfo finalizeOrderInfo) {
        finalizeOrderInfo.setResult(PRE_FINALIZE_AFFIRM_AUTHORIZATION_CHECK_FAIL);
        finalizeOrderInfo.setCanContinue(false);
        finalizeOrderInfo.setFinalizeAllowed(false);
        return finalizeOrderInfo;
    }
}
