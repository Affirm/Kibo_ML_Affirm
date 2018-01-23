package com.deplabs.app.b2c.thirdparty.affirm.payments.common;

import com.marketlive.app.service.ServiceContext;

public class AffirmJavaScriptSDKModel extends ServiceContext {

    /** Identifies a unique name for this model. */
    public static final String NAME = "AffirmJavaScriptSDKModel";
    
    public static final String AFFIRM_API_KEY = "affirmApiKey"; 
    
    /** The JS include URL for Affirm Payments*/
    public static final String AFFIRM_JS_URL = "affirmJSUrl";

    /** Indicates if Affirm Affirm is enabled/disabled. */
    public static final String AFFIRM_ENABLED = "isAffirmEnabled";    
    
    /** The URL to the 'what is' site */
    public static final String WHAT_IS_AFFIRM_URL = "whatIsAffirmURL";
    
    /**
     * Returns a string which holds the JS include URL for Affirm Payments.
     * @return a String.
     */
    public String getAffirmJSUrl() {
        Object attribute = getAttribute(AFFIRM_JS_URL);
        return (attribute != null) ? (String)attribute : null;
    }

    /**
     * Sets a string for holding the JS include URL for Affirm Payments.
     * @param amazonJSUrl
     */
    public void setAffirmJSUrl(String affirmJSUrl) {
        setAttribute(AFFIRM_JS_URL, affirmJSUrl);
    }

    /**
     * Returns a string which holds the public api key for Affirm Payments.
     * @return a String.
     */
    public String getAffirmApiKey() {
        Object attribute = getAttribute(AFFIRM_API_KEY);
        return (attribute != null) ? (String)attribute : null;
    }

    /**
     * Sets a string for holding the public api key for Affirm Payments.
     * @param amazonJSUrl
     */
    public void setAffirmApiKey(String publicApiKey) {
        setAttribute(AFFIRM_API_KEY, publicApiKey);
    }
    
    /**
     * Returns a boolean which indicates if Affirm Payments is enabled/disabled.
     * @return a boolean.
     */
    public boolean isAffirmEnabled() {
        Object attribute = getAttribute(AFFIRM_ENABLED);
        return (attribute != null) ? (Boolean)attribute : false;
    }

    /**
     * Sets a boolean which indicates if Affirm Payments is enabled/disabled.
     * @param isAffirmEnabled
     */
    public void setAffirmEnabled(boolean isAffirmEnabled) {
        setAttribute(AFFIRM_ENABLED, isAffirmEnabled);
    }
    
    /**
     * Returns a string which holds the 'What Is Affirm' URL page 
     * @return a String.
     */
    public String getWhatIsAffirmURL() {
        Object attribute = getAttribute(WHAT_IS_AFFIRM_URL);
        return (attribute != null) ? (String)attribute : null;
    }

    /**
     * Sets a string for holding the 'What Is Affirm' URL page 
     * @param amazonJSUrl
     */
    public void setWhatIsAffirmURL(String url) {
        setAttribute(WHAT_IS_AFFIRM_URL, url);
    }
}
