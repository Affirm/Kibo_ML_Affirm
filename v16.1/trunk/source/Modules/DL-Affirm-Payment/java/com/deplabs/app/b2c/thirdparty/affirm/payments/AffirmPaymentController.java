package com.deplabs.app.b2c.thirdparty.affirm.payments;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.deplabs.affirm.app.b2c.AffirmForwardResolver;
import com.deplabs.affirm.app.b2c.checkout.IAffirmCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.impl.AffirmCheckoutModel;
import com.deplabs.app.service.checkout.IAffirmCheckoutService;
import com.deplabs.app.service.checkout.IAffirmPaymentService;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.b2c.checkout.CheckoutForm;
import com.marketlive.app.b2c.checkout.PaymentForm;
import com.marketlive.app.b2c.checkout.accordion.PaymentController;
import com.marketlive.app.b2c.common.constants.RequestParams;
import com.marketlive.app.service.IServiceContext;
import com.marketlive.app.service.checkout.IPaymentServiceContext;
import com.marketlive.app.service.checkout.impl.PaymentServiceContext;
import com.marketlive.app.service.checkout.impl.PaymentServiceResponse;
import com.marketlive.mod.accordioncheckout.app.b2c.AccordionConstants;
import com.marketlive.system.annotation.ApplicationController;


@ApplicationController
@RequestMapping(value = {"/checkout/affirm-payment/payment**","/accordion/payment*"})
@SessionAttributes({RequestParams.CHECKOUT_FORM, PaymentController.PAYMENT_FORM})
public class  AffirmPaymentController extends PaymentController {

	public static final String VIEW_AFFIRM = "VIEW_AFFIRM";
	
	@Autowired
	protected IAffirmPaymentService affirmPaymentService;
	    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AffirmPaymentController.class);

    @Autowired
    protected IAffirmCheckoutService affirmCheckoutService;
    
    @Autowired
    protected AffirmForwardResolver affirmForwardResolver;
    
    /**
     * This method will get called when user submits payment info form.
     * It processes user entered payment info.
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return Action or JSP page that the request should be forwarded to.
     * @throws Exception Runtime exception.
     */
    @RequestMapping(method = {RequestMethod.POST}, params = {"method=submit"})
    public String submit(HttpServletRequest request, HttpServletResponse response,
                         @Valid @ModelAttribute(PAYMENT_FORM) PaymentForm paymentForm, BindingResult result,
                         @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {
        log.debug("in payment submit method");
        String forwardUrl = super.submit(request, response, paymentForm, result,checkoutForm);
        String affirmFowardKey = (String) WebUtil.getCommerceSession(request).getAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
        if (StringUtils.isNotBlank(affirmFowardKey)){
        	forwardUrl = affirmForwardResolver.findAffirmForward(PaymentController.NAME, affirmFowardKey);
        	WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
        }
        return forwardUrl;
        
    }
	/**
     * Executes view method to set up the view.
     *
     * @param request
     * @param response
     * @param checkoutForm
     * @return
     * @throws Exception
     */
    @RequestMapping(method = { RequestMethod.POST }, params = { "method=confirm" })
    public String confirm(HttpServletRequest request, HttpServletResponse response,@ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {
    	
    	 PaymentServiceResponse paymentResponse = new PaymentServiceResponse();
    	 paymentResponse.setResponseCode("ERROR");
    	 try{
    		 IPaymentServiceContext paymentServiceContext = createServiceContext(request,checkoutForm);
    		 paymentResponse = affirmPaymentService.processConfirm(request, response, paymentServiceContext);
    		 /*if(IAffirmPaymentService.AFFIRM_CHECKOUT_SUCCESS.equalsIgnoreCase(paymentResponse.getResponseCode())){
    			 checkoutForm.setInitializeView(true);
    		 }*/
    		 applyServiceContext(paymentServiceContext, checkoutForm);
    	 }
    	 catch (Exception e) {
    		 log.error(e.getMessage());
		}
    	
    	return affirmForwardResolver.findAffirmForward(PaymentController.NAME, paymentResponse.getResponseCode());
    }
    
    @RequestMapping(method = { RequestMethod.POST, RequestMethod.GET }, params = { "method=cancel" })
    public String cancel(HttpServletRequest request, HttpServletResponse response, @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {

    	PaymentServiceResponse paymentResponse = new PaymentServiceResponse();
    	 paymentResponse.setResponseCode("ERROR");
    	 try{
    		 IPaymentServiceContext paymentServiceContext = createServiceContext(request,checkoutForm);
    		 paymentResponse = affirmPaymentService.processCancel(request, response, paymentServiceContext);
    		 applyServiceContext(paymentServiceContext, checkoutForm);
    	 }
    	 catch (Exception e) {
    		 log.error(e.getMessage());
		}
    	return affirmForwardResolver.findAffirmForward(PaymentController.NAME, paymentResponse.getResponseCode());
    }
    
    
    public void applyServiceContext(IServiceContext serviceContext, CheckoutForm checkoutForm) {
        checkoutForm.setAttributeMap(serviceContext.getAttributeMap());
     }
    
    
    /* -------------------------------------------------------------------------------- */
    /*                         Apply AFFIRM PAYMENT                                     */
    /* -------------------------------------------------------------------------------- */
    @RequestMapping(method = {RequestMethod.POST}, params = {"method=affirmCheckoutInfo"})
    public String affirmCheckoutInfo(HttpServletRequest request, HttpServletResponse response,
                                       @ModelAttribute(PAYMENT_FORM) PaymentForm paymentForm,
                                       @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm) throws Exception {
        log.info("in affirmCheckoutInfo method");
        try {
            IPaymentServiceContext paymentServiceContext = createServiceContext(request, checkoutForm);
            
            // make sure basket not empty
            if (WebUtil.isEmptyBasket(paymentServiceContext.getBasket())) {
                log.info("Empty Basket");
                return forwardResolver.findForward(NAME, "BASKET");
            }

        } catch (Exception e) {
            log.error(AccordionConstants.EXCEPTION, e);
        }
        return affirmForwardResolver.findAffirmForward(NAME, VIEW_AFFIRM);
    }

    /* -------------------------------------------------------------------------------- */
    /*                         UPDATE GIFT CERTIFICATE JSON data                        */
    /* -------------------------------------------------------------------------------- */
    @RequestMapping(method = {RequestMethod.POST}, params = {"method=updateAffirmView"})
    @ResponseBody
    public IAffirmCheckoutModel updateAffirmView(HttpServletRequest request, HttpServletResponse response,
                                                               @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm)
            throws Exception {

    	log.info("in updateGiftCertificates method");
        try {
            IPaymentServiceContext paymentServiceContext = createServiceContext(request, checkoutForm);
            return affirmCheckoutService.getAffirmCheckoutModel(paymentServiceContext); 
        } catch (Exception e) {
            log.error("Error updating gift certificates json data.", e);
        }
        return new AffirmCheckoutModel();
    }

    public void applyServiceContext(CheckoutForm checkoutForm, IPaymentServiceContext paymentServiceContext) {
        checkoutForm.setAttributeMap(paymentServiceContext.getAttributeMap());
    }
    
}
