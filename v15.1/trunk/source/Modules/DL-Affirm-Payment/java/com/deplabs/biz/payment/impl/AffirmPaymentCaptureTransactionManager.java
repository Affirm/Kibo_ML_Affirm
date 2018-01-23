/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

package com.deplabs.biz.payment.impl;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.biz.email.IVelocityMessagePreparator;
import org.marketlive.biz.email.IVelocityMessagePreparatorFactory;
import org.marketlive.entity.account.IContact;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderDiscount;
import org.marketlive.entity.cart.order.IOrderHome;
import org.marketlive.entity.cart.order.IOrderItem;
import org.marketlive.entity.cart.order.IOrderItemPart;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderPaymentAmazon;
import org.marketlive.entity.cart.order.IOrderPaymentCreditCard;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.cart.order.IOrderTracking;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.messaging.IEmailQueueMessage;
import org.marketlive.messaging.IQueueWriter;
import org.marketlive.system.config.IConfigurationManager;
import org.marketlive.system.encryption.DecryptionException;
import org.marketlive.system.encryption.IDecryptor;
import org.marketlive.system.locale.IMessageResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deplabs.biz.payment.AffirmPaymentCaptureResult;
import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmApiAction;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.cart.order.ICCOMOrderFinder;
import com.marketlive.biz.email.VelocityMessagePreparator;
import com.marketlive.biz.payment.AmazonRequest;
import com.marketlive.biz.payment.AmazonResponse;
import com.marketlive.biz.payment.CreditCardRequest;
import com.marketlive.biz.payment.CreditCardResponse;
import com.marketlive.biz.payment.IAmazonPaymentTransactionManager;
import com.marketlive.biz.payment.IPaymentTransactionManager;
import com.marketlive.biz.payment.PaymentCaptureResult;
import com.marketlive.biz.payment.PaymentCaptureResult.PaymentCaptureResultCode;
import com.marketlive.biz.payment.PaymentUtil;
import com.marketlive.biz.payment.gateway.IAmazonPaymentGateway;
import com.marketlive.biz.payment.gateway.IPaymentGateway;
import com.marketlive.biz.payment.gateway.amazon.impl.AmazonPaymentConstants;
import com.marketlive.biz.payment.impl.PaymentCaptureTransactionManager;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.domain.payment.Capture;
import com.marketlive.entity.currency.Amount;
import com.marketlive.messaging.EmailQueueMessage;
import com.marketlive.messaging.StompQueueMessage;
import com.marketlive.system.annotation.PlatformService;
import com.marketlive.system.locale.ActiveLanguage;
import com.marketlive.system.site.ActiveSite;

/**
 * An extension of the {@link PaymentCaptureTransactionManager} responsible for
 * capturing amounts of order shipment for OOB & AFFIRM Payments
 * 
 * @author horacioa
 */
@Primary
@PlatformService
public class AffirmPaymentCaptureTransactionManager extends PaymentCaptureTransactionManager {

    /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(AffirmPaymentCaptureTransactionManager.class);

    private static final String MSG_ORDER_SHIPMENT_CONFIRMATION_SUBJECT = "ordershipmentconfirmation.subject";
    
    /** References to Bronto Order Shipment template name */
    private static final String ORDER_SHIPMENT_CONFIRMATION = "OrderShipmentConfirmation";
    
    /** Constant holding the configurable property to send Shipment Confirmation Email. */
    private static final String SHIPMENT_CONFIRMATION_EMAIL_SEND= "biz.payment.paymentCaptureJob.sendShipmentConfirmationEmail";
    
    /** Constant holding the configurable property key name for 'allowed Capture attempts'. */
    private static final String ALLOWED_CAPTURE_ATTEMPTS= "biz.payment.paymentCaptureJob.allowed_Capture_attempts";
    
    /** Reference to the {@link IConfigurationManager}. */
    @Autowired
	private IConfigurationManager configurationManager;
	
    /** Reference to the {@link IOrderManager}. */
    @Autowired
	private IOrderManager orderManager;
	
	/** Reference to the {@link IOrderHome}. */
    @Autowired
	private IOrderHome orderHome;
	
	/** Reference to the {@link IPaymentTransactionManager}. */
    @Autowired
    private IPaymentTransactionManager paymentTransactionManager;
    @Autowired
    private IAmazonPaymentTransactionManager amazonPaymentTransactionManager;
    
    @Autowired
    private IAffirmPaymentTransactionManager affirmPaymentTransactionManager;
    
    @Autowired
    private IAmazonPaymentGateway amazonPaymentGateway;
    
	/** Reference to the {@link IAuthorizationDAO}. */
    @Autowired
    private IAuthorizationDAO authorizationDAO;
    
    /** Reference to the {@link ICaptureDAO}. */
    @Autowired
    private ICaptureDAO captureDAO;
    
    /** Reference to the {@link IDecryptor}. */
    @Autowired
	private IDecryptor decryptor;
	
	/** Reference to the QueueWriter. */
    @Autowired
    @Qualifier("emailQueueWriter")
    private IQueueWriter emailQueueWriter;
    
    /** Reference to the QueueWriter. */
    @Autowired
    @Qualifier("jmsQueueWriter")
    private IQueueWriter jmsQueueWriter;
    
    /** Reference to the OrderShipmentMessagePreparator. */
    @Autowired
    @Qualifier("orderShipmentMessagePreparator")
    private IVelocityMessagePreparatorFactory velocityOrderShipmentMessagePreparator;
    
    /** Reference to the messageResources. */
    @Autowired
    private IMessageResources messageResources;
    
    /** Reference to ICCOMOrderFinder. */
    @Autowired
    private ICCOMOrderFinder ccomOrderFinder;
    
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public PaymentCaptureResult capturePayment(IOrderShipment orderShipment, boolean isCCOMsettlement) {
        PaymentCaptureResult paymentCaptureResult = new AffirmPaymentCaptureResult();
        CreditCardResponse creditCardResponse = new CreditCardResponse();
        AmazonResponse amazonResponse = new AmazonResponse();
        AffirmResponse affirmResponse = new AffirmResponse();
        boolean sendShipmentConfirmationEmail = false;
        
        //The Shipment Confirmation Email is always going to be sent for CCOM settlements
        //If the order is being settled by the Job, sending the email could be configurable 
        if (isCCOMsettlement){
            sendShipmentConfirmationEmail = true;
        }else{
            if(configurationManager.getAsBoolean(SHIPMENT_CONFIRMATION_EMAIL_SEND)){
                sendShipmentConfirmationEmail = true;
            }
        }
        
        // Get the Order for this OrderShipment.
        IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());

        // Check if payment was through credit card.
        if (PaymentUtil.isPaymentThroughCreditCard(order.getPayments())) {

            boolean captureTransactionRequired = false;

            // Get available authorizations for this Order.
            List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));
            
            // Check if any of the authorizations are unused.
            Authorization authorization = getUnusedAuthorization(authorizations);

            // Get amount to capture.
            IAmount amountToCharge = getAmountToCapture(orderShipment, order);
            
            if (amountToCharge != null) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToCharge.toBigDecimal());
                amountToCharge = new Amount(formattedAmount);

                if (formattedAmount != null && amountToCharge != null && amountToCharge.compareTo(new Amount()) == 1) {
                    captureTransactionRequired = true;

                    boolean isPaymentechPaymentGateway = false;
                    IPaymentGateway paymentGateway = paymentTransactionManager.getPaymentGatewayByCreditCardType(PaymentUtil.getCreditCard(order).getType());
                	String paymentechPaymentGatewayName = configurationManager.getAsString("biz.payment.paymentech.paymentGatewayName");
                    if (paymentGateway.getPaymentGatewayName().equalsIgnoreCase(paymentechPaymentGatewayName)) {
                    	// For paymentech payment gateway.
                    	logger.debug("Capture Request - Paymentech Payment Gateway.");
                    	isPaymentechPaymentGateway = true;
                    }

                    if (authorization != null) {
                        CreditCardRequest creditCardRequest = new CreditCardRequest();
                        creditCardRequest.setCurrency(order.getSite().getCurrency().getCurrencyCode());
                        creditCardRequest.setAmount(formattedAmount);
                        creditCardRequest.setMerchantReferenceCode(authorization.getPaymentTransaction().getMerchantReferenceCode());
                        creditCardRequest.setAuthorizationCode(authorization.getAuthorizationCode());
                        creditCardRequest.setRequestID(authorization.getRequestID());
                        creditCardRequest.setCustomRequestFields(authorization.getCustomFields());
                        IOrderPaymentCreditCard orderPaymentCreditCard =  PaymentUtil.getCreditCard(order);
                        creditCardRequest.setCreditCardType(orderPaymentCreditCard.getType());
                        creditCardResponse = paymentTransactionManager.capturePayment(creditCardRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()), Long.parseLong(order.getPk().getAsString()), Long.parseLong(orderShipment.getPk().getAsString()));

                        if (creditCardResponse.isSuccess()) {
                        	// Don't mark authorization as used in case of paymentech payment gateway
                        	// because paymentech supports 'split transactions' and we reuse original/first authorization in all captures.
                        	if (!isPaymentechPaymentGateway) {
	                            authorization.setAuthorizationUsed(true);
	                            authorizationDAO.update(authorization);
                        	}
                        }
                    } else {
                        CreditCardRequest creditCardRequest = new CreditCardRequest();
                        populateAuthRequest(creditCardRequest, order, order.getSite().getCurrency().getCurrencyCode());
                        creditCardRequest.setAmount(formattedAmount);
                        creditCardResponse = paymentTransactionManager.authorizeCreditCardAndCapture(creditCardRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()), Long.parseLong(order.getPk().getAsString()), Long.parseLong(orderShipment.getPk().getAsString()));
                        if (creditCardResponse.isSuccess()) {
                            String requestId = creditCardResponse.getRequestID();
                            authorization = authorizationDAO.findCreditCardAuthorizationByRequestId(requestId);
                            authorization.setOrderId(new Long(order.getPk().getAsString()));
                            authorization.setAuthorizationUsed(true);
                            authorizationDAO.update(authorization);
                        }
                    }
                }
            }
            
            // There was a credit card payment amount to be captured in this method execution.
            if (captureTransactionRequired) {
                if (creditCardResponse != null && creditCardResponse.isSuccess()) {
                    updateOrderShipmentStatus(order, orderShipment,ORDER_STATUS_COMPLETE );
                    if (sendShipmentConfirmationEmail){
                        sendOrderShipmentEmail(order, orderShipment);
                    }
                }
            } else { // There was no credit card payment amount to be captured in this method execution.
                updateOrderShipmentStatus(order, orderShipment, ORDER_STATUS_COMPLETE );
                if (sendShipmentConfirmationEmail){
                    sendOrderShipmentEmail(order, orderShipment);
                }
                // IMPORTANT: Although there is no hit to payment gateway in this case where there is no amount left to capture,
                // but setting the success flag in Credit Card Response to true for callers/users to display success message.
                creditCardResponse.setSuccess(true);
                
                logger.debug("A capture transaction was not processed for order with id: " + 
                        order.getPk().getAsString() + " and order CODE: " + order.getCode() + 
                        " because the given capture amount was null or zero.");
            }
	        if (creditCardResponse.isSuccess()) {
	            paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);
	        } else {
	            paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.FAILURE);
	        }
	        paymentCaptureResult.setResultMessage(creditCardResponse.getResponseMessage());
	        paymentCaptureResult.setCreditCardResponse(creditCardResponse);
        } else if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())) {
            boolean captureTransactionRequired = false;

            // Get available authorizations for this Order.
            List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // Check if any of the authorizations are unused.
            Authorization authorization = getUnusedAuthorization(authorizations);

            // Get amount to capture.
            IAmount amountToCharge = getAmountToCapture(orderShipment, order);

            if (amountToCharge != null) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToCharge.toBigDecimal());
                amountToCharge = new Amount(formattedAmount);

                if (formattedAmount != null && amountToCharge != null && amountToCharge.compareTo(new Amount()) == 1) {
                    captureTransactionRequired = true;

                    if (authorization != null) {
                        AmazonRequest amazonRequest = new AmazonRequest();
                        amazonRequest.setCurrency(order.getSite().getCurrency().getCurrencyCode());
                        amazonRequest.setAmount(formattedAmount);
                        amazonRequest.setSellerId(configurationManager.getAsString("biz.payment.amazonpayment.sellerId"));
                        String randomNumber = String.valueOf(Math.random()).substring(0,9);
                        randomNumber = randomNumber.replace('.', '0');
                        amazonRequest.setCaptureRefId(order.getPk().getAsString().concat(randomNumber));
                        String authorizationID = authorization.getCustomFields() != null ? authorization.getCustomFields().get(AmazonPaymentConstants.AMAZON_AUTHORIZATION_ID) : "";

                        amazonRequest.setAmazonAuthId(authorizationID);

                        amazonResponse = amazonPaymentTransactionManager.capturePayment(amazonRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()), Long.parseLong(order.getPk().getAsString()), Long.parseLong(orderShipment.getPk().getAsString()));

                        if (amazonResponse.getResponseCode()!= null && (amazonResponse.getResponseCode().equals("Completed") || amazonResponse.getResponseCode().equals("Pending"))) {
                            authorization.setAuthorizationUsed(true);
                            authorizationDAO.update(authorization);
                        }
                    }
                }
            }

            // There was a amazon payment amount to be captured in this method execution.
            if (captureTransactionRequired) {
                if (amazonResponse.getResponseCode()!= null && (amazonResponse.getResponseCode().equals("Completed") || amazonResponse.getResponseCode().equals("Pending"))) {
                    updateOrderShipmentStatus(order, orderShipment,ORDER_STATUS_COMPLETE );

                    IOrderPaymentAmazon orderPayment = PaymentUtil.getAmazonPayment(order.getPayments());

                    //Closing AmazonOrderReferenceID in Amazon after a successful capture
                    if(order.getStatus().equalsIgnoreCase(ORDER_STATUS_COMPLETE)) {
                        AmazonRequest amazonRequestForClose= new AmazonRequest();
                        AmazonResponse amazonResponseForClose = new AmazonResponse();
                        amazonRequestForClose.setSellerId(configurationManager.getAsString("biz.payment.amazonpayment.sellerId"));
                        amazonRequestForClose.setAmazonOrderRefId(orderPayment.getOrderReferenceId());
                        amazonResponseForClose = amazonPaymentGateway.closeOrderReference(amazonRequestForClose);
                        if (amazonResponseForClose.isSuccess()) {
                            logger.info("AmazonOrderReferenceID: "+ orderPayment.getOrderReferenceId() + " is closed with Amazon after capture");
                        } else {
                            logger.info("Problem closing AmazonOrderReferenceID: "+ orderPayment.getOrderReferenceId() + " with Amazon after capture");
                        }
                    }

                    if (sendShipmentConfirmationEmail){
                        sendOrderShipmentEmail(order, orderShipment);
                    }

                    paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);
                    paymentCaptureResult.setAmazonResponse(amazonResponse);

                }else{
                    paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.FAILURE);
                    paymentCaptureResult.setAmazonResponse(amazonResponse);
                }
            } else { // There was no credit card payment amount to be captured in this method execution.
                updateOrderShipmentStatus(order, orderShipment, ORDER_STATUS_COMPLETE );
                if (sendShipmentConfirmationEmail){
                    sendOrderShipmentEmail(order, orderShipment);
                }
                // IMPORTANT: Although there is no hit to payment gateway in this case where there is no amount left to capture,
                // but setting the success flag in Amazon Response to true for callers/users to display success message.
                amazonResponse.setSuccess(true);
                paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);
                paymentCaptureResult.setAmazonResponse(amazonResponse);

                logger.debug("A capture transaction was not processed for order with id: " +
                        order.getPk().getAsString() + " and order CODE: " + order.getCode() +
                        " because the given capture amount was null or zero.");
            }
        } else if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) { // Check if payment is through AFFIRM
        	boolean captureTransactionRequired = false;

            // Get available authorizations for this Order.
            List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // Check if any of the authorizations are unused.
            Authorization authorization = getUnusedAuthorization(authorizations);

            // Get amount to capture.
            IAmount amountToCharge = getAmountToCapture(orderShipment, order);

            if (amountToCharge != null) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToCharge.toBigDecimal());
                amountToCharge = new Amount(formattedAmount);

                if (formattedAmount != null && amountToCharge != null && amountToCharge.compareTo(new Amount()) == 1) {
                    captureTransactionRequired = true;

                    if (authorization != null) {
                        AffirmRequest affirmRequest = new AffirmRequest();
                     	// for capture AFFIRM needs: {"order_id": "JKLM4321", "shipping_carrier": "USPS", "shipping_confirmation": "1Z23223"}
                        // and CHARGE_ID for the ServiceURL
                        affirmRequest.setRequestType(AffirmApiAction.CAPTURE);
                        //affirmRequest.setOrderId(authorization.getRequestID()); // OrderId = BasketId in KIBO, but we don't have it now --> TODO: use AUTH CUSTOM FIELDS for this
                        affirmRequest.setChargeId(authorization.getRequestID());  // This is the charge ID returned by AFFIRM on the auth action
                        affirmRequest.setShippingCarrier(getShippingCarrier(orderShipment));
                        affirmRequest.setShippingConfirmation(orderShipment.getPk().getAsString());

                        affirmResponse = affirmPaymentTransactionManager.capturePayment(affirmRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()), Long.parseLong(order.getPk().getAsString()), Long.parseLong(orderShipment.getPk().getAsString()));
                        
                        if (affirmResponse.isSuccess()) {
                            authorization.setAuthorizationUsed(true);
                            authorizationDAO.update(authorization);
                        }
                    }
                }
            }

            // There was a affirm payment amount to be captured in this method execution.
            if (captureTransactionRequired) {
                if (affirmResponse.isSuccess()) {
                    updateOrderShipmentStatus(order, orderShipment,ORDER_STATUS_COMPLETE );

                    if (sendShipmentConfirmationEmail){
                        sendOrderShipmentEmail(order, orderShipment);
                    }

                    paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);
                    ((AffirmPaymentCaptureResult)paymentCaptureResult).setAffirmResponse(affirmResponse);

                }else{
                    paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.FAILURE);
                    ((AffirmPaymentCaptureResult)paymentCaptureResult).setAffirmResponse(affirmResponse);
                }
            } else { // There was no affirm payment amount to be captured in this method execution.
                updateOrderShipmentStatus(order, orderShipment, ORDER_STATUS_COMPLETE );
                if (sendShipmentConfirmationEmail){
                    sendOrderShipmentEmail(order, orderShipment);
                }
                // IMPORTANT: Although there is no hit to payment gateway in this case where there is no amount left to capture,
                // but setting the success flag in Amazon Response to true for callers/users to display success message.
                amazonResponse.setSuccess(true);
                paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);
                ((AffirmPaymentCaptureResult)paymentCaptureResult).setAffirmResponse(affirmResponse);

                logger.debug("A capture transaction was not processed for order with id: " +
                        order.getPk().getAsString() + " and order CODE: " + order.getCode() +
                        " because the given capture amount was null or zero.");
            }
        } else { // Payment is through Gift Certificate.
        	updateOrderShipmentStatus(order, orderShipment,ORDER_STATUS_COMPLETE);
        	if (sendShipmentConfirmationEmail){
                sendOrderShipmentEmail(order, orderShipment);
            }
        	// IMPORTANT: Although there is no Cybersource hit in case of Gift Certificate, but setting
			// the success flag in Credit Card Response to true for callers/users to display success message.
			creditCardResponse.setSuccess(true);
            paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.SUCCESS);

            paymentCaptureResult.setResultMessage(creditCardResponse.getResponseMessage());
            paymentCaptureResult.setCreditCardResponse(creditCardResponse);
        }
        
        paymentCaptureResult.setOrderShipmentId(orderShipment.getPk().getAsString());
        
        // Identify shipment within order that needs to be updated so that save order works 
        List<IOrderShipment> orderShipments = order.getShipments();
        IOrderShipment shipmentWithinOrder = null;

        for (IOrderShipment orderShipment1 : orderShipments) {
            if (orderShipment1.getPk().equals(orderShipment.getPk())) {
                shipmentWithinOrder = orderShipment1;
            }
        }        
        
        // Updating the captureAttempts
        int attemptsMade = shipmentWithinOrder.getCaptureAttempts();
        shipmentWithinOrder.setCaptureAttempts(attemptsMade + 1);

        logger.debug("Attempts made to capture order shipment with id: " + 
            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + " has been set to: " + attemptsMade+1);
        
        // Check if payment is through AFFIRM
        if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) { 
        	 if (((AffirmPaymentCaptureResult)paymentCaptureResult).getAffirmResponse() == null) { // If the capture request seems to have failed.
                 String msg = "Capture request failed for order shipment with id: " +
                         orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();
                 logger.error(msg);
                 processCaptureFailure(order, orderShipment, msg, attemptsMade+1);
             } else if (!((AffirmPaymentCaptureResult)paymentCaptureResult).getAffirmResponse().isSuccess()) {
                 String msg = "Capture request failed for order shipment with id: " +
                         orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                         " with error message: " + ((AffirmPaymentCaptureResult)paymentCaptureResult).getAffirmResponse().getResponseMessage();
                 logger.error(msg);
                 processCaptureFailure(order,orderShipment, msg, attemptsMade+1);
             } else {
                 logger.debug("Capture request succeeded for order shipment with id: " +
                         orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
             }
        } else if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())){
            if (paymentCaptureResult.getAmazonResponse() == null) { // If the capture request seems to have failed.
                String msg= "Capture request failed for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();
                logger.error(msg);
                processCaptureFailure(order, orderShipment, msg, attemptsMade+1);
            } else if (!paymentCaptureResult.getAmazonResponse().isSuccess()) {
                String msg= "Capture request failed for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                        " with error message: " + paymentCaptureResult.getAmazonResponse().getResponseMessage();
                logger.error(msg);
                processCaptureFailure(order,orderShipment, msg, attemptsMade+1);
            } else {
                logger.debug("Capture request succeeded for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
            }
        } else {
            if (paymentCaptureResult.getCreditCardResponse() == null) { // If the capture request seems to have failed.
                String msg = "Capture request failed for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();
                logger.error(msg);
                processCaptureFailure(order, orderShipment, msg, attemptsMade + 1);
            } else if (!paymentCaptureResult.getCreditCardResponse().isSuccess()) {
                String msg = "Capture request failed for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                        " with error message: " + paymentCaptureResult.getCreditCardResponse().getResponseMessage();
                logger.error(msg);
                processCaptureFailure(order, orderShipment, msg, attemptsMade + 1);
            } else {
                logger.debug("Capture request succeeded for order shipment with id: " +
                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
            }
        }


            // save the success or failure (only attempts made)
        orderHome.update(order);
     
        return paymentCaptureResult;
    }
    
    /**
     * This Method returns the Shipping Carrier data for this shipment
     * @param orderShipment
     * @return the Shipping Carrier of this shipment
     */
    private String getShippingCarrier(IOrderShipment orderShipment) {
    	if (CollectionUtils.isNotEmpty(orderShipment.getTrackings())) {
	    	for (Iterator it = orderShipment.getTrackings().iterator(); it.hasNext();) {
				IOrderTracking orderTracking = (IOrderTracking) it.next();
				if (StringUtils.isNotBlank(orderTracking.getCarrierName())) {
					return orderTracking.getCarrierName();
				}
				
			}
    	}
    	
    	return null;
	}

	/**
     * This method returns the unused (means amount is not captured against it) {@link Authorization}.
     * If unused {@link Authorization} is not found then returns null.
     * 
     * @param authorizations the list of {@link Authorization}(s)
     * @return the unused {@link Authorization} or null
     */
    private Authorization getUnusedAuthorization(List<Authorization> authorizations) {
        if (authorizations != null) {
            for (Authorization authorization : authorizations) {
                if (!authorization.isAuthorizationUsed()) {
                    return authorization;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Calculates and returns the amount that needs to be capture.
     * 
     * @param orderShipment the {@link IOrderShipment} object
     * @param order the {@link IOrder} object
     * @return the amount to capture
     */
    private IAmount getAmountToCapture(IOrderShipment orderShipment, IOrder order) {
        IAmount amountToCapture = null;
        // Total amount that was paid and needs to be captured.
        IAmount orderPaymentAmount = null;
        IAmount amountRemainingToCapture = null;

        boolean creditCardPayment = PaymentUtil.isPaymentThroughCreditCard(order.getPayments());
        boolean amazonPayment = PaymentUtil.isPaymentThroughAmazon((order.getPayments()));
        boolean affirmPayment = AffirmPaymentUtil.isPaymentThroughAffirm((order.getPayments()));

        if (creditCardPayment || amazonPayment || affirmPayment) {
            IOrderPayment orderPayment = null;

            if (amazonPayment) {
            	orderPayment = PaymentUtil.getAmazonPayment((order.getPayments()));
            } else if (creditCardPayment) {
            	orderPayment = PaymentUtil.getCreditCard(order.getPayments());
            } else if (affirmPayment) {
            	orderPayment = AffirmPaymentUtil.getAffirmPayment(order.getPayments());
            }

            orderPaymentAmount = new Amount(orderPayment.getAmount());
            amountRemainingToCapture = new Amount(orderPayment.getAmount());
        }
        
        // Total amount that has already been captured for this Order.
        IAmount totalAmountCaptured = new Amount();
        
        List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
        // Add up the amounts that have already been captured for this Order.
        if (captures != null && captures.size() > 0) {
            for (Capture capture : captures) {
                totalAmountCaptured.add(new Amount(capture.getAmountCaptured()));
            }
        }
        
        // If the amount that has already been captured for this Order is less than the payment amount
        // that needs to be captured, then calculate the amount that is left to be
        // captured.
        // If otherwise, then there is no amount left to capture.
        if (totalAmountCaptured.compareTo(orderPaymentAmount) == -1) {

            // First calculate the amount left to capture (total payment amount - amount already captured).
            amountRemainingToCapture.subtract(totalAmountCaptured);

            // If the amount left to capture is less than or equal to this shipment's total,
            // then capture the entire amount that is remaining to be captured.
            if (amountRemainingToCapture.compareTo(orderShipment.getTotal()) < 1) {
                amountToCapture = amountRemainingToCapture;
            }
            // If the amount left to capture is greater than this shipment's total,
            // then only capture the amount of this shipment's total.
            else {
                amountToCapture = new Amount(orderShipment.getTotal());

                IAmount additionalAddressTotal = order.getAdditionalAddressTotal();
                if (additionalAddressTotal != null && additionalAddressTotal.compareTo(new Amount()) == 1) {
                    IAmount amountRemainingAfterThisCapture = new Amount(amountRemainingToCapture);
                    amountRemainingAfterThisCapture.subtract(orderShipment.getTotal());
                    if (amountRemainingAfterThisCapture.compareTo(additionalAddressTotal) < 1) {
                        amountToCapture.add(amountRemainingAfterThisCapture);
                    }
                }
            }
        }

        return amountToCapture;
    }
    
    private void populateAuthRequest(CreditCardRequest creditCardRequest, IOrder order, String currency) {
        // Set billing and address fields.
        IContact billContact = order.getCustomer().getPrimaryContact();
        creditCardRequest.setCompany(billContact.getCompany());
        creditCardRequest.setEmail(billContact.getEmail());
        creditCardRequest.setFirstName(billContact.getPerson().getFirstName());
        creditCardRequest.setLastName(billContact.getPerson().getLastName());
        creditCardRequest.setMiddleName(billContact.getPerson().getMiddleName());
        creditCardRequest.setTitle(billContact.getPerson().getTitle());
        creditCardRequest.setPhone1(billContact.getPhone1());
        creditCardRequest.setPhone2(billContact.getPhone2());
        
        creditCardRequest.setApartmentNumber(billContact.getAddress().getApartmentNumber());
        creditCardRequest.setStreet1(billContact.getAddress().getStreet1());
        creditCardRequest.setStreet2(billContact.getAddress().getStreet2());
        creditCardRequest.setStreet3(billContact.getAddress().getStreet3());
        creditCardRequest.setPOBox(billContact.getAddress().isPostOfficeBox());
        creditCardRequest.setCity(billContact.getAddress().getCity());
        creditCardRequest.setStateCode(billContact.getAddress().getState().getStateCode());
        creditCardRequest.setCountryCode(billContact.getAddress().getCountry().getCode());
        creditCardRequest.setPostalCode(billContact.getAddress().getPostalCode());
        
        // Set credit card fields.
        IOrderPaymentCreditCard orderPaymentCreditCard =  PaymentUtil.getCreditCard(order);
        String decryptedCreditCardNumber = decryptCreditCardNumber(orderPaymentCreditCard.getNumber());
        creditCardRequest.setCreditCardNumber(decryptedCreditCardNumber);
        creditCardRequest.setCreditCardType(orderPaymentCreditCard.getType());
        creditCardRequest.setCreditCardExpirationMonth(orderPaymentCreditCard.getExpMonth());
        creditCardRequest.setCreditCardExpirationYear(orderPaymentCreditCard.getExpYear());
        
        creditCardRequest.setCurrency(currency);
        creditCardRequest.setMerchantReferenceCode(order.getPk().getAsString());
    }
    
    private String decryptCreditCardNumber(String creditCardNumber) {
        String decryptedCreditCardNumber = null;
        
        try {
            decryptedCreditCardNumber = decryptor.decrypt(creditCardNumber);
        } catch (DecryptionException e) {
            throw new RuntimeException("Could not decrypt credit card number.", e);
        }
        
        return decryptedCreditCardNumber;
    }
    @SuppressWarnings("unchecked")
    private void processCaptureFailure(IOrder order, IOrderShipment orderShipment, String failureMsg, int attemptsMade){
        
        if (attemptsMade >= configurationManager.getAsInt(ALLOWED_CAPTURE_ATTEMPTS)){
            updateOrderShipmentStatus(order, orderShipment, ORDER_STATUS_ERROR_HOLD );
            logger.error("Exceeded the number of attempts made to capture order shipment with id: " + 
                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + " and number of attempts: " +
                    attemptsMade + " Updating the order shipment status to " + ORDER_STATUS_ERROR_HOLD);
                
            if (ActiveSite.getSite()== null){
                ActiveSite.setSite( order.getSite());
            }else if (!order.getSite().getCode().equals(ActiveSite.getSite().getCode())){
                ActiveSite.setSite(order.getSite());
            }
            sendNotificationToJMSQueue(failureMsg);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void updateOrderShipmentStatus(IOrder order, IOrderShipment orderShipment, String status) {

        // Set status of the Order if all of its OrderShipments have the updated status
        List<IOrderShipment> orderShipments = order.getShipments();
        IOrderShipment shipmentWithinOrder = null;
        for (IOrderShipment orderShipment1 : orderShipments) {
            // Identify the shipment within order that should be updated for cascaded changes
            if (orderShipment1.getPk().equals(orderShipment.getPk())) {
                shipmentWithinOrder = orderShipment1;
            }
        }
        
        boolean flag = true;
        for (IOrderShipment orderShipment1 : orderShipments) {
            // If there is another order shipment and not with same status, complete order can not have same status
			if (!orderShipment1.getStatus().equalsIgnoreCase(status) &&
                    !orderShipment1.getStatus().equalsIgnoreCase(ORDER_STATUS_CANCELED) &&
                    !orderShipment1.getPk().getAsString().equalsIgnoreCase(orderShipment.getPk().getAsString())) {
                flag = false;
                break;
            }
        }
        if (flag) {
            order.setStatus(status);
        }       
        
        // Set status of the OrderShipment
        shipmentWithinOrder.setStatus(status);
        if (status.equalsIgnoreCase(ORDER_STATUS_COMPLETE)){
            shipmentWithinOrder.setDateShipped(new Date());
        }
        
        // Set status of OrderItems of this OrderShipment
        List<IOrderItem> orderItems = shipmentWithinOrder.getItems();
		for (IOrderItem orderItem : orderItems) {
            if (!orderItem.getStatus().equalsIgnoreCase(ORDER_STATUS_CANCELED)) {
                orderItem.setStatus(status);
                List<IOrderItemPart> orderItemParts = orderItem.getKitParts();
                if (orderItem.isKit() && orderItemParts.size() > 0){
                    for (IOrderItemPart orderItemPart : orderItemParts) {
                        orderItemPart.setStatus(status);
                    }
                }
            }
        }
        
        orderHome.update(order);
    }
    
       
    /**
     * This method sends Order shipment email. If some exception exception occurs while sending
     * an email then catches that exception and continue with further processing.
     *
     * @param order
     * @param orderShipment
     */
    private void sendOrderShipmentEmail(IOrder order, IOrderShipment orderShipment) {
        try {
            if (ActiveSite.getSite()== null){
                ActiveSite.setSite( order.getSite());
            }else if (!order.getSite().getCode().equals(ActiveSite.getSite().getCode())){
                ActiveSite.setSite(order.getSite());
            }
            IVelocityMessagePreparator messagePreparator = velocityOrderShipmentMessagePreparator.getVelocityMessagePreparator();
            Map<String, Object> orderShipmentEmailDataMap = new HashMap<String, Object>();
            String companyName = messageResources.getMessage(
                    ActiveSite.getSite(), ActiveLanguage.getUserLocale(), IMessageResources.LOCALIZED_COMPANY_NAME_KEY);
            String customerServicePhone = messageResources.getMessage(
                    ActiveSite.getSite(), ActiveLanguage.getUserLocale(), IMessageResources.LOCALIZED_GLOBAL_CUSTOMER_SERVICE_PHONE_KEY);
            String customerServiceEmailAddress = messageResources.getMessage(
                    ActiveSite.getSite(), ActiveLanguage.getUserLocale(), IMessageResources.LOCALIZED_GLOBAL_CUSTOMER_SERVICE_EMAIL_KEY);
            String subject = messageResources.getMessage(ActiveSite.getSite(), ActiveLanguage.getUserLocale(),MSG_ORDER_SHIPMENT_CONFIRMATION_SUBJECT);
            
            // Populate Order, Shipment and Item level discount
            populateDiscount(order);
            
            //If this is CCOM order, then load CCOMOrder information
            IAmount ccomOrderOverrideAmount = new Amount();
            if (order.getChannel()!= null && order.getChannel().equalsIgnoreCase(CCOM_ORDER_CHANNEL)){
                ccomOrderOverrideAmount = ccomOrderFinder.findCCOMOrderOverrideAmountByOrderId(Integer.parseInt(order.getPk().getAsString()));
            }
            
            orderShipmentEmailDataMap.put("order", order);
            orderShipmentEmailDataMap.put("ccomOrderOverrideAmount", ccomOrderOverrideAmount);
            orderShipmentEmailDataMap.put("shipmentConfirmationNumber", orderShipment.getCode());
            orderShipmentEmailDataMap.put("configManager", configurationManager);
            orderShipmentEmailDataMap.put("locale", ActiveLanguage.getUserLocale());
            orderShipmentEmailDataMap.put("customerServicePhone", customerServicePhone);
            orderShipmentEmailDataMap.put("customerServiceEmailAddress", customerServiceEmailAddress);
            orderShipmentEmailDataMap.put("companyName", companyName);
            
            messagePreparator.setData(orderShipmentEmailDataMap);
            messagePreparator.setTo(order.getBillToInfo().getEmail());
            messagePreparator.setLocale(ActiveLanguage.getUserLocale());
            messagePreparator.setSubject(subject);
            
            if (!(messagePreparator instanceof VelocityMessagePreparator)) {
                logger.error("Invalid MessagePreparator, could not send Order Shipment email.");
                return;
            }
            
            VelocityMessagePreparator velocityMessagePreparator = (VelocityMessagePreparator) messagePreparator;
            String orderHTML = velocityMessagePreparator.createContent(velocityMessagePreparator.getHtmlTemplate());
            String orderText = velocityMessagePreparator.createContent(velocityMessagePreparator.getTextTemplate());
            
            // Put the message data in a HashMap for storage in the queue.
            Map<String,String> messageData = new HashMap<String,String>();
            messageData.put("order", orderHTML);
    	    messageData.put("ordertext", orderText);
            messageData.put("shipmentConfirmationNumber", orderShipment.getCode());
            messageData.put("company", companyName);
            messageData.put("csphone", customerServicePhone);
            messageData.put("csemail", customerServiceEmailAddress);
            messageData.put(IEmailQueueMessage.TIME_STAMP, Long.toString(System.currentTimeMillis()));
            
            String recipientEmail = order.getBillToInfo().getEmail();
           // IContact contact = order.getCustomer().getPrimaryContact();
            
            // Put the Order Shipment email in queue.
            IEmailQueueMessage qmessage = new EmailQueueMessage();
            qmessage.queueGeneric(recipientEmail, messageData, ORDER_SHIPMENT_CONFIRMATION);
            emailQueueWriter.write(qmessage);
        } catch (Exception ex) {
            logger.error("Failed to send email during credit card charge for Order : " + order.getPk().getAsString() + " and Order Shipment : " + orderShipment.getPk().getAsString());
            ex.printStackTrace();
        }
    }
    
    /**
     * Load Order, Shipment and Item level discounts 
     * to avoid LazyInitializationException in velocity templates. 
     * 
     * @param order
     */
    @SuppressWarnings("unchecked")
    private void populateDiscount(IOrder order) {
        List<IOrderDiscount> orderLevelDiscount = new ArrayList<IOrderDiscount>();
        List<IOrderDiscount> shipmentLevelDiscount = null;
        List<IOrderDiscount>itemLevelDiscount = null;
        
        orderLevelDiscount = orderManager.findOrderDiscounts(order.getPk());
        if(orderLevelDiscount !=null && !orderLevelDiscount.isEmpty()){
            order.setDiscounts(orderLevelDiscount);
        }
        
        List<IOrderShipment> orderShipmentList = order.getShipments();
        for (IOrderShipment orderShipment : orderShipmentList) {
            shipmentLevelDiscount = orderManager.findOrderShipmentDiscounts(orderShipment.getPk());
            if(shipmentLevelDiscount !=null && !shipmentLevelDiscount.isEmpty()){
                orderShipment.setDiscounts(shipmentLevelDiscount);
            }
            
            List<IOrderItem> orderItemList = orderShipment.getItems();
            for (IOrderItem orderItem : orderItemList) {
                itemLevelDiscount = orderManager.findOrderItemDiscounts(orderItem.getPk());
                if(itemLevelDiscount !=null && !itemLevelDiscount.isEmpty()){
                    orderItem.setDiscounts(itemLevelDiscount);
                }
            }
        }
    }
    
    private void sendNotificationToJMSQueue(String msg){
        sendNotificationToJMSQueue(msg, null);
    }
    
    private void sendNotificationToJMSQueue(String msg, Exception ex){
        Map additionalMessageData = new HashMap();
        StompQueueMessage stompMessage = new StompQueueMessage ();      

        additionalMessageData.put("Process_ID", "PaymentCaptureJob");
        additionalMessageData.put("Site_Code", ActiveSite.getSite().getCode());
        additionalMessageData.put("Environment", System.getProperty("MARKETLIVE_ENVIRONMENT")); 
        additionalMessageData.put("Severity", "2");
        
        stompMessage.addMessage(msg,additionalMessageData,ex);          
        try{
            jmsQueueWriter.write(stompMessage);

        }catch(Exception exception){
            logger.error("failed to send message to JMS Queue");
            ex.printStackTrace();
        }
    }
}
