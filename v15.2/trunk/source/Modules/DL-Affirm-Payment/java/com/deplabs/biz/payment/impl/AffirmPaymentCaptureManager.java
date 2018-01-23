package com.deplabs.biz.payment.impl;

/*
(C) Copyright MarketLive. 2012. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderHome;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderPaymentAmazon;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.cart.order.IOrderTracking;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.system.config.IConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deplabs.biz.payment.AffirmPaymentCaptureResult;
import com.deplabs.biz.payment.IAffirmPaymentCaptureManager;
import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmApiAction;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
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
import com.marketlive.biz.payment.impl.PaymentCaptureManager;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.domain.payment.Capture;
import com.marketlive.entity.currency.Amount;
import com.marketlive.system.annotation.PlatformService;

/**
 * An extension of {@link AffirmPaymentCaptureManager} responsible for capturing
 * amounts of order shipments including the OOB & the AFFIRM payments
 * 
 * @author horacioa
 * 
 */
@Primary
@PlatformService
public class AffirmPaymentCaptureManager extends PaymentCaptureManager implements IAffirmPaymentCaptureManager{

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(AffirmPaymentCaptureManager.class);
	
	private static final String STR_EMPTY = "";

	/** Constant holding the configurable property key name for 'order shipment status'. */
	private static final String ORDER_SHIPMENT_STATUS_CAPTURE_ALLOWED= "biz.payment.paymentCaptureJob.order_shipment_status";
	
	/** Constant holding the configurable property to send Shipment Confirmation Email. */
	private static final String SHIPMENT_CONFIRMATION_EMAIL_SEND= "biz.payment.paymentCaptureJob.sendShipmentConfirmationEmail";

	private static final String PAYMENTECH_PAYMENT_GATEWAY_NAME = "biz.payment.paymentech.paymentGatewayName";

	private static final String BIZ_PAYMENT_AMAZONPAYMENT_SELLER_ID = "biz.payment.amazonpayment.sellerId";

	/** Constant for CCOM orderChannel. */
	public static final String CCOM_ORDER_CHANNEL= "CCOM";

	/** Constant holding the configurable order status when the capture is performed in real time */ 
	private static final String REAL_TIME_AFFIRM_CAPTURE_ORDER_STATUS = "custom.affirmpayment_real_time_capture_order_status"; // Confirmed
	
    /** Reference to the {@link IConfigurationManager}. */
    @Autowired
	private IConfigurationManager configurationManager;
	
    /** Reference to the {@link IOrderManager}. */
    @Autowired
	private IOrderManager orderManager;
	
    /** Reference to the {@link IPaymentCaptureTransactionManager}. */
    @Autowired
    private IPaymentTransactionManager paymentTransactionManager;

    /** Reference to the {@link IOrderHome}. */
    @Autowired
    private IOrderHome orderHome;

    @Autowired
    private IAmazonPaymentTransactionManager amazonPaymentTransactionManager;

    @Autowired
    private IAmazonPaymentGateway amazonPaymentGateway;

    /** Reference to the {@link IAuthorizationDAO}. */
    @Autowired
    private IAuthorizationDAO authorizationDAO;

    /** Reference to the {@link ICaptureDAO}. */
    @Autowired
    private ICaptureDAO captureDAO;
    
    @Autowired
	private IAffirmPaymentTransactionManager affirmPaymentTransactionManager;
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public PaymentCaptureResult capturePayment(IOrderShipment orderShipment, boolean isCCOMsettlement, boolean isJobProcessing, boolean isRealTimeCapture, String affirmRequestId) {
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

        // reset the shipment confirmation flag if its a ppos order.
        boolean pPosOrder = orderManager.isPPosOrder(order);
        if (pPosOrder) {
            sendShipmentConfirmationEmail = false;
        }
        // Check if payment was through credit card or card present.
        if (PaymentUtil.isPaymentThroughCreditCardOrCardPresent(order.getPayments())) {

            boolean captureTransactionRequired = false;

            // Get available authorizations for this Order or Order_Shipment in case of PPOS.
            List<Authorization> authorizations = pPosOrder ? authorizationDAO.findAuthorizationsByOrderShipmentID(new Long(orderShipment.getPk().getAsString()))
                    : authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // check if this is a PPOS OrderShipment paid by cash
            boolean shipmentPaidByCash = false;
            if (pPosOrder && authorizations== null && PaymentUtil.isPaymentThroughCash(order.getPayments())
            && orderShipment.getAuthRequestIDs().isEmpty()){
               shipmentPaidByCash = true;
            }
            // Check if any of the authorizations are unused.
            Authorization authorization = getUnusedAuthorization(authorizations);

            // Get amount to capture.
            IAmount amountToCharge = new Amount();
            if (pPosOrder && !shipmentPaidByCash) {
                // PPOS orders have authorizations by shipment, so no need to calculate the capture to amount
                amountToCharge = new Amount (authorization.getAmountAuthorized());
            } else if (!pPosOrder){
                amountToCharge = getAmountToCapture(orderShipment, order);
            }
            
            if (amountToCharge != null && !shipmentPaidByCash) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToCharge.toBigDecimal());
                amountToCharge = new Amount(formattedAmount);

                if (formattedAmount != null && amountToCharge != null && amountToCharge.compareTo(new Amount()) == 1) {
                    captureTransactionRequired = true;

                    boolean isPaymentechPaymentGateway = false;
                    // Get type of card used for payment i.e. VI,MC,DS etc.
                    String type = getCardType(order);
                    IPaymentGateway paymentGateway = paymentTransactionManager.getPaymentGatewayByCreditCardType(type);
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
                        creditCardRequest.setCreditCardType(type);
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
                        // If authorization not available and payment through credit card then authorize and capture
                        if (PaymentUtil.isPaymentThroughCreditCard(order)) {
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
            }

            // There was a credit card payment amount to be captured in this method execution.
            if (captureTransactionRequired && !shipmentPaidByCash) {
                if (creditCardResponse != null && creditCardResponse.isSuccess()) {
                    updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName() );
                    if (sendShipmentConfirmationEmail){
                        sendOrderShipmentEmail(order, orderShipment);
                    }
                }
            } else { // There was no credit card payment amount to be captured in this method execution.
                updateOrderShipmentStatus(order, orderShipment, IOrderManager.OrderStatus.COMPLETE.getName() );
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
                    updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName() );

                    IOrderPaymentAmazon orderPayment = PaymentUtil.getAmazonPayment(order.getPayments());

                    //Closing AmazonOrderReferenceID in Amazon after a successful capture
                    if(order.getStatus().equalsIgnoreCase(IOrderManager.OrderStatus.COMPLETE.getName())) {
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
                updateOrderShipmentStatus(order, orderShipment, IOrderManager.OrderStatus.COMPLETE.getName() );
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

			// for affirm payment we do a search by request Id to get the auth
			Authorization authorization = authorizationDAO.findCreditCardAuthorizationByRequestId(affirmRequestId);

			// Check if the authorizations is unused.
			if (authorization.isAuthorizationUsed()) {
				authorization = null;
			}

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
						}
						// update the orderId that was not updated on PostFinalize
						Long orderId = new Long(order.getPk().getAsString());
						authorization.setOrderId(orderId);
						authorizationDAO.update(authorization);
					}
				}
			}

			String newOrderStatus = IOrderManager.OrderStatus.COMPLETE.getName(); // default status
			if (isRealTimeCapture) {
				String configuredStatus = this.configurationManager.getAsString(REAL_TIME_AFFIRM_CAPTURE_ORDER_STATUS);
				if (StringUtils.isNotBlank(configuredStatus)) { // check for configured status
					newOrderStatus = configuredStatus;
				}
			}

			// There was a affirm payment amount to be captured in this method execution.
			if (captureTransactionRequired) {
				if (affirmResponse.isSuccess()) {
					updateOrderShipmentStatus(order, orderShipment,newOrderStatus );

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
				updateOrderShipmentStatus(order, orderShipment, newOrderStatus );
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
		
        }else { // Payment is through Gift Certificate/Cash.
        	updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName());
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
        
        // Updating the captureAttempts only for capture job
        if (isJobProcessing){
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
			}
            if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())){
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
     * Calculates and returns the amount that needs to be capture.
     * 
     * @param orderShipment the {@link IOrderShipment} object
     * @param order the {@link IOrder} object
     * @return the amount to capture
     */
	@SuppressWarnings("unchecked")
	protected IAmount getAmountToCapture(IOrderShipment orderShipment, IOrder order) {
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
	
    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public PaymentCaptureResult capturePayment(IOrderShipment orderShipment, boolean isCCOMsettlement, boolean isJobProcessing) {
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

        // reset the shipment confirmation flag if its a ppos order.
        boolean pPosOrder = orderManager.isPPosOrder(order);
        if (pPosOrder) {
            sendShipmentConfirmationEmail = false;
        }
        // Check if payment was through credit card or card present.
        if (PaymentUtil.isPaymentThroughCreditCardOrCardPresent(order.getPayments())) {

            boolean captureTransactionRequired = false;

            // Get available authorizations for this Order or Order_Shipment in case of PPOS.
            List<Authorization> authorizations = pPosOrder ? authorizationDAO.findAuthorizationsByOrderShipmentID(new Long(orderShipment.getPk().getAsString()))
                    : authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // check if this is a PPOS OrderShipment paid by cash
            boolean shipmentPaidByCash = false;
            if (pPosOrder && authorizations== null && PaymentUtil.isPaymentThroughCash(order.getPayments())
            && orderShipment.getAuthRequestIDs().isEmpty()){
               shipmentPaidByCash = true;
            }
            // Check if any of the authorizations are unused.
            Authorization authorization = getUnusedAuthorization(authorizations);

            // Get amount to capture.
            IAmount amountToCharge = new Amount();
            if (pPosOrder && !shipmentPaidByCash) {
                // PPOS orders have authorizations by shipment, so no need to calculate the capture to amount
                amountToCharge = new Amount (authorization.getAmountAuthorized());
            } else if (!pPosOrder){
                amountToCharge = getAmountToCapture(orderShipment, order);
            }
            
            if (amountToCharge != null && !shipmentPaidByCash) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToCharge.toBigDecimal());
                amountToCharge = new Amount(formattedAmount);

                if (formattedAmount != null && amountToCharge != null && amountToCharge.compareTo(new Amount()) == 1) {
                    captureTransactionRequired = true;

                    boolean isPaymentechPaymentGateway = false;
                    // Get type of card used for payment i.e. VI,MC,DS etc.
                    String type = getCardType(order);
                    IPaymentGateway paymentGateway = paymentTransactionManager.getPaymentGatewayByCreditCardType(type);
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
                        creditCardRequest.setCreditCardType(type);
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
                        // If authorization not available and payment through credit card then authorize and capture
                        if (PaymentUtil.isPaymentThroughCreditCard(order)) {
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
            }

            // There was a credit card payment amount to be captured in this method execution.
            if (captureTransactionRequired && !shipmentPaidByCash) {
                if (creditCardResponse != null && creditCardResponse.isSuccess()) {
                    updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName() );
                    if (sendShipmentConfirmationEmail){
                        sendOrderShipmentEmail(order, orderShipment);
                    }
                }
            } else { // There was no credit card payment amount to be captured in this method execution.
                updateOrderShipmentStatus(order, orderShipment, IOrderManager.OrderStatus.COMPLETE.getName() );
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
                    updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName() );

                    IOrderPaymentAmazon orderPayment = PaymentUtil.getAmazonPayment(order.getPayments());

                    //Closing AmazonOrderReferenceID in Amazon after a successful capture
                    if(order.getStatus().equalsIgnoreCase(IOrderManager.OrderStatus.COMPLETE.getName())) {
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
                updateOrderShipmentStatus(order, orderShipment, IOrderManager.OrderStatus.COMPLETE.getName() );
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
                    updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName() );

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
                updateOrderShipmentStatus(order, orderShipment, IOrderManager.OrderStatus.COMPLETE.getName() );
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
        }else { // Payment is through Gift Certificate/Cash.
        	updateOrderShipmentStatus(order, orderShipment,IOrderManager.OrderStatus.COMPLETE.getName());
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
        
        // Updating the captureAttempts only for capture job
        if (isJobProcessing){
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
            }else if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())){
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

        }

        // save the success or failure (only attempts made)
        orderHome.update(order);
     
        return paymentCaptureResult;
    }
	
}
