package com.deplabs.app.service.checkout.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marketlive.biz.cart.basket.IManagedBasket;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.entity.account.IContact;
import org.marketlive.entity.cart.ICartShipment;
import org.marketlive.entity.cart.basket.IBasketItem;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.entity.discount.IDiscount;
import org.marketlive.entity.discount.IDiscountCategory;
import org.marketlive.entity.discount.IDiscountDefinition;
import org.marketlive.entity.product.IProduct;
import org.marketlive.entity.sku.ISku;
import org.marketlive.page.address.IAddress;
import org.marketlive.system.config.IConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import com.deplabs.affirm.app.b2c.checkout.IAffirmCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmConfigCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmMerchantModel;
import com.deplabs.affirm.app.b2c.checkout.IAffirmMetadataCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.cart.IAffirmBasketItemModel;
import com.deplabs.affirm.app.b2c.checkout.cart.impl.AffirmBasketItemModel;
import com.deplabs.affirm.app.b2c.checkout.discounts.IAffirmDiscountItemModel;
import com.deplabs.affirm.app.b2c.checkout.discounts.impl.AffirmDiscountItemModel;
import com.deplabs.affirm.app.b2c.checkout.impl.AffirmCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.impl.AffirmConfigCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.impl.AffirmMerchantModel;
import com.deplabs.affirm.app.b2c.checkout.impl.AffirmMetadataCheckoutModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmAddressModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmContactModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.IAffirmPersonModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.impl.AffirmAddressModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.impl.AffirmContactModel;
import com.deplabs.affirm.app.b2c.checkout.shipping.impl.AffirmPersonModel;
import com.deplabs.app.service.checkout.IAffirmCheckoutService;
import com.deplabs.app.service.checkout.IAffirmPaymentService;
import com.marketlive.app.b2c.WebUtil;
import com.marketlive.app.b2c.checkout.CheckoutAlertModel;
import com.marketlive.app.b2c.checkout.CheckoutButtonModel;
import com.marketlive.app.b2c.checkout.ICheckoutAlertModel;
import com.marketlive.app.b2c.checkout.ICheckoutButtonModel;
import com.marketlive.app.b2c.checkout.ICheckoutModel;
import com.marketlive.app.b2c.checkout.ICheckoutStep;
import com.marketlive.app.b2c.checkout.ICheckoutTargetData;
import com.marketlive.app.b2c.nav.DefaultLinkGenerator;
import com.marketlive.app.b2c.nav.ILinkGenerator;
import com.marketlive.app.b2c.struts.security.SSLUtility;
import com.marketlive.app.service.IServiceContext;
import com.marketlive.app.service.checkout.IBillingService;
import com.marketlive.app.service.checkout.ICheckoutService;
import com.marketlive.app.service.checkout.ICheckoutServiceContext;
import com.marketlive.app.service.checkout.impl.CheckoutService;
import com.marketlive.biz.borderfree.BorderFreeConstants;
import com.marketlive.biz.borderfree.IFormatPriceData;
import com.marketlive.entity.cart.basket.BasketItem;
import com.marketlive.entity.currency.Amount;
import com.marketlive.entity.helper.DiscountCategoryLink;
import com.marketlive.entity.sku.Sku;
import com.marketlive.system.annotation.ApplicationService;
import com.marketlive.system.config.ConfigurationManager;
import com.marketlive.system.text.AmountFormat;
import com.marketlive.util.VersionModel;

@ApplicationService
@Primary
public class AffirmCheckoutService extends CheckoutService  implements IAffirmCheckoutService {
	
	private static Log log = LogFactory.getLog(AffirmCheckoutService.class);

	public static final String PAYMENT_AFFIRM_API_KEY = "custom.affirmpayment_public_api_key"; //3JI8S4SX92K6DENK
																								//Private API key: SVBwsdxChDMbvUAE3q2uyoMtep3HJGdZ
	public static final String PAYMENT_AFFIRM_FINANCIAL_PRODUCT_KEY = "custom.affirmpayment_financial_product_key";  //J6WICDJTT1Y27X45
	public static final String PAYMENT_AFFIRM_CANCEL_URL = "custom.affirmpayment_checkout_cancel_url";  // /checkout/affirm-payment/payment.do?method=cancel
	public static final String PAYMENT_AFFIRM_CONFIRM_URL = "custom.affirmpayment_checkout_confirm_url"; // /checkout/affirm-payment/payment.do?method=confirm
	public static final String PAYMENT_AFFIRM_CONFIRM_URL_ACTION = "custom.affirmpayment_checkout_confirm_url_action"; // POST
	public static final String PAYMENT_AFFIRM_PLATFORM_PREFIX = "custom.affirmpayment_platform_prefix"; // modules-affirm
	public static final String PAYMENT_AFFIRM_PLATFORM_TYPE = "custom.affirmpayment_platform_type"; // "Kibo Commerce" 
	
	
    public static final String MSG_ERROR_HEADER = "msg.resource.affirmpayment_checkout_errorHeader";//"msg.thirdparty.amazon.payments.checkout.errorHeader";
    public static final String ALT_TXT_CONTINUE = "btn.altTxt.continue";
    public static final String MSG_ALTERNATIVE_PAYMENT = "msg.resource.affirmpayment_alternativePayment";
    public static final String ALERT_TYPE_DANGER = "alert-danger";
    public static final String REDIRECT = "redirect";
    public static final String DEFAULT_CHECKOUT_ACTION = "/feature.do?itemType=CHECKOUTACTION";
	
    public static final String PAYMENT_AFFIRM_DISCOUNT_CATEGORY = "custom.affirmpayment_checkout_discount_category"; // Affirm
    
    private static final String IMAGE = "image";
    
    private static final String PLATFORM_KEY = "Platform";
    
    @Autowired @Qualifier(ILinkGenerator.NAME)
    protected DefaultLinkGenerator linkGenerator;
	
    public void getAffirmPaymentTargetData(HttpServletRequest request, HttpServletResponse response, List targetDataForUpdate, ICheckoutServiceContext serviceContext) throws Exception {

        IAffirmCheckoutModel affirmCheckoutModel = getAffirmCheckoutModel(serviceContext);
        
        ICheckoutTargetData targetMap = getTargetData("steps", ICheckoutService.STEP_ORDER, "affirm", affirmCheckoutModel);

        targetDataForUpdate.add(targetMap);
        
        if (log.isDebugEnabled()) {
        	log.debug("Affirm Checkout Model is: " + affirmCheckoutModel.toString());
        }
    
    }
    
	public ICheckoutModel getCheckoutModel(ICheckoutServiceContext serviceContext) throws Exception {
		
		ICheckoutModel checkoutModel = super.getCheckoutModel(serviceContext);
		Object stepId = serviceContext.getAttribute("stepId");
		if (ICheckoutService.STEP_ORDER.equals(stepId)) {
			IAffirmCheckoutModel target = getAffirmCheckoutModel(serviceContext);
			if(target != null){
				ICheckoutTargetData targetMap = getTargetData("steps", ICheckoutService.STEP_ORDER, "affirm", target);
				List<ICheckoutTargetData> targetDataForUpdate = checkoutModel.getTargetDataForUpdate();
				if(CollectionUtils.isEmpty(targetDataForUpdate)){
					targetDataForUpdate = new ArrayList<ICheckoutTargetData>();
				}
				targetDataForUpdate.add(targetMap);
				checkoutModel.setTargetDataForUpdate(targetDataForUpdate);
			}
		}
		return checkoutModel;
	}
	
	public IAffirmCheckoutModel getAffirmCheckoutModel(ICheckoutServiceContext serviceContext){
		
			IAffirmCheckoutModel affirmCheckoutModel = new AffirmCheckoutModel();
			affirmCheckoutModel.setConfig(getAffirmConfigCheckout());
			affirmCheckoutModel.setMerchant(getAffirmMerchantModel());
			affirmCheckoutModel.setShipping(getAffirmShippingInfoModel(serviceContext));
			affirmCheckoutModel.setBilling(getAffirmBillingInfoModel(serviceContext));
			affirmCheckoutModel.setItems(getAffirmBasketItems(serviceContext));
		affirmCheckoutModel.setCurrency(this.getCurrency().getCurrencyCode());
			affirmCheckoutModel.setOrderId(serviceContext.getBasket().getPk().getAsString());
			affirmCheckoutModel.setMetadata(getAffirmMetadataModel(serviceContext));
		affirmCheckoutModel.setTaxAmount(this.formatToIntegerUSCents(serviceContext, super.getTaxTotal(serviceContext)));
		affirmCheckoutModel.setShippingAmount(this.formatToIntegerUSCents(serviceContext, super.getShippingTotal(serviceContext)));
		affirmCheckoutModel.setTotal(this.formatToIntegerUSCents(serviceContext, super.getTotal(serviceContext)));

			String financingProgram = getFinancingProgram(serviceContext);
			if(StringUtils.isNotBlank(financingProgram)){
				affirmCheckoutModel.setFinancingProgram(financingProgram);
			}
			
		// finally set the discounts once the totals/subtotals/thipping amounts were calculated
		affirmCheckoutModel.setDiscounts(getAffirmDiscounts(serviceContext));
		
			return affirmCheckoutModel;
	}
	
	protected String getFinancingProgram(ICheckoutServiceContext serviceContext){
			try{
				ICommerceSession session = (ICommerceSession) serviceContext.getAttribute(ICheckoutServiceContext.COMMERCE_SESSION);
				IManagedBasket basket = session.getBasket();
				String affirmDiscountCategoryName = configurationManager.getAsString(PAYMENT_AFFIRM_DISCOUNT_CATEGORY);
				if(StringUtils.isNotBlank(affirmDiscountCategoryName)){
					for(Object oDiscount : basket.getDiscounts()) {
						IDiscount discount = (IDiscount) oDiscount;
						if(discount.getDiscountResult() != null && discount.getDiscountResult().getDiscountDefinition() != null){
							for(Object oDiscountCatLinks : discount.getDiscountResult().getDiscountDefinition().getDiscountCategoryLinks()){
								DiscountCategoryLink discountCategoryLink = (DiscountCategoryLink) oDiscountCatLinks;
								if(getFinancingProgram(discountCategoryLink.getDiscountCategory(),affirmDiscountCategoryName)){
									return discountCategoryLink.getDiscount().getDescription();
								}
							}
						}
					}
				}
			}catch (Exception e) {
				
			}
			return null;
	}
	
	protected boolean getFinancingProgram(IDiscountCategory discountCategory, String affirmDiscountCategoryName){
		if(discountCategory != null){
			if(affirmDiscountCategoryName.equalsIgnoreCase(discountCategory.getName())){
					return true;
			} else {
				// FIx avoid infinit loop
				IDiscountCategory parentCategory = discountCategory.getParentDiscountCategory();
				if (parentCategory != null && parentCategory.getPk().getAsString().equals(discountCategory.getPk().getAsString())) {
					return false;
				}
				return getFinancingProgram(parentCategory, affirmDiscountCategoryName); 
			}
		}
		return false;
	}
	
	
	protected IAffirmConfigCheckoutModel getAffirmConfigCheckout(){
		IAffirmConfigCheckoutModel affirmConfigCheckoutModel = new AffirmConfigCheckoutModel();
		String financialProductKey = configurationManager.getAsString(PAYMENT_AFFIRM_FINANCIAL_PRODUCT_KEY);
		affirmConfigCheckoutModel.setFinancialProductKey(financialProductKey);
		return affirmConfigCheckoutModel;
	}
	
	protected IAffirmMerchantModel getAffirmMerchantModel(){
		IAffirmMerchantModel affirmMerchantModel = new AffirmMerchantModel();
		
		String affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_API_KEY);
		affirmMerchantModel.setPublicApiKey(affirmProperty);
		
		affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_CANCEL_URL);
		affirmMerchantModel.setUserCancelUrl(affirmProperty);
		
		affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_CONFIRM_URL);
		affirmMerchantModel.setUserConfirmationUrl(affirmProperty);
		
		affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_CONFIRM_URL_ACTION);
		affirmMerchantModel.setUserConfirmationUrlAction(affirmProperty);
		
		return affirmMerchantModel;
	}
	
	protected IAffirmContactModel getAffirmShippingInfoModel(ICheckoutServiceContext serviceContext){
		IAffirmContactModel affirmModel = new AffirmContactModel();

		ICommerceSession session = (ICommerceSession) serviceContext.getAttribute(ICheckoutServiceContext.COMMERCE_SESSION);
		
		List<Object> values = new ArrayList<Object>();
        Object shippingStyle = serviceContext.getAttribute(ICheckoutServiceContext.SHIPPING_STYLE);
        if (IBillingService.OPTION_SHIPTO_MULTIPLE_ADDRESSES.equals(shippingStyle)) {
           /* int i = 1;
            for (Object obj : session.getBasket().getShipmentList()) {
                ICartShipment shipment = (ICartShipment) obj;
                IContact contact = shipment.getShipToContact();
                if (contact != null) {
                    String person = getPersonString(contact.getPerson());
                    String value = i + ". " + person;
                    values.add(value);
                }
                i++;
            }*/
        } else {
            if (session.getBasket().getShipmentList() != null) {
                for (Object obj : session.getBasket().getShipmentList()) {
                    ICartShipment shipment = (ICartShipment) obj;
                    IContact contact = shipment.getShipToContact();
                    
                    IAffirmPersonModel person = getAffirmPersonModel(contact);
                    
                    
                    IAddress address = contact.getAddress();
                    IAffirmAddressModel affirmAddress = getAffirmAddressModel(address);
                 
                    affirmModel.setName(person);
                    affirmModel.setAddress(affirmAddress);
                }
            }

        }
	
		return affirmModel;
	}
	
	protected IAffirmContactModel getAffirmBillingInfoModel(ICheckoutServiceContext serviceContext){
		IAffirmContactModel affirmModel = new AffirmContactModel();
		IContact contact = serviceContext.getBillContact();
		IAffirmPersonModel person = getAffirmPersonModel(contact);
        
        
        IAddress address = contact.getAddress();
        IAffirmAddressModel affirmAddress = getAffirmAddressModel(address);
     
        affirmModel.setName(person);
        affirmModel.setAddress(affirmAddress);
        
        return affirmModel;
	}
	
	protected IAffirmPersonModel getAffirmPersonModel(IContact contact){
		
		IAffirmPersonModel person = new AffirmPersonModel();
        person.setFirst(contact.getPerson().getFirstName());
        person.setLast(contact.getPerson().getLastName());
    
        return person;
	}
	
	protected IAffirmAddressModel getAffirmAddressModel(IAddress address){

		IAffirmAddressModel affirmAddress = new AffirmAddressModel();
        affirmAddress.setLine1(address.getStreet1());
        String line2 = StringUtils.isBlank(address.getStreet2()) ? "":address.getStreet2();
        affirmAddress.setLine2(line2);
        affirmAddress.setCity(address.getCity());
        
        String countryCode = getCountryCode(address);
        if (countryCode.equalsIgnoreCase("US") || countryCode.equalsIgnoreCase("CA")) {
            if (null != address.getState()) {
                affirmAddress.setState(address.getState().getStateCode());
                if (StringUtils.isNotBlank(address.getPostalCode())) {
                	affirmAddress.setZipcode(address.getPostalCode());
                }
            }
        } else {
            if(StringUtils.isNotBlank(address.getRegion())){
            	affirmAddress.setState(address.getRegion());
            }
            if (StringUtils.isNotBlank(address.getPostalCode())) {
            	affirmAddress.setZipcode(address.getPostalCode());
            }
        }
        return affirmAddress;
        
	}
		
	protected Map<String,Map<String,String>> getDiscounts(ICheckoutServiceContext serviceContext){
		Map<String,Map<String,String>> discounts = null;
		return discounts;
	}

	public List<Object> getPaymentInfo(IServiceContext serviceContext){
		return super.getPaymentSummary(serviceContext);
	}

	protected String formatToIntegerUSCents(ICheckoutServiceContext serviceContext, String amount) {
		if (StringUtils.isEmpty(amount))
			return "";
		
		ICommerceSession commerceSession = (ICommerceSession) serviceContext.getAttribute(ICheckoutServiceContext.COMMERCE_SESSION);
	     Currency currency = getCurrency(commerceSession);
	     String formattedPrice = amount.replace(currency.getSymbol(), "");
	     formattedPrice = formattedPrice.replace(".", "");
	     formattedPrice = formattedPrice.replace(",", "");
	      
	     return formattedPrice.trim();
	}
	
	protected String formatToIntegerUSCents(ICheckoutServiceContext serviceContext, IAmount amount) {
		if (null == amount) {
			return "";
		}
		return this.formatToIntegerUSCents(serviceContext, amount.toBigDecimal().setScale(2).toString());
	}

	 protected List<Object> getPaymentSummary(IServiceContext serviceContext) {
	        List<Object> values = super.getPaymentSummary(serviceContext);
	        ICommerceSession commerceSession = (ICommerceSession) serviceContext.getAttribute(ICheckoutServiceContext.COMMERCE_SESSION);
	        boolean borderFreeEnabledAndNotUSSelected = borderFreeManager.isBorderFreeEnabledAndNotUSSelected(commerceSession);
	        Currency currency = getCurrency(commerceSession);
	        List payments = (List) serviceContext.getAttribute(ICheckoutServiceContext.PAYMENTS);
	        boolean borderFreeEnabled = isBorderFreeEnabled();
	        if (payments != null && !payments.isEmpty()) {
	            // Affirm payment Summary data
	            for (Object payment : payments) {
	                if (IAffirmPaymentService.AFFIRM_PAYMENT_TYPE.equals(((IOrderPayment) payment).getDescription())) {
	                    IAmount paymentAmount = ((IOrderPayment) payment).getAmount();
	                    if (borderFreeEnabledAndNotUSSelected) {
	                        Map<String, IAmount> basketTotalMap = (Map<String, IAmount>) commerceSession.getAttribute(BorderFreeConstants.BASKET_TOTAL_MAP);
	                        paymentAmount = basketTotalMap.get(BorderFreeConstants.BASKET_TOTAL_PRICE);
	                    }
	                    String affirmSummary = String.format("%s %s %s", ((IOrderPayment) payment).getDescription(), getMessage(MSG_FOR), AmountFormat.format(paymentAmount, currency));
	                    values.add(affirmSummary);
	                }
	            }
	        }
	   	 return values;
    }
	 
	    /**
	     * Returns list of IBasketItemModels
	     *
	     * @param serviceContext
	     * @return
	     */
	    protected List<IAffirmBasketItemModel> getAffirmBasketItems(ICheckoutServiceContext serviceContext) {
	    	
	        List<IAffirmBasketItemModel> basketItemModels = new ArrayList<IAffirmBasketItemModel>();
	        IManagedBasket basket = serviceContext.getBasket();

	        List<IBasketItem> basketItems = serviceContext.getBasket().getSplitItems();

	        // load image paths
	        Map<String, Map<String, String>> imageProductMap = loadBasketItemData(basketItems);

	        for (Object item : basket.getItems()) {
	            BasketItem basketItem = (BasketItem) item;
	            IAffirmBasketItemModel model = getAffirmBasketItemModel(serviceContext,imageProductMap, basketItem);
	            basketItemModels.add(model);
	        }
	        return basketItemModels;
	    }
	 /**
	     * Returns basketItem model containing the id, image path, description,
	     * code, qty, price model,sku options, personalizations, and kit items (and
	     * their personalizations). Used to display basket item info for the
	     * multiple shipping, gift and delivery options, and shopping bag summary
	     * section for accordion checkout.
	     *
	     * @param imageProductMap
	     * @param basketItem
	     * @return
	     */
	    protected IAffirmBasketItemModel getAffirmBasketItemModel(ICheckoutServiceContext serviceContext, Map<String, Map<String, String>> imageProductMap, BasketItem basketItem) {
	    	IAffirmBasketItemModel model = new AffirmBasketItemModel();

	    	// set item#
	        if (basketItem.getProduct() != null) {
	            model.setSku(String.valueOf(basketItem.getProduct().getCode()));
	        }
	    	
	        // set image path
	        Map<String, String> imageMap = imageProductMap.get(basketItem.getProduct().getName());
	        if (imageMap != null) {
	            String imagePath = imageMap.get(IMAGE);
	            if (imagePath != null) {
	                model.setItemImageUrl(imagePath);
	            }
	        }
	        
	        // set product path
	        model.setItemUrl(getProductUrl(basketItem.getProduct()));

	        // set sku options
	        if (StringUtils.equalsIgnoreCase(basketItem.getCartItemType().getType(), Sku.itemType)) {

	        	ISku sku = (ISku) basketItem.getCartItemType();
	            
	        	model.setDisplayName(sku.getDefaultProduct().getName());
	            
	            model.setSku(sku.getCode());
	        }

	        // set qty
	        model.setQty(String.valueOf(basketItem.getQty()));

	        // set price 
	        // RM-27785: fix for freegift issues
	        IFormatPriceData bfPriceData = borderFreeManager.getFormatPriceDataBasket(serviceContext.getCommerceSession());
	        IAmount sellPrice = basketItem.getSellPrice();
	        IAmount regularPrice = basketItem.getRegularPrice();
	        String itemPrice = "";
	        if (greaterThan(regularPrice, sellPrice)) {
	        	itemPrice = AmountFormat.format(regularPrice, bfPriceData);
            } else {
            	itemPrice = AmountFormat.format(sellPrice, bfPriceData);
            }
	        model.setUnitPrice(formatToIntegerUSCents(serviceContext, itemPrice));

	        return model;
	    }
	    
	    private String getProductUrl(IProduct product) {
	    	String baseUrl = SSLUtility.getRedirectString(WebUtil.getCurrentServletRequest(), WebUtil.getCurrentServletRequest().getSession().getServletContext(), false);
            if(baseUrl != null) {
            	baseUrl = baseUrl.substring(0, baseUrl.indexOf(WebUtil.getCurrentServletRequest().getRequestURI()));
            } else {
            	baseUrl = createURLFromRequest(WebUtil.getCurrentServletRequest());
            }   
            
			String productUrl = linkGenerator.entityRef(product, null);
			if (StringUtils.isBlank(productUrl)) {
				return "";
			}
			
			return baseUrl + productUrl;
		}
	    
	    /**
	     * Method to create the URL from request.
	     * @param request
	     * @return
	     */
	    protected String createURLFromRequest(HttpServletRequest request) {
			StringBuffer stringBuffer = new StringBuffer();

			stringBuffer.append(request.getScheme());
	        stringBuffer.append("://");
			stringBuffer.append(request.getServerName());
	        if (request.getServerPort() != 80 && request.getServerPort() != 443)
	        {
	            stringBuffer.append(":");
	            stringBuffer.append(request.getServerPort());
	        }     
	           
	        return stringBuffer.toString();
		}

		protected Map<String, IAffirmDiscountItemModel> getAffirmDiscounts(ICheckoutServiceContext serviceContext) {
	    	Map<String, IAffirmDiscountItemModel> discounts = new HashMap<String, IAffirmDiscountItemModel>();
	    	
			// basket level discounts
			for (Object obj : serviceContext.getBasket().getDiscounts()) {
				IDiscount d = (IDiscount) obj;
				if (d.getAmount().compareTo(ZERO) == 1) {
					IAffirmDiscountItemModel discount = new AffirmDiscountItemModel();
					String message = "";
					if (d.getMessage() != null && !d.getMessage().isEmpty()) {
						message = d.getMessage();
					}
					discount.setDiscountDisplayName(message);
                    discount.setDiscountAmount(formatToIntegerUSCents(serviceContext, d.getAmount()));

                    discounts.put(message, discount);
				}
			}
	    	 
			// basket items level discounts
			List<IBasketItem> basketItems = serviceContext.getBasket().getSplitItems();
	    	 for (IBasketItem item : basketItems) {
		            Map<String, IAffirmDiscountItemModel> itemDiscounts = getAffirmItemDiscounts(item); 
		            discounts.putAll(itemDiscounts);
		     }
	    	 
			// shipping level discounts
	    	 addShippingDiscountMessage(serviceContext, discounts);
	    	 
	    	 return discounts;
	    }
	    
	    protected Map<String, IAffirmDiscountItemModel> getAffirmItemDiscounts(IBasketItem basketItem) {
	    	Map<String, IAffirmDiscountItemModel> discounts = new HashMap<String, IAffirmDiscountItemModel>(); 
	    	List discountsItem = basketItem.getSortedDiscounts();
	    	
	    	IAffirmDiscountItemModel discount = new AffirmDiscountItemModel();
	        if (!discounts.isEmpty()) {
	            for (Object obj : discountsItem) {
	                IDiscount d = (IDiscount) obj;
	                if (d.getAmount().compareTo(ZERO) == 1) {
	                    String dMssg = " ";
	                    if (d.getMessage() != null && !d.getMessage().isEmpty()) {
	                        dMssg = d.getMessage();
	                    }
	                    discount.setDiscountDisplayName(dMssg);
	                    discount.setDiscountAmount(AmountFormat.format(d.getAmount(), getCurrency()));

	                    discounts.put(dMssg, discount);
	                }
	            }
	        }
	        return discounts;
	    }
	    
	    private void addShippingDiscountMessage(ICheckoutServiceContext serviceContext, Map<String, IAffirmDiscountItemModel> discounts) {
	        Map<IDiscountDefinition, List<IDiscount>> shippingDiscountMap = new HashMap<IDiscountDefinition, List<IDiscount>>();
	        
	        for (Object obj : serviceContext.getBasket().getShipmentList()) {
	            ICartShipment s = (ICartShipment) obj;

	            for (Object d : s.getDiscounts()) {
	                IDiscount discount = (IDiscount) d;
	                IDiscountDefinition discountDefinition = discount.getDiscountResult().getDiscountDefinition();
	                
	                List<IDiscount> discountList = shippingDiscountMap.get(discountDefinition);
	                discountList = (discountList == null) ? new ArrayList<IDiscount>() : discountList;
	                discountList.add(discount);
	                
	                shippingDiscountMap.put(discountDefinition, discountList);
	            }
	        }

	        for (IDiscountDefinition discountDefinition : shippingDiscountMap.keySet()) {
	        	
	            List<IDiscount> discountList = shippingDiscountMap.get(discountDefinition);
	            IAmount discountAmount = new Amount(ZERO);

	            for (IDiscount discount : discountList) {
	                discountAmount.add(discount.getAmount());
	            }
	            String message = discountDefinition.isShowMessage() ? discountDefinition.getMessage() : "";
	            
	            IAffirmDiscountItemModel itemDiscount = new AffirmDiscountItemModel();
	            
	            itemDiscount.setDiscountDisplayName(message);
	            itemDiscount.setDiscountAmount(formatToIntegerUSCents(serviceContext, discountAmount));
	            
	            discounts.put(discountDefinition.getName(), itemDiscount);
	        }
	    }
	    
	    public IAffirmMetadataCheckoutModel getAffirmMetadataModel(ICheckoutServiceContext serviceContext){
	    	IAffirmMetadataCheckoutModel metadata = new AffirmMetadataCheckoutModel();

	    	for (Object obj : serviceContext.getBasket().getShipmentList()) {
	            ICartShipment s = (ICartShipment) obj;
	            if(s.getShippingMethod() != null){
	            	metadata.setShippingType(s.getShippingMethod().getName());
	            }
	    	}
	    	String platformVersion = getPlatformVersion();
	    	metadata.setPlatformVersion(platformVersion);
	    	metadata.setOrderId(serviceContext.getBasket().getPk().getAsString());
	    	
	    	String affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_PLATFORM_PREFIX) + "-" + platformVersion;
	    	metadata.setPlatformAffirm(affirmProperty);
	    	
	    	affirmProperty = configurationManager.getAsString(PAYMENT_AFFIRM_PLATFORM_TYPE);
	    	metadata.setPlatformType(affirmProperty);
	    	
	    	return metadata;
	    }
	    
	    private String getPlatformVersion() {
	    	String platformVersion = null;
	    	String siteHome = System.getProperty(IConfigurationManager.CONTEXT_KEY_PREFIX + ConfigurationManager.SITE_HOME);
	        String resourcePath = siteHome + VersionModel.VERSION_RESOURCE_PATH;
	        InputStream is = null;
	        try {
	            is = new BufferedInputStream(new FileInputStream(resourcePath));
	            Properties properties = new Properties();
	            properties.load(is);
	            for (Object key : properties.keySet()) {
	                String name = (String) key;
	                String value = (String) properties.get(name);
	                
	                if (PLATFORM_KEY.equalsIgnoreCase(name)) {
	                	platformVersion = value;
	                }
	            }
	        } catch (Exception e) {
	            log.error(String.format("Failed to load version properties from %s, exception: %s", resourcePath, e));
	        }
	        finally {
	            if (is != null) {
	                try {
	                    is.close();
	                }
	                catch (IOException e) {
	                    log.error(String.format("Failed to close version properties from %s, exception: %s", resourcePath, e));
	                }
	            }
	        }
	        return platformVersion;
		}

		public ICheckoutModel processViewAffirmError(HttpServletRequest request, HttpServletResponse response, ICheckoutServiceContext serviceContext) throws Exception{
	        ICheckoutModel model =  getCheckoutModel(serviceContext);

	        Object errorCode = request.getAttribute(IAffirmPaymentService.STEP_ERROR_CODE);
	        if (errorCode != null){
	            request.removeAttribute(IAffirmPaymentService.STEP_ERROR_CODE);
	            List<ICheckoutAlertModel> alertModel = new ArrayList<ICheckoutAlertModel>();
	            if (ICheckoutService.STEP_PAY.equals(ICheckoutService.STEP_PAY)){
	                alertModel = getAffirmPaymentAlertModel();
	            }
	            for (ICheckoutStep step : model.getSteps()){
	                if (serviceContext.getStepId().equals(step.getId())) {
	                    step.setAlerts(alertModel);
	                    break;
	                }
	            }
	        }
	        return model;
	    }
	    
	    /**
	     * Returns alerts for payment step.
	     * @return
	     */
	    protected List<ICheckoutAlertModel> getAffirmPaymentAlertModel(){
	        List<ICheckoutAlertModel> alerts = new ArrayList<ICheckoutAlertModel>();
	        List<ICheckoutButtonModel> buttons = new ArrayList<ICheckoutButtonModel>();
	        // Add the buttons to the list of buttons
	        ICheckoutButtonModel buttonModel = new CheckoutButtonModel();
	        buttonModel.setText(getMessage(ALT_TXT_CONTINUE));
	        buttonModel.setType(REDIRECT);
	        buttonModel.setAction(DEFAULT_CHECKOUT_ACTION);
	        buttons.add(buttonModel);
	        // Add the alert to the list of alerts
	        ICheckoutAlertModel alertModel = new CheckoutAlertModel();
	        alertModel.setButtons(buttons);
	        alertModel.setType(ALERT_TYPE_DANGER);
	        alertModel.setDismissible(Boolean.FALSE);
	        alertModel.setHeader(getMessage(MSG_ERROR_HEADER));
	        alertModel.setMessage(getMessage(MSG_ALTERNATIVE_PAYMENT));
	        // Add it to the list of alerts to be shown
	        alerts.add(alertModel);

	        return alerts;
	    }
}
