package com.deplabs.app.b2c.thirdparty.affirm.payments;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.deplabs.affirm.app.b2c.AffirmForwardResolver;
import com.deplabs.affirm.app.b2c.checkout.IAffirmCheckoutModel;
import com.deplabs.app.service.checkout.IAffirmCheckoutService;
import com.deplabs.app.service.checkout.IAffirmPaymentService;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.b2c.checkout.CheckoutForm;
import com.marketlive.app.b2c.checkout.accordion.ReviewController;
import com.marketlive.app.b2c.common.constants.ActionForwards;
import com.marketlive.app.b2c.common.constants.RequestParams;
import com.marketlive.app.b2c.reporting.ReportingConstants;
import com.marketlive.app.service.checkout.ICheckoutService;
import com.marketlive.app.service.checkout.IReviewService;
import com.marketlive.app.service.checkout.IReviewServiceContext;
import com.marketlive.app.service.checkout.IReviewServiceResponse;
import com.marketlive.system.annotation.ApplicationController;

@ApplicationController
@Primary
public class AffirmReviewController extends ReviewController {
	
    private static Logger log = LoggerFactory.getLogger(AffirmReviewController.class);
    
    @Autowired
    IReviewService reviewService;
    
    /**
     * Configurable parameter exposed for action definition to set the forward to be used
     * when going to personalization or kit config from the review table.  Personalization and kit configuration
     * pages currently take the struts forward as a param to know where to return to.  This will allow for
     * multiple return forwards to be used depending on checkout path through configuration.
     */
    @Value("INVOICE")
    String invoiceReturnForward;
    
    @Autowired
	protected AffirmForwardResolver affirmForwardResolver;
    
    @Autowired
    protected IAffirmCheckoutService affirmCheckoutService;
    
	/**
     * Called to initialize Review page.
     *
     * @param request
     * @param response
     * @param checkoutForm
     * @return
     * @throws Exception
     */
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, params = {"method=view"})
    public String view(HttpServletRequest request, HttpServletResponse response,
                       @ModelAttribute(RequestParams.CHECKOUT_FORM) CheckoutForm checkoutForm)
            throws Exception {
        log.debug("ReviewController.view");
        // reporting
        request.setAttribute(ReportingConstants.ACTIVE_STEP_NAME, ReportingConstants.ORDER_REVIEW_STEP);

        IReviewServiceContext reviewContext = createServiceContext(request, checkoutForm);
        IReviewServiceResponse reviewResponse = reviewService.processView(request, response, reviewContext);
        applyServiceContext(checkoutForm, reviewContext);
        String responseCode = reviewResponse.getResponseCode();

        // forward to the Empty Basket if Back button was clicked on the Thank You page.
        if (responseCode.equals(ActionForwards.EMPTY_BASKET_KEY)) {
            return forwardResolver.findForward(NAME, ActionForwards.EMPTY_BASKET_KEY);
        }

        //Setup the invoice return forward for use on the OrderItemTableRow tile
        checkoutForm.setCheckoutInvoiceKey(invoiceReturnForward);
        // If pay pal is selected then forward to the accordion view instead of writing to the response stream
        if (checkoutForm.isInitializeView()) {
            checkoutForm.setStepId(ICheckoutService.STEP_ORDER);
            return forwardResolver.findForward(NAME, "ACCORDION_VIEW");
        }

        if(StringUtils.isNotBlank(reviewResponse.getResponseCode()) && IReviewService.FINALIZE_ORDER.equals(reviewResponse.getResponseCode())){
        	return forwardResolver.findForward(NAME,IReviewService.FINALIZE_ORDER);
        }
        return forwardResolver.findForward(NAME, "VIEW_REVIEW");
    }

    @SuppressWarnings("unchecked")
	@RequestMapping(method = {RequestMethod.POST}, params = {"method=submitAffirm"})
    @ResponseBody
	public Map<String, Object> submitAffirm(HttpServletRequest request, HttpServletResponse response, CheckoutForm checkoutForm) throws Exception {
    	log.debug("ReviewController.submit");
        Map<String, Object> map = new HashMap<String, Object>();
        String forwardKey = ActionForwards.SUCCESS_KEY;

        IReviewServiceContext reviewContext = createServiceContext(request, checkoutForm);
        IReviewServiceResponse reviewResponse = reviewService.processSubmit(request, response, reviewContext);
        applyServiceContext(checkoutForm, reviewContext);

        String forward = null;
        forwardKey = reviewResponse.getResponseCode();
        String affirmForwardKey = (String) WebUtil.getCommerceSession(request).getAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
        if (StringUtils.isNotBlank(affirmForwardKey)){
        	forward = affirmForwardResolver.findAffirmForward(NAME, affirmForwardKey);
        	WebUtil.getCommerceSession(request).removeAttribute(IAffirmPaymentService.AFFIRM_FORWARD);
        	IReviewServiceContext reviewServiceContext = createServiceContext(request, checkoutForm);
            IAffirmCheckoutModel affirmCheckoutModel = affirmCheckoutService.getAffirmCheckoutModel(reviewServiceContext);
            return affirmCheckoutModel.getAttributeMap();
        } else {
        	forward = forwardResolver.findForward(NAME, forwardKey);
        }
        map.put("redirect", forward);

        return map;
	}
    
}
