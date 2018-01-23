package com.deplabs.affirm.app.b2c;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.marketlive.app.b2c.ForwardResolver;
import com.marketlive.app.b2c.checkout.accordion.AccordionController;
import com.marketlive.app.b2c.checkout.accordion.PaymentController;
import com.marketlive.app.b2c.checkout.accordion.ReviewController;
import com.marketlive.system.annotation.ApplicationComponent;

@ApplicationComponent
public class AffirmForwardResolver extends ForwardResolver {
	
    private Map<String, Map<String, String>> affirmControllerPaths = new HashMap<String, Map<String, String>>();
    
	@PostConstruct
	public void init(){
		
		
		// ---- AffirmPaymentController
		Map<String, String> paths = new HashMap<String, String>(6);
		paths.put("BASKET", "redirect:/basket.do");
        paths.put("VIEW_AFFIRM", "forward:/accordion/payment.do?method=updateAffirmView");
        paths.put("CHECKOUT_SUCCESS", "redirect:/checkout/accordioncheckout.do?method=next");
        paths.put("CHECKOUT_FAIL", "redirect:/checkout/accordioncheckout.do?method=view");
        paths.put("CHECKOUT_CANCEL", "redirect:/checkout/accordioncheckout.do?method=view");
        paths.put("AFFIRM_CHEKOUT_INFO", "forward:/checkout/affirm-payment/payment.do?method=affirmCheckoutInfo");
        paths.put("AFFIRM_STEP_ERROR", "forward:/checkout/accordioncheckout.do?method=viewAffirmError");
        paths.put("ERROR", "redirect:/jump.do?itemType=ErrorPage");
        affirmControllerPaths.put(PaymentController.NAME, paths);
        
        //---- AffirmAccordionController
        paths = new HashMap<String, String>(1);
        paths.put("affirmCheckoutInfo", "forward:/checkout/affirm-payment/payment.do?method=affirmCheckoutInfo");
        paths.put("reviewSubmit", "forward:/accordion/review.do?method=submitAffirm");
        affirmControllerPaths.put(AccordionController.NAME, paths);
        
        //---- AffirmReviewController
        paths = new HashMap<String, String>(1);
        paths.put("AFFIRM_CHEKOUT_INFO", "/checkout/affirm-payment/payment.do?method=affirmCheckoutInfo");
        paths.put("VIEW_AFFIRM", "forward:/accordion/payment.do?method=updateAffirmView");
        paths.put("FINALIZE_ORDER", "redirect:/accordion/review.do?method=submit");
        affirmControllerPaths.put(ReviewController.NAME, paths);
        
        
	}
	
	
    public final String findAffirmForward(String controllerName, String forwardCode) {

        Map<String, String> paths = affirmControllerPaths.get(controllerName);
        
        if (paths != null) {
            String path = paths.get(forwardCode);
            if (path != null) {
                path = appendNavStateParameter(path);
                return path;
            }
        } else {
        	return super.findForward(controllerName, forwardCode);
        }
        return "/jump.do?itemType=ErrorPage";
    }

    public final String findAffirmForward(String controllerName, String forwardCode, String queryStringParam) {
        StringBuffer forward = new StringBuffer(findAffirmForward(controllerName, forwardCode));
        forward.append(queryStringParam);
        return forward.toString();
    }
}
