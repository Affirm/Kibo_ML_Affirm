package com.deplabs.affirm.app.b2c.p2p;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tiles.Attribute;
import org.apache.tiles.AttributeContext;
import org.apache.tiles.request.Request;
import org.marketlive.biz.cart.basket.IManagedBasket;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.entity.currency.IAmount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deplabs.affirm.app.b2c.p2p.AffirmProductMonthlyPaymentPriceModel;
import com.deplabs.affirm.app.b2c.p2p.AffirmProductMonthlyPaymentPricePreparer;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.system.annotation.ApplicationController;
import com.marketlive.web.tiles.AbstractViewPreparer;

@ApplicationController
public class AffirmProductMonthlyPaymentPricePreparer extends  AbstractViewPreparer {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(AffirmProductMonthlyPaymentPricePreparer.class);

    public static final String PAYMENT_AFFIRM_APR_LOAN = "custom.affirmpayment_monthly_payment_apr_loan";  	// percentage assumed APR for loan
    public static final String PAYMENT_AFFIRM_MONTHS = "custom.affirmpayment_monthly_payment_months";  		// can be 3, 6, or 12
	private static final String AFFIRM_MONTHLY_PAYMENT_GLOABAL_CART = "custom.affirmpayment_monthly_payment_globalcart_enabled";	
	private static final String IS_AFFIRM_MONTHLY_PAYMENT_GLOABAL_CART = "isAffirmMonthlyPaymentGlobalCartEnabled";
	private static final String AFFIRM_MONTHLY_PAYMENT_BASKET = "custom.affirmpayment_monthly_payment_basket_enabled";	
	private static final String IS_AFFIRM_MONTHLY_PAYMENT_BASKET = "isAffirmMonthlyPaymentBasketEnabled";
	public static final String PAYMENT_AFFIRM_MONTHY_PAYMENT_PRODUCT_ENABLED = "custom.affirmpayment_monthly_payment_product_enabled";
	public static final String IS_PAYMENT_AFFIRM_MONTHY_PAYMENT_PRODUCT_ENABLED = "isAffirmMonthlyPaymentProductEnabled";
    public static final String PAYMENT_AFFIRM_MONTHY_PAYMENT_DIRECTORY_ENABLED = "custom.affirmpayment_monthly_payment_directory_enabled";
    public static final String IS_PAYMENT_AFFIRM_MONTHY_PAYMENT_DIRECTORY_ENABLED = "isAffirmMonthlyPaymentDirectoryEnabled";
	private static final String SUMMARY_AMOUNT = "summaryAmount";
	private static final String AFFIRM_MONTHLY_PAYMENT_CART_SUMMARY_ENABLED = "custom.affirmpayment_monthly_payment_cartsummary_enabled";
	private static final String IS_AFFIRM_MONTHLY_PAYMENT_CART_SUMMARY_ENABLED = "isAffirmMonthlyPaymentCartSummaryEnabled";
    public static final String DIRECTORY_CONTEXT = "directory";

    public static final String PAYMENT_AFFIRM_MIN_PRICE_RANGE = "custom.affirmpayment_monthly_payment_min_price_range"; // 5000 ($50.00)
    public static final String PAYMENT_AFFIRM_MAX_PRICE_RANGE= "custom.affirmpayment_monthly_payment_max_price_range"; //17500 ($175.00)

    /**
     * Configures the {@link AffirmProductMonthlyPaymentPriceModel} bean in request scope.
     *
     * @param viewRequest      the {@link Request}
     * @param attributeContext the {@link AttributeContext}
     */
    @Override
    public void execute(Request viewRequest, AttributeContext attributeContext) {
        HttpServletRequest request = getServletRequest(viewRequest);
        try{
        	AffirmProductMonthlyPaymentPriceModel model = getModel(request);
        	
        	model.setAprLoan(configurationManager.getAsString(PAYMENT_AFFIRM_APR_LOAN));
        	model.setMonths(configurationManager.getAsInt(PAYMENT_AFFIRM_MONTHS));
        	
        	ICommerceSession commerceSession = WebUtil.getCommerceSession(request);
			IManagedBasket basket = commerceSession.getBasket();
			if(basket != null){
				IAmount totalAmount = basket.getTotal();
				model.setAttribute(SUMMARY_AMOUNT, totalAmount);		
			}
			
			model.setAttribute(IS_AFFIRM_MONTHLY_PAYMENT_CART_SUMMARY_ENABLED, configurationManager.getAsBoolean(AFFIRM_MONTHLY_PAYMENT_CART_SUMMARY_ENABLED, false));
       		model.setAttribute(IS_PAYMENT_AFFIRM_MONTHY_PAYMENT_DIRECTORY_ENABLED, configurationManager.getAsBoolean(PAYMENT_AFFIRM_MONTHY_PAYMENT_DIRECTORY_ENABLED, false));       	
            model.setAttribute(IS_PAYMENT_AFFIRM_MONTHY_PAYMENT_PRODUCT_ENABLED, configurationManager.getAsBoolean(PAYMENT_AFFIRM_MONTHY_PAYMENT_PRODUCT_ENABLED, false));
            model.setAttribute(IS_AFFIRM_MONTHLY_PAYMENT_GLOABAL_CART, configurationManager.getAsBoolean(AFFIRM_MONTHLY_PAYMENT_GLOABAL_CART, false));       	
            model.setAttribute(IS_AFFIRM_MONTHLY_PAYMENT_BASKET, configurationManager.getAsBoolean(AFFIRM_MONTHLY_PAYMENT_BASKET, false));
        	
        	model.setMinRangePrice(configurationManager.getAsInt(PAYMENT_AFFIRM_MIN_PRICE_RANGE));
        	model.setMaxRangePrice(configurationManager.getAsInt(PAYMENT_AFFIRM_MAX_PRICE_RANGE));
        	
        } catch (Exception e) {

		}
        
    }
    
    
    /**
     * Returns the {@link AffirmProductMonthlyPaymentPriceModel}, instantiating it if required.
     *
     * @param request the {@link HttpServletRequest}
     * @return the {@link AffirmProductMonthlyPaymentPriceModel}
     * @throws
     */
    protected AffirmProductMonthlyPaymentPriceModel getModel(HttpServletRequest request) throws Exception {
        AffirmProductMonthlyPaymentPriceModel model = (AffirmProductMonthlyPaymentPriceModel) request.getAttribute(AffirmProductMonthlyPaymentPriceModel.NAME);
        if (model == null) {
            model = new AffirmProductMonthlyPaymentPriceModel();
            request.setAttribute(AffirmProductMonthlyPaymentPriceModel.NAME, model);
        }
        return model;
    }

}
