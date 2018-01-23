package com.deplabs.biz.checkout;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.marketlive.biz.cart.basket.ISourceCodeInfo;
import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.biz.checkout.IFinalizeOrderInfo;
import org.marketlive.biz.checkout.IPostFinalizeCheck;
import org.marketlive.biz.email.IVelocityMessagePreparator;
import org.marketlive.biz.email.IVelocityMessagePreparatorFactory;
import org.marketlive.biz.session.context.ICommerceSession;
import org.marketlive.entity.account.IContact;
import org.marketlive.entity.cart.ICartShipment;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderDiscount;
import org.marketlive.entity.cart.order.IOrderItem;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.entity.discount.IDiscount;
import org.marketlive.entity.site.ISite;
import org.marketlive.messaging.IEmailQueueMessage;
import org.marketlive.messaging.IQueueWriter;
import org.marketlive.system.config.multisite.ISiteAwareConfigurationManager;
import org.marketlive.system.locale.IMessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;

import com.marketlive.biz.borderfree.IBorderFreeManager;
import com.marketlive.biz.checkout.FinalizeOrderInfo;
import com.marketlive.biz.email.VelocityMessagePreparator;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.entity.cart.order.OrderPaymentGiftCertificate;
import com.marketlive.entity.currency.Amount;
import com.marketlive.messaging.EmailQueueMessage;
import com.marketlive.system.annotation.PlatformComponent;
import com.marketlive.system.locale.ActiveLanguage;
import com.marketlive.system.site.ActiveSite;

/**
 * Responsible for sending out Order confirmation emails. Called after Order has been saved to the database (finalized)
 * by CheckoutManager.finalizeOrder().
 */
@Primary
@PlatformComponent
public class OrderConfirmationEmailCheck implements IPostFinalizeCheck {
    /**
     * Defines failed status for this check.
     */
    private static final String RESULT_FAILED = "FAILED_SEND_ORDER_CONFIRMATION";
    /**
     * Logger for this class.
     */
    private static Logger log = LoggerFactory.getLogger(OrderConfirmationEmailCheck.class);

    private static final String MSG_ORDER_CONFIRMATION_SUBJECT = "orderconfirmation.subject";
    
    public static final String PROMO_CODE_PREFIX = "app.b2c.mod.visame.checkout.promo_code_prefix";
    
    public static final String PROMO_CODE_EMAIL_MSG = "app.b2c.mod.visame.checkout.promo_code_email_message";

    /**
     * Message preparator responsible for creating text and html content for emails.
     */
    @Autowired
    @Qualifier("orderConfirmationMessagePreparator")
    IVelocityMessagePreparatorFactory preparatorFactory;

    /**
     * Reference to the email queue writer where out going email requests are queued.
     */
    @Autowired
    @Qualifier("emailQueueWriter")
    private IQueueWriter emailQueueWriter;

    /**
     * Configuration Manager to pass to the message preparator.
     */
    @Autowired
    ISiteAwareConfigurationManager configManager;

    /**
     * Order Manager to pass to the message preparator.
     */
    @Autowired
    IOrderManager orderManager;

    /**
     * Message Resources needed to access localized data to be used in the order confirmation email.
     */
    @Autowired
    private IMessageResources messageResources;

    /**
     * Reference to the IBorderManager.
     */
    @Autowired
    IBorderFreeManager borderFreeManager;
    
    @Autowired
    private IAuthorizationDAO authorizationDAO;

    /**
     * Prepares order confirmation email content from the passed in Order object using Velocity framework and sends it
     * to the customer email address.
     * If fails to send the email:
     * <br>- logs the error,
     * <br>- sets the result in the returned <code>IFinalizeOrderInfo</code> object to <code>RESULT_FAILED</code>.
     * <p/>
     * Called by CheckoutManager.postFinalizeCheck().
     *
     * @param order           Instance of <code>IOrder</code> that needs to be evaluated by the routine after
     *                        the order has been finalized.
     * @param commerceSession Instance of <code>ICommerceSession</code>
     * @param infoItem        Instance of the <code>IFinalizeOrderInfo</code> returned by the previous post-finalize routine.
     *                        Can be used by this routine to alter it's processes.
     * @return Intance of <code>IFinalizeOrderInfo</code> with the information about this routine execution results or null.
     * @throws Exception for unrecoverable errors.
     */
    public IFinalizeOrderInfo evaluate(IOrder order, ICommerceSession commerceSession, IFinalizeOrderInfo infoItem) throws Exception {
        IFinalizeOrderInfo finalizeInfo = new FinalizeOrderInfo();
        finalizeInfo.setResult(IFinalizeOrderInfo.RESULT_SUCCESS);
        boolean isBorderFreeOrder = borderFreeManager.isBorderFreeOrder(order);
        if(!isBorderFreeOrder) {
	        try {
	            IVelocityMessagePreparator messagePreparator = preparatorFactory.getVelocityMessagePreparator();
	            messagePreparator.setTo(order.getBillToInfo().getEmail());
	
	            ISite site = ActiveSite.getSite();
	
	            String companyName = messageResources.getMessage(
	                    site, ActiveLanguage.getUserLocale(), IMessageResources.LOCALIZED_COMPANY_NAME_KEY);
	
	            String customerServicePhone = messageResources.getMessage(site, ActiveLanguage.getUserLocale(),
	                    IMessageResources.LOCALIZED_GLOBAL_CUSTOMER_SERVICE_PHONE_KEY);
	
	            String customerServiceEmailAddress = messageResources.getMessage(site, ActiveLanguage.getUserLocale(),
	                    IMessageResources.LOCALIZED_GLOBAL_CUSTOMER_SERVICE_EMAIL_KEY);
	
	            Map data = new HashMap();
	            data.put("order", order);
	            Map<String, String> productCatIds = loadOrderData(order);
	            data.put("certificateTotal", calculateCertificate(order));
	            data.put("totalCharge", calculateTotalCharge(order));
	            data.put("totalDiscount", calculateTotalOrderDiscount(order));
	            IAmount totalShipping = new Amount(calculateTotalShipping(order));
	            totalShipping.subtract(calculateTotalShippingDiscount(order));
	            data.put("totalShipping", totalShipping);
	            data.put("totalShippingDiscount", calculateTotalShippingDiscount(order));
	            data.put("youSaved", calculateYouSaved(order));
	            data.put("commercSession", commerceSession);
	            data.put("configManager", configManager);
	            // PEBL-13672 - Source code list modified for visa promo codes
	            List sourceCodeInfoListOfOrder = orderManager.getSourceCodeInfoListOfOrder(order);
	            this.modifySourceCodeInfoList(sourceCodeInfoListOfOrder);
	            data.put("sourceCodes", sourceCodeInfoListOfOrder);
	            data.put("locale", commerceSession.getLocale());
	            data.put("companyName", companyName);
	            data.put("customerServicePhone", customerServicePhone);
	            data.put("customerServiceEmailAddress", customerServiceEmailAddress);
	            
	            // START Affirm Customization
	            String affirmRequestId = getAffirmRequestId(order);
	            data.put("affirmCustomerDetailUrl", getAffirmCustomerDetailUrl(order, affirmRequestId));
	            data.put("affirmRequestId", affirmRequestId);
	            // END Affirm Customization
	
	            messagePreparator.setData(data);
	            messagePreparator.setLocale(commerceSession.getLocale());
	
	            String subject = messageResources.getMessage(site, ActiveLanguage.getUserLocale(), MSG_ORDER_CONFIRMATION_SUBJECT);
	            messagePreparator.setSubject(subject);
	
	            if (!(messagePreparator instanceof VelocityMessagePreparator)) {
	                throw new Exception("Invalid MessagePreparator, could not process OrderConfirmation.");
	            }
	
	            VelocityMessagePreparator velocityMessagePreparator = (VelocityMessagePreparator) messagePreparator;
	            String orderHTML = velocityMessagePreparator.createContent(velocityMessagePreparator.getHtmlTemplate());
	            String orderText = velocityMessagePreparator.createContent(velocityMessagePreparator.getTextTemplate());
	            // put the message data in a Hashmap for storage in the queue
	            Map<String, String> messageData = new HashMap<String, String>();
	            messageData.put("order", orderHTML);
	            messageData.put("ordertext", orderText);
	            messageData.put("company", companyName);
	            messageData.put("csphone", customerServicePhone);
	            messageData.put("csemail", customerServiceEmailAddress);
	            messageData.put(IEmailQueueMessage.PRODUCT_CODES, productCatIds.get(IEmailQueueMessage.PRODUCT_CODES));
	            messageData.put(IEmailQueueMessage.CATEGORY_CODES, productCatIds.get(IEmailQueueMessage.CATEGORY_CODES));
	            messageData.put(IEmailQueueMessage.TIME_STAMP, Long.toString(System.currentTimeMillis()));
	
	            String recipientEmail = order.getBillToInfo().getEmail();
	            IContact contact = order.getCustomer().getPrimaryContact();
	
	            // Put the order confirmation in the queue
	            IEmailQueueMessage qmessage = new EmailQueueMessage();
	            qmessage.queueOrderConfirmation(recipientEmail, messageData, contact);
	            emailQueueWriter.write(qmessage);
	
	        } catch (Exception e) {
	            //if the application needs to act on the routine results, return the finalize info object with the
	            // result value that will be recognized by the application, otherwise you can return null.
	            finalizeInfo.setResult(RESULT_FAILED);
	            log.error("Failed to send Order Confirmation Email. Order Pk:  " + order.getPk(), e);
	        }
        }
        return finalizeInfo;
    }
    
    /**
     * Get the Affirm Customer Order Detail URL 
     * @param order
     * @param affirmRequestId
     * @return
     */
	private Object getAffirmCustomerDetailUrl(IOrder order, String affirmRequestId) {
		String affirmPaymentCustomerDetailUrl = configManager.getAsString("custom.affirmpayment_customer_detail_url");
        String affirmPaymentPublicKey = configManager.getAsString("custom.affirmpayment_public_api_key");
        
        if (StringUtils.isNotBlank(affirmPaymentCustomerDetailUrl) && StringUtils.isNotBlank(affirmPaymentPublicKey)) {
        	return affirmPaymentCustomerDetailUrl + affirmRequestId + "?trk=" + affirmPaymentPublicKey;
        } else {
        	log.error("Cannot create the Affirm Customer Order Detail URL  because the configuration is wrong");
        	log.error("custom.affirmpayment_customer_detail_url = " + affirmPaymentCustomerDetailUrl);
        	log.error("custom.affirmpayment_public_api_key = " + affirmPaymentPublicKey);
        }
        
        
		return null;
	}

	/**
	 * Get the Affirm Authorization Request id of this order
	 * 
	 * @param order
	 * @return the affirm authorization request id
	 */
	private String getAffirmRequestId(IOrder order) {
		List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));
		if(authorizations != null && !authorizations.isEmpty()){
			return authorizations.get(0).getRequestID();
		}
		else {
			return "";
		}
	}

    /**
     * Calculating the total of discount were applied on order (You Saved)
     *
     * @param order
     * @return IAmount id total discount amounts
     */

    private IAmount calculateYouSaved(IOrder order) {
        IAmount youSavedAmount = new Amount(calculateTotalOrderDiscount(order));
        IAmount shippingDiscountAmount = calculateTotalShippingDiscount(order);
        youSavedAmount.add(shippingDiscountAmount);
        return youSavedAmount;
    }

    /**
     * Calculating the total of certificates were applied on order
     *
     * @param order
     * @return IAmount id total amounts
     */

    private IAmount calculateCertificate(IOrder order) {
        IAmount certificateAmount = new Amount();
        List payments = order.getPayments();

        for (Iterator i = payments.iterator(); i.hasNext(); ) {
            IOrderPayment orderPayment = (IOrderPayment) i.next();
            if (orderPayment instanceof OrderPaymentGiftCertificate) {
                certificateAmount.add(orderPayment.getAmount());
            }
        }
        return certificateAmount;
    }

    /**
     * Calculating the totalCharge of order
     *
     * @param order
     * @return IAmount
     */

    private IAmount calculateTotalCharge(IOrder order) {
        IAmount totalChargeAmount = new Amount(order.getTotal());
        totalChargeAmount.subtract(calculateCertificate(order));
        return totalChargeAmount;
    }

    /**
     * Calculating the totalDiscount include item discounts & order discount
     *
     * @param order
     * @return IAmount
     */

    private IAmount calculateTotalOrderDiscount(IOrder order) {
        IAmount totalDiscountAmount = new Amount();
        // Calculate item discounts
        List items = order.getItems();
        for (Iterator i = items.iterator(); i.hasNext(); ) {
            IOrderItem item = (IOrderItem) i.next();
            totalDiscountAmount.add(item.getTotalDiscount());
        }
        // Calculate order discount
        List discounts = order.getDiscounts();
        for (Iterator i = discounts.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (o instanceof IOrderDiscount) {
                IOrderDiscount discount = (IOrderDiscount) o;
                totalDiscountAmount.add(discount.getAmount());
            } else if (o instanceof IDiscount) {
                IDiscount discount = (IDiscount) o;
                totalDiscountAmount.add(discount.getAmount());
            }
        }
        return totalDiscountAmount;
    }

    /**
     * Calculating the total of shipments
     *
     * @param order
     * @return IAmount
     */

    private IAmount calculateTotalShipping(IOrder order) {
        IAmount total = new Amount();
        List<IOrderShipment> shipments = order.getShipments();
        for (IOrderShipment shipment : shipments) {
            total.add(shipment.getShippingTotal());
        }
        return total;
    }

    /**
     * Calculating the total discounts of shipments
     *
     * @param order
     * @return IAmount
     */

    private IAmount calculateTotalShippingDiscount(IOrder order) {
        IAmount discountTotal = new Amount();
        List<IOrderShipment> shipments = order.getShipments();
        for (IOrderShipment shipment : shipments) {
            List discounts = shipment.getDiscounts();
            for (Iterator i = discounts.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof IOrderDiscount) {
                    IOrderDiscount discount = (IOrderDiscount) o;
                    discountTotal.add(discount.getAmount());
                } else if (o instanceof IDiscount) {
                    IDiscount discount = (IDiscount) o;
                    discountTotal.add(discount.getAmount());
                }
            }
        }
        return discountTotal;
    }

    /**
     * Getting LazyLoad exceptions from VelocityMessagePreparator because it has no session to link to. So,
     * load all the lazy data now.
     *
     * @param order Instance of <code>IOrder</code>.
     */
    private Map loadOrderData(IOrder order) {
        // try to initialize all lazy-loaded item collections

        List items = order.getItems();
        Map<String, String> productCatCodes = new HashMap(items.size());
        StringBuffer productCodes = new StringBuffer();
        StringBuffer categoryCodes = new StringBuffer();
        for (Iterator i = items.iterator(); i.hasNext(); ) {
            IOrderItem item = (IOrderItem) i.next();
            // get fresh collection of skus
            Set links = item.getProduct().getSkus();

            List discounts = item.getDiscounts();
            for (Iterator j = discounts.iterator(); j.hasNext(); ) {
                IDiscount discount = (IDiscount) j.next();
            }

            if (item.getGiftWrapping() != null) {
                if (item.getGiftWrapping().getGiftWrap() != null) {
                    String giftWrapName = item.getGiftWrapping().getGiftWrap().getName();
                    String giftWrapDescription = item.getGiftWrapping().getGiftWrap().getDescription();

                }
            }
            // set the productCodes in a map
            if (productCodes.length() > 0) {
                productCodes.append(",");
            }
            productCodes.append(item.getProductCode());
            // set the category ID in a map
            if (categoryCodes.length() > 0) {
                categoryCodes.append(",");
            }
            if (item.getCategory() != null) {
                categoryCodes.append(item.getCategory().getCode());
            }
        }
        productCatCodes.put(IEmailQueueMessage.PRODUCT_CODES, productCodes.toString());
        productCatCodes.put(IEmailQueueMessage.CATEGORY_CODES, categoryCodes.toString());

        // try to initialize all lazy-loaded shipment collections
        List shipments = order.getShipments();
        for (Iterator j = shipments.iterator(); j.hasNext(); ) {
            ICartShipment shipment = (ICartShipment) j.next();
            for (Iterator k = shipment.getDiscounts().iterator(); k.hasNext(); ) {
                IDiscount discount = (IDiscount) k.next();
                String shippingMethod = shipment.getShippingMethod().getName();
            }
        }
        return productCatCodes;
    }

    /**
     * Parses a String with source codes associated with the order and returns them as a List to
     * provide access to source codes to the email template.
     *
     * @param sourceCodes
     * @return
     */
    private List getSourceCodesAsList(String sourceCodes) {
        List scList = new ArrayList();
        for (StringTokenizer st = new StringTokenizer(sourceCodes, "*|*"); st.hasMoreTokens(); ) {
            scList.add(st.nextToken());
        }
        return scList;
    }
    
    /**
     * For VISA Promo Codes/ Source Codes, the source codes should not be visible to user in email.
     * This method modifies the source code list so that the actual source code is replaced with a configurable message.
     * Added for PEBL-13672
     * @param sourceCodeInfoList
     * @return
     */
    private void modifySourceCodeInfoList(List sourceCodeInfoList) {
		if (null != sourceCodeInfoList && !sourceCodeInfoList.isEmpty()) {
			String visaPromoCodePrefix = configManager.getAsString(PROMO_CODE_PREFIX), visaPromoCodeMessage = configManager.getAsString(PROMO_CODE_EMAIL_MSG);
			Iterator itr = sourceCodeInfoList.iterator();
			ISourceCodeInfo sourceCodeInfo = null;
			while(itr.hasNext()) {
				sourceCodeInfo = (ISourceCodeInfo) itr.next();
				if (null != sourceCodeInfo) {
					if(StringUtils.isNotBlank(sourceCodeInfo.getCode()) && StringUtils.isNotBlank(visaPromoCodePrefix)) {
						if (sourceCodeInfo.getCode().toUpperCase().startsWith(visaPromoCodePrefix.toUpperCase())) {
							sourceCodeInfo.setCode(visaPromoCodeMessage);
						}
					}
				}
			}
		}
	}

}
