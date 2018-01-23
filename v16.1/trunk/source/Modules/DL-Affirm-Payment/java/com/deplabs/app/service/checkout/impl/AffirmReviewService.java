package com.deplabs.app.service.checkout.impl;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import com.deplabs.affirm.app.b2c.AffirmForwardResolver;
import com.deplabs.app.service.checkout.IAffirmPaymentService;
import com.deplabs.entity.cart.order.OrderPaymentAffirm;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.service.checkout.IReviewServiceContext;
import com.marketlive.app.service.checkout.IReviewServiceResponse;
import com.marketlive.app.service.checkout.impl.ReviewService;
import com.marketlive.app.service.checkout.impl.ReviewServiceResponse;
import com.marketlive.system.annotation.ApplicationService;

@ApplicationService
@Primary
public class AffirmReviewService extends ReviewService {

	/**
     * Logger for this class.
     */
    private static Logger log = LoggerFactory.getLogger(AffirmReviewService.class);
    
    @Autowired
    protected AffirmForwardResolver affirmForwardResolver;
    
	/**
    * Called to initialize Review page.
    *
    * @param request
    * @param response
    * @param checkoutForm
    * @return
    * @throws Exception
    */
   
    @Override
    public IReviewServiceResponse processView(HttpServletRequest request, HttpServletResponse response, IReviewServiceContext reviewServiceContext){

        log.debug("processView");

        IReviewServiceResponse reviewServiceResponse = new ReviewServiceResponse();
        reviewServiceResponse = super.processView(request, response, reviewServiceContext);
        // If Affirm payment method is selected and acepted, SKIP review order step
        if(IAffirmPaymentService.AFFIRM_PAYMENT_METHOD.equals(reviewServiceContext.getPaypalCheckoutSelected()) &&
        	StringUtils.isNotBlank((String)WebUtil.getCommerceSession(request).getAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN))){
     	   return this.processSubmit(request, response, reviewServiceContext);
       }
        return reviewServiceResponse;
	}
    
	@Override
	public IReviewServiceResponse processSubmit(HttpServletRequest request, HttpServletResponse response, IReviewServiceContext reviewServiceContext) {
		IReviewServiceResponse reviewServiceResponse = new ReviewServiceResponse();
		
		// check if we need to redirect to affirm process
		String checkoutToken = (String)WebUtil.getCommerceSession(request).getAttribute(IAffirmPaymentService.AFFIRM_CHECKOUT_TOKEN);
		// checkoutToken != null means it was processed & accepted by affirm
		if (StringUtils.isBlank(checkoutToken) && isAffirmPayment(reviewServiceContext)) {
			reviewServiceContext.setPaymentConfirmed(false);
            WebUtil.getCommerceSession(request).setAttribute(IAffirmPaymentService.AFFIRM_FORWARD, IAffirmPaymentService.AFFIRM_CHEKOUT_INFO);
            reviewServiceResponse.setResponseCode(IAffirmPaymentService.AFFIRM_CHEKOUT_INFO);
            return reviewServiceResponse;
            
		}

		return super.processSubmit(request, response, reviewServiceContext);
	}
	
	@SuppressWarnings("rawtypes")
	private boolean isAffirmPayment(IReviewServiceContext reviewServiceContext) {
		if (CollectionUtils.isNotEmpty(reviewServiceContext.getPayments())) {
			for (Iterator it = reviewServiceContext.getPayments().iterator(); it.hasNext();) {
				IOrderPayment orderPayment = (IOrderPayment) it.next();
				if (orderPayment instanceof OrderPaymentAffirm) {
					return true;
				}
			}
		}
		return false;
	}
    
    
}
