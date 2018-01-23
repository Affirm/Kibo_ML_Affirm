package com.deplabs.app.b2c.thirdparty.affirm.payments;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.marketlive.biz.session.context.ICommerceSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.deplabs.affirm.app.b2c.AffirmForwardResolver;
import com.deplabs.app.service.checkout.IAffirmCheckoutService;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.b2c.checkout.CheckoutForm;
import com.marketlive.app.b2c.checkout.ICheckoutModel;
import com.marketlive.app.b2c.checkout.PaymentForm;
import com.marketlive.app.b2c.checkout.accordion.AccordionAddressBookController;
import com.marketlive.app.b2c.checkout.accordion.AccordionController;
import com.marketlive.app.b2c.checkout.accordion.BillingController;
import com.marketlive.app.b2c.checkout.accordion.GiftOptionsController;
import com.marketlive.app.b2c.checkout.accordion.PaymentController;
import com.marketlive.app.b2c.common.constants.RequestParams;
import com.marketlive.app.service.checkout.IBillingService;
import com.marketlive.app.service.checkout.ICheckoutService;
import com.marketlive.app.service.checkout.ICheckoutServiceContext;
import com.marketlive.biz.borderfree.IBorderFreeManager;
import com.marketlive.system.annotation.ApplicationController;

@ApplicationController
@RequestMapping(value = { "/accordioncheckout*", "/checkout/accordion*" })
@SessionAttributes({ RequestParams.CHECKOUT_FORM, BillingController.BILLING_ADDRESS_FORM, AccordionAddressBookController.ADD_ADDRESS_FORM, GiftOptionsController.GIFT_OPTION_FORM })
public class AffirmAccordionController extends AccordionController {
	
	private static Logger log = LoggerFactory.getLogger(AffirmAccordionController.class);

	@Autowired
	protected AffirmForwardResolver affirmForwardResolver;

	@Autowired
	protected IAffirmCheckoutService affirmCheckoutService;
	
	@Autowired
    protected IBorderFreeManager borderFreeManager;

	@RequestMapping(method = { RequestMethod.POST }, params = { "method=affirmCheckoutInfo" })
    public String affirmCheckoutInfo(HttpServletRequest request, HttpServletResponse response, @ModelAttribute(PaymentController.PAYMENT_FORM) PaymentForm paymentForm,
            @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {
        return affirmForwardResolver.findAffirmForward(NAME, "affirmCheckoutInfo");
    }
	
	/**
     * Initializes Accordion page. Calls view methods of billingAction,
     * shipToOtherAction, shippingMethodAction, paymentAction and recalculates
     * GiftCertificates if there are any in sesssion.
     *
     * @param request
     * @param response
     * @return String
     * @throws Exception
     */
	@Override
    @RequestMapping(method = { RequestMethod.POST }, params = { "method=submitStep" })
    public String submitStep(HttpServletRequest request, HttpServletResponse response, @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm, Map<String, Object> model)
            throws Exception {

        ICommerceSession commerceSession = WebUtil.getCommerceSession(request);
        if (commerceSession == null || (commerceSession != null && !commerceSession.isLoggedIn())) {
            // Forward to basket if not logged in
            return findForward(request, NAME, "BASKET");
        }

        // forward to the Empty Basket if the basket is empty.
        if (WebUtil.isEmptyBasket(checkoutForm.getBasket())) {
            return forwardResolver.findForward(NAME, "EMPTY_BASKET");
        }

        checkoutForm.setStepId(request.getParameter("stepId"));
        try {
            if (ICheckoutService.STEP_BILL.equals(checkoutForm.getStepId())) {
                return forwardResolver.findForward(NAME, "billingSubmit");
            } else if (ICheckoutService.STEP_SHIP.equals(checkoutForm.getStepId())) {
                Object shippingStyle = checkoutForm.getAttribute(ICheckoutServiceContext.SHIPPING_STYLE);
                if (IBillingService.OPTION_SHIPTO_MULTIPLE_ADDRESSES.equals(shippingStyle)) {
                    return forwardResolver.findForward(NAME, "multipleshiptoSubmit");
                } else {
                    //If borderFree shipping form submitting then forward to shipmethodSubmitBorderFree
                    if (borderFreeManager.isBorderFreeBasedOnBillShip(checkoutForm.getBillContact(), commerceSession)) {
                        return forwardResolver.findForward(NAME, "shipmethodSubmitBorderFree");
                    }
                    return forwardResolver.findForward(NAME, "shipmethodSubmit");
                }
            } else if (ICheckoutService.STEP_DELIVERY.equals(checkoutForm.getStepId())) {
                return forwardResolver.findForward(NAME, "shipmethodSubmit");
            } else if (ICheckoutService.STEP_PAY.equals(checkoutForm.getStepId())) {
                return forwardResolver.findForward(NAME, "paymentSubmit");
            } else if (ICheckoutService.STEP_ORDER.equals(checkoutForm.getStepId())) {
                return affirmForwardResolver.findAffirmForward(NAME, "reviewSubmit"); // AFFIRM forward customization
            }
        } catch (Exception e) {
            log.error("Error submitting accordion step: ", e);
        }

        return null;
    }
	
	 protected boolean isPayPalURL(HttpServletRequest request) {
		    try{
		    	return super.isPayPalURL(request);
		    } catch (NullPointerException e) {
		    	return false;
			}
    }
	 
	 /**
	     * Show error for the current step
	     *
	     * @param request
	     * @param response
	     * @param checkoutForm
	     * @return
	     * @throws Exception
	     */
	    @RequestMapping(method = {RequestMethod.POST}, params = {"method=viewAffirmError"})
	    @ResponseBody
	    public ICheckoutModel viewAffirmError(HttpServletRequest request, HttpServletResponse response, @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {
	        log.debug("in viewError");
	        ICheckoutServiceContext serviceContext = createServiceContext(request, checkoutForm);
	        ICheckoutModel model = affirmCheckoutService.processViewAffirmError(request, response, serviceContext);
	        applyServiceContext(checkoutForm, serviceContext);
	        return model;
	    }
	 
	 
	
}
