package com.deplabs.affirm.app.b2c.p2p;

import com.marketlive.app.service.ServiceContext;

public class AffirmProductMonthlyPaymentPriceModel  extends ServiceContext {

    /** Identifies a unique name for this model. */
    public static final String NAME = "MonthlyPaymentPriceModel";
    
    /**  monthly payment price APR loan*/ 
    public static final String AFFIRM_APR_LOAN = "affirmAprLoan"; 
    
    /** Affim monthly payment price months*/
    public static final String AFFIRM_MONTHS = "affirmMonths";
    
    /** Indicates if  is enabled/disabled. */
    public static final String AFFIRM_ENABLED = "isEnabled";   
    
    /**Only display as low as for items over AFFIRM_MIN_RANGE_PRICE */
    public static final String AFFIRM_MIN_RANGE_PRICE = "minRangePrice";
    
    /** Only display as low as for items less than AFFIRM_MIN_RANGE_PRICE */
    public static final String AFFIRM_MAX_RANGE_PRICE = "maxRangePrice";
    
    

    /**
     *
     * @return a String.
     */
    public int getMonths() {
        Object attribute = getAttribute(AFFIRM_MONTHS);
        return (attribute != null) ? (Integer)attribute : null;
    }

    /**
     * @param affirmMonths
     */
    public void setMonths(int affirmMonths) {
        setAttribute(AFFIRM_MONTHS, affirmMonths);
    }

    /**
     * Returns a string which holds the public api key for  Payments.
     * @return a String.
     */
    public String getAprLoan() {
        Object attribute = getAttribute(AFFIRM_APR_LOAN);
        return (attribute != null) ? (String)attribute : null;
    }

    /**
     * Sets a string for percentage assumed APR for loan
     * @param affirmAprLoan
     */
    public void setAprLoan(String affirmAprLoan) {
        setAttribute(AFFIRM_APR_LOAN, affirmAprLoan);
    }
    
    /**
     * Returns a boolean which indicates if  Payments is enabled/disabled.
     * @return a boolean.
     */
    public boolean isEnabled() {
        Object attribute = getAttribute(AFFIRM_ENABLED);
        return (attribute != null) ? (Boolean)attribute : false;
    }

    /**
     * Sets a boolean which indicates if  Payments is enabled/disabled.
     * @param isEnabled
     */
    public void setEnabled(boolean isEnabled) {
        setAttribute(AFFIRM_ENABLED, isEnabled);
    }

    public int getMinRangePrice() {
        Object attribute = getAttribute(AFFIRM_MIN_RANGE_PRICE);
        return (attribute != null) ? (Integer)attribute : null;
    }

    
    public void setMinRangePrice(int affirmMinRangePrice) {
        setAttribute(AFFIRM_MIN_RANGE_PRICE, affirmMinRangePrice);
    }

    public int getMaxRangePrice() {
        Object attribute = getAttribute(AFFIRM_MAX_RANGE_PRICE);
        return (attribute != null) ? (Integer)attribute : null;
    }

    
    public void setMaxRangePrice(int affirmMaxRangePrice) {
        setAttribute(AFFIRM_MAX_RANGE_PRICE, affirmMaxRangePrice);
    }
}
