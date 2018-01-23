package com.deplabs.app.b2c.thirdparty.affirm.payments.common;

import javax.servlet.http.HttpServletRequest;

import org.apache.tiles.AttributeContext;
import org.apache.tiles.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marketlive.system.annotation.ApplicationController;
import com.marketlive.web.tiles.AbstractViewPreparer;

@ApplicationController
public class AffirmJavaScriptSDKPreparer extends AbstractViewPreparer {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AffirmJavaScriptSDKPreparer.class);

    public static final String PAYMENT_AFFIRM_JS_URL = "custom.affirmpayment_js_url";  	//SANDBOX: https://cdn1-sandbox.affirm.com
    																								//LIVE: https://cdn1.affirm.com 
    public static final String PAYMENT_AFFIRM_API_KEY = "custom.affirmpayment_public_api_key";
    
    public static final String PAYMENT_AFFIRM_ENABLED = "custom.affirmpayment_paymentenabled";
    
    public static final String PAYMENT_AFFIRM_WHAT_IS_URL = "custom.affirmpayment_whatis_url";

    /**
     * Configures the {@link AffirmJavaScriptSDKModel} bean in request scope.
     *
     * @param viewRequest      the {@link Request}
     * @param attributeContext the {@link AttributeContext}
     */
    @Override
    public void execute(Request viewRequest, AttributeContext attributeContext) {
        HttpServletRequest request = getServletRequest(viewRequest);

        log.debug("JavaScriptSDKPreparer.execute");

        try {
            // get model
            AffirmJavaScriptSDKModel model = getJavaScriptSDKModel(request);

            // set model
            model.setAffirmApiKey(configurationManager.getAsString(PAYMENT_AFFIRM_API_KEY));
            model.setAffirmJSUrl(configurationManager.getAsString(PAYMENT_AFFIRM_JS_URL));
            model.setWhatIsAffirmURL(configurationManager.getAsString(PAYMENT_AFFIRM_WHAT_IS_URL));
            boolean affirmEnabled = configurationManager.getAsBoolean(PAYMENT_AFFIRM_ENABLED, false);
            model.setAffirmEnabled(affirmEnabled);
            
        } catch (Exception e) {
            log.error("ERROR", e);
        }
    }

    /**
     * Returns the {@link AffirmJavaScriptSDKModel}, instantiating it if required.
     *
     * @param request the {@link HttpServletRequest}
     * @return the {@link AffirmJavaScriptSDKModel}
     * @throws
     */
    protected AffirmJavaScriptSDKModel getJavaScriptSDKModel(HttpServletRequest request) throws Exception {
        AffirmJavaScriptSDKModel model = (AffirmJavaScriptSDKModel) request.getAttribute(AffirmJavaScriptSDKModel.NAME);
        if (model == null) {
            model = new AffirmJavaScriptSDKModel();
            request.setAttribute(AffirmJavaScriptSDKModel.NAME, model);
        }
        return model;
    }
}