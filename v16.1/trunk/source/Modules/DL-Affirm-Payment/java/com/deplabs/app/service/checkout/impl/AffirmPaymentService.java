package com.deplabs.app.service.checkout.impl;
 
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deplabs.entity.cart.order.IExtendedOrderPaymentType;
import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.marketlive.biz.account.ICreditCard;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.biz.session.tracker.ISessionTracker;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderPaymentType;
import org.marketlive.entity.currency.IAmount;

import com.deplabs.affirm.checkout.IAffirmPayment;
import com.deplabs.affirm.checkout.impl.AffirmPayment;
import com.deplabs.app.service.checkout.IAffirmPaymentService;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.service.checkout.ICheckoutServiceContext;
import com.marketlive.app.service.checkout.IPaymentServiceContext;
import com.marketlive.app.service.checkout.impl.PaymentService;
import com.marketlive.app.service.checkout.impl.PaymentServiceResponse;
import com.marketlive.entity.currency.Amount;
import com.marketlive.system.annotation.ApplicationService;

@ApplicationService
public class AffirmPaymentService extends PaymentService implements IAffirmPaymentService {

	/**
     * The LogFactory of the GiftOptionsModel class.
     */
    private static Log log = LogFactory.getLog(AffirmPaymentService.class);
    
	public PaymentServiceResponse processSubmit(HttpServletRequest request, HttpServletResponse response, IPaymentServiceContext paymentServiceContext) throws Exception {
		log.debug("AffirmPaymentService.processValidate");
		PaymentServiceResponse paymentServiceResponse = super.processSubmit(request, response, paymentServiceContext);
		try{
				if("OK".equalsIgnoreCase(paymentServiceResponse.getResponseCode()) && IAffirmPaymentService.AFFIRM_PAYMENT_METHOD.equalsIgnoreCase(paymentServiceContext.getPaypalCheckoutSelected())){
					// ------- Affirm check -------------->
		            // if payment type = affirm, do affirm payment processing
		                String checkoutToken = (String)WebUtil.getCommerceSession(request).getAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
		                if (StringUtils.isNotBlank(checkoutToken)) {
		                    addAffirmPayment(paymentServiceContext);  // payment submit clears payments
		                } else if (paymentServiceContext.isPaymentConfirmed()) {
		                    if (processAffirmPayment(paymentServiceContext)) { //if Affirm payment is successfully added to the payments, we need to forward to review step so then it redirects to affirm
		                        return paymentServiceResponse;
		                    	//paymentServiceContext.setPaymentConfirmed(false);
		                        //WebUtil.getCommerceSession(request).setAttribute(IAffirmPaymentService.AFFIRM_FORWARD, IAffirmPaymentService.AFFIRM_CHEKOUT_INFO);
		                        //paymentServiceResponse.setResponseCode(IAffirmPaymentService.AFFIRM_CHEKOUT_INFO);
		                    } else {
		                        paymentServiceContext.setPaymentConfirmed(false);
		                        paymentServiceResponse.setResponseCode(IAffirmPaymentService.STEP_ERROR);
		                        request.setAttribute(IAffirmPaymentService.AFFIRM_FORWARD , paymentServiceResponse.getResponseCode());
		                    }
		                }
		            } else {
		                WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
		                WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
				}
			
		} catch (Exception e) {
			 WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
             WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
		}
		return paymentServiceResponse;
	}
	
	@Override
    public PaymentServiceResponse processValidate(HttpServletRequest request, IPaymentServiceContext paymentServiceContext) throws Exception {
    	
		log.debug("AffirmPaymentService.processValidate");
    	
    	PaymentServiceResponse paymentResponse = super.processValidate(request, paymentServiceContext);
		try{
			if(!paymentServiceContext.isAmazonOrder() && !paymentServiceContext.isPaypalPaymentSelected() && !paymentServiceContext.isVisaCheckoutEdit() && !paymentServiceContext.isVisaCheckoutOrder()){
				
			}
			
		} catch (Exception e) {
			
		}
		return paymentResponse;
	}

	public PaymentServiceResponse processConfirm(HttpServletRequest request, HttpServletResponse response, IPaymentServiceContext paymentServiceContext) throws Exception {
		PaymentServiceResponse paymentResponse = new PaymentServiceResponse();
		String checkoutToken = request.getParameter(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
		
		ICommerceSession commerceSession = WebUtil.getCommerceSession(request) ;
		
		if(StringUtils.isNotBlank(checkoutToken)){
			
			commerceSession.setAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN, checkoutToken);
	    
			if (!paymentServiceContext.isShippingConfirmed()) {
	        	paymentServiceContext.setPaymentConfirmed(false);
	        	paymentServiceContext.setShippingConfirmed(false);
	        	paymentServiceContext.setBillingConfirmed(false);
	        } else {
	        	paymentServiceContext.setPaymentConfirmed(true);
	        	paymentServiceContext.setShippingConfirmed(true);
	        	paymentServiceContext.setBillingConfirmed(true);
	        }

	        paymentServiceContext.setReviewConfirmed(false);
	        paymentServiceContext.setFinalized(false);
	        paymentServiceContext.setThankyouViewed(false);

	        //Log Affirm payment method used
	        ISessionTracker sessionTracker = commerceSession.getSessionTracker();
	        if (sessionTracker != null) {
	            sessionTracker.logCheckoutPaymentMethod(commerceSession, "affirm");
	        }
	        
			paymentResponse.setResponseCode(IAffirmPaymentService.AFFIRM_CHECKOUT_SUCCESS);
			
		} else {
			if(commerceSession.getAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN) != null){
				commerceSession.removeAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
			}
			paymentServiceContext.setPaymentConfirmed(false);
			paymentResponse.setResponseCode(IAffirmPaymentService.AFFIRM_CHECKOUT_FAIL);
		}
		
		return paymentResponse;
    }
    
    public PaymentServiceResponse processCancel(HttpServletRequest request, HttpServletResponse response, IPaymentServiceContext paymentServiceContext) throws Exception {
    	PaymentServiceResponse paymentResponse = new PaymentServiceResponse();

    	ICommerceSession commerceSession = WebUtil.getCommerceSession(request) ;
    	
    	paymentServiceContext.setPaymentConfirmed(false);
    	paymentServiceContext.setReviewConfirmed(false);
	    paymentServiceContext.setFinalized(false);
	    paymentServiceContext.setThankyouViewed(false);
	        
    	if(commerceSession.getAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN) != null){
			commerceSession.removeAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
			WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
		}
    	
    	paymentResponse.setResponseCode(IAffirmPaymentService.AFFIRM_CHECKOUT_CANCEL);
    	return paymentResponse;
    }
    
	
	 /**
     * Processes Affirm Payment
     *
     * @param paymentServiceContext on which submit information is maintained
     * @returns if a successful PayPalPayment object is added to checkoutForm.payments() or not
     *
     * @throws Exception thrown by accountManager or checkoutForm
     */
    public boolean processAffirmPayment(ICheckoutServiceContext paymentServiceContext) throws Exception {

        removeExistingAffirmPayments(paymentServiceContext);

        // create affirm payment if there is outstanding payment amount left after applying Gift Certificates
        if (getPaymentAmountToCover(paymentServiceContext).compareTo(Amount.ZERO) > 0) {
            paymentServiceContext.getPayments().add(
                    createPayment(new AffirmPayment(), checkoutManager.findOrderPaymentTypeByName(IAffirmPaymentService.AFFIRM_PAYMENT_TYPE),paymentServiceContext)
              );
            return true;
        }
        return false;
    }

    /**
     * Adds Affirm Payment
     *
     * @param paymentServiceContext form on which submit information is maintained
     *
     * @throws Exception thrown by accountManager or paymentServiceRequest
     */
    public void addAffirmPayment(IPaymentServiceContext paymentServiceContext) throws Exception {

        paymentServiceContext.getPayments().add(createPayment(new AffirmPayment(), checkoutManager.findOrderPaymentTypeByName("AFFIRM"),paymentServiceContext));
    }

    /**
     *  removes any existing Affirm payments
     */
    protected void removeExistingAffirmPayments(ICheckoutServiceContext paymentServiceContext) {
        List paymentList = paymentServiceContext.getPayments();
        for(int u = 0; paymentList != null && u < paymentList.size(); u++) {
            if(paymentList.get(u) instanceof IAffirmPayment) {
                paymentList.remove(u);
            }
        }
    }
    
    /**
     * Creates a payment of the specified payment type.
     *
     * @param tender payment object
     * @param paymentType of payment ojbect
     * @param paymentServiceContext <code>PaymentServiceRequest</code> object needed to calculate payment
     * @return <code>IOrderPayment</code> if created, null otherwise
     * @throws Exception thrown by checkoutMananger
     */
    protected IOrderPayment createPayment(Object tender, IOrderPaymentType paymentType,  ICheckoutServiceContext paymentServiceContext) throws Exception {
    	IOrderPayment payment = null;
    	
    	try{
    		payment = super.createPayment(tender, paymentType, paymentServiceContext);
    	} catch (NullPointerException e) {
    		if(log.isDebugEnabled()){
    			log.debug("");
    		}
			if (IExtendedOrderPaymentType.TYPE_AFFIRM.equals(paymentType.getName())) {
	            payment = createAffirmPayment((IAffirmPayment) tender);
	        }

			IAmount paymentAmount = calculatePaymentAmount(tender, paymentServiceContext);
	        payment.setAmount(paymentAmount);
	        payment.setDescription(paymentType.getName());
	        payment.setPaymentType(paymentType);
		}
        return payment;
      }
    /**
     * Creates an instance of  PayPal Payment .
     *
     * @param paypal to collect information from
     * @return IOrderPayment created
     * @throws Exception thrown by checkoutManager
     */
    protected IOrderPayment createAffirmPayment(IAffirmPayment affirmPayment) throws Exception {
    	IOrderPaymentAffirm payment = (IOrderPaymentAffirm) checkoutManager.getOrderPaymentInstance(IOrderPaymentAffirm.class, null);
        payment.setDescription(IExtendedOrderPaymentType.TYPE_AFFIRM);

        return payment;
    }
    

    public void processCreditCard(ICheckoutServiceContext paymentServiceContext, boolean isReview) throws Exception {
        // If paypal selected, use separate logic for determining if should add new credit card
        // else
        // create credit card payment if there is outstanding payment amount left
        // after applying Gift Certificates
        // Create a payment associates with the credit card if
        // 1. payment amount to cover greater than ZERO
        // OR
        // 2. payment amount is ZERO and no payment has been created for this order (payment list is empty)

        if (IAffirmPaymentService.AFFIRM_PAYMENT_METHOD.equals(paymentServiceContext.getPaypalCheckoutSelected())){
            if ((getPaymentAmountToCover(paymentServiceContext).compareTo(Amount.ZERO) > 0) && (paymentServiceContext.getNewCreditCard() != null)) {
                ICreditCard creditCard = getCreditCard(paymentServiceContext);
                if ((creditCard != null) && (creditCardInfoEntered(creditCard))) {
                    if (!isReview) { // new credit card was entered
                        addCreditCard(paymentServiceContext, creditCard, isReview, true);
                    }
                }
            }
        } else {
        	super.processCreditCard(paymentServiceContext, isReview);
        }
    }
    
    
   
}
