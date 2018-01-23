package com.deplabs.biz.payment.impl;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderHome;
import org.marketlive.entity.cart.order.IOrderItem;
import org.marketlive.entity.cart.order.IOrderItemPart;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.messaging.IQueueWriter;
import org.marketlive.system.config.IConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deplabs.biz.payment.AffirmPaymentRefundResult;
import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmApiAction;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.payment.AmazonRequest;
import com.marketlive.biz.payment.AmazonResponse;
import com.marketlive.biz.payment.CreditCardResponse;
import com.marketlive.biz.payment.IAmazonPaymentTransactionManager;
import com.marketlive.biz.payment.PaymentRefundResult;
import com.marketlive.biz.payment.PaymentUtil;
import com.marketlive.biz.payment.gateway.amazon.impl.AmazonPaymentConstants;
import com.marketlive.biz.payment.impl.PaymentRefundManager;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.dao.payment.IRefundDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.domain.payment.Capture;
import com.marketlive.domain.payment.Refund;
import com.marketlive.entity.currency.Amount;
import com.marketlive.messaging.StompQueueMessage;
import com.marketlive.system.annotation.PlatformService;
import com.marketlive.system.site.ActiveSite;

/**
 * An extension of {@link PaymentRefundManager} responsible for refunding amounts of order shipments
 *
 */
@Primary
@PlatformService
public class AffirmPaymentRefundManager extends PaymentRefundManager {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(AffirmPaymentRefundManager.class);

	
	/** Constant holding the configurable property key name for 'order shipment status'. */
	private static final String ORDER_SHIPMENT_STATUS_REFUND_ALLOWED= "biz.payment.paymentRefundJob.order_shipment_status";
	
	/** Constant holding the configurable property key name for 'days'. */
	private static final String DAYS = "biz.payment.paymentRefundJob.days";

    /** Constant holding the configurable property key name for 'allowed Refund attempts'. */
    private static final String ALLOWED_REFUND_ATTEMPTS= "biz.payment.paymentRefundJob.allowed_Refund_attempts";
	
    private boolean isRefundAmountExceed = false;
    
    /** Reference to the {@link IConfigurationManager}. */
    @Autowired
	private IConfigurationManager configurationManager;
	
    /** Reference to the {@link IOrderManager}. */
    @Autowired
	private IOrderManager orderManager;
	
	/** Reference to the {@link IOrderHome}. */
    @Autowired
	private IOrderHome orderHome;

    @Autowired
    private IAmazonPaymentTransactionManager amazonPaymentTransactionManager;
    
    @Autowired
    private IAffirmPaymentTransactionManager affirmPaymentTransactionManager;

    /** Reference to the {@link ICaptureDAO}. */
    @Autowired
    private ICaptureDAO captureDAO;

    /** Reference to the {@link IRefundDAO}. */
    @Autowired
    private IRefundDAO refundDAO;
	
    /** Reference to the QueueWriter. */
    @Autowired
    @Qualifier("jmsQueueWriter")
    private IQueueWriter jmsQueueWriter;
    
    /** Reference to the {@link IAuthorizationDAO}. */
    @Autowired
    private IAuthorizationDAO authorizationDAO;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<PaymentRefundResult> refundPayment() {
		List<PaymentRefundResult> paymentRefundResultList = new ArrayList<PaymentRefundResult>();
		
		Set<IOrderShipment> orderShipmentsToRefund = getOrderShipmentsToRefund();

        if (orderShipmentsToRefund != null && !orderShipmentsToRefund.isEmpty()) {
            // For each shipment, do a payment Refund.
            for (IOrderShipment orderShipment : orderShipmentsToRefund) {
                if (orderShipment != null && orderShipment.getPk() != null) {
                    	
                    	logger.debug("Processing Refund for order shipment with id: " + orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
                        
                    	PaymentRefundResult paymentRefundResult = new AffirmPaymentRefundResult();

                        // Get the order for this OrderShipment
                        IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());
                        boolean amazonOrder = false;
                        boolean affirmOrder = false;
                        if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())) {
                            amazonOrder = true;
                        }
                        if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {
                            affirmOrder = true;
                        }
                    	
                    	// Get order shipment status value from configuration that allows payment to be Refunded.
                        String orderShipmentStatus = configurationManager.getAsString(ORDER_SHIPMENT_STATUS_REFUND_ALLOWED);
                        if (!orderShipment.getStatus().equalsIgnoreCase(orderShipmentStatus)) { // We assume that shipment status is never null.
                        	logger.error("Refund request failed for order shipment with id: " +
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + 
                                    " with error message: " + "Cannot Refund - because Order Shipment status does not match with the one defined in " +
                                    "'biz.paymentRefundJob.order_shipment_status' properties file.");
                        	
                        	paymentRefundResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                        	paymentRefundResult.setPaymentRefundResultCode(PaymentRefundResult.PaymentRefundResultCode.FAILURE);
                        	paymentRefundResult.setResultMessage("Cannot Refund - because Order Shipment status does not match with the one defined in " +
                        			"'biz.paymentRefundJob.order_shipment_status' properties file.");
							if (amazonOrder) {
								paymentRefundResult.setAmazonResponse(new AmazonResponse());
							} else if (affirmOrder) {
								((AffirmPaymentRefundResult) paymentRefundResult).setAffirmResponse(new AffirmResponse());
							} else {
								paymentRefundResult.setCreditCardResponse(new CreditCardResponse());
							}

                        	paymentRefundResultList.add(paymentRefundResult);
                        	
                        	continue;
                        }

                        // Check whether all items within the order shipment have status "Canceled" or "Complete"
                        List<IOrderItem> shipmentOrderItems = orderShipment.getItems();
                        if (shipmentOrderItems != null) {
                        	boolean isRefundAllowed = true;
                            for (IOrderItem shipmentOrderItem : shipmentOrderItems) {
                                if(!shipmentOrderItem.getStatus().equalsIgnoreCase("Canceled") && !shipmentOrderItem.getStatus().equalsIgnoreCase("Complete")){
                                    logger.error("Refund request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                                            " with error message: " + "Cannot Refund - because the status of all items within the Order Shipment is not Complete or Canceled. " +
                                            "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + " have status of " + shipmentOrderItem.getStatus());

                                    paymentRefundResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                                    paymentRefundResult.setPaymentRefundResultCode(PaymentRefundResult.PaymentRefundResultCode.FAILURE);
                                    paymentRefundResult.setResultMessage("Cannot Refund - because the status of all items within the Order Shipment is not Complete or Canceled. " +
                                            "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + "have status of " + shipmentOrderItem.getStatus());
									if (amazonOrder) {
										paymentRefundResult.setAmazonResponse(new AmazonResponse());
									} else if (affirmOrder) {
										((AffirmPaymentRefundResult) paymentRefundResult).setAffirmResponse(new AffirmResponse());
									} else {
										paymentRefundResult.setCreditCardResponse(new CreditCardResponse());
									}
                                    paymentRefundResultList.add(paymentRefundResult);
                                    isRefundAllowed = false;
                                    break;
                                }
                            }
                            if(!isRefundAllowed){
                            	continue;
                            }
                        }
                        if (order.isRefund() && order.getRefundAmount() != null && order.getRefundAmount().compareTo(new Amount()) > 0){
                            // Refund payment for this OrderShipment.
                            paymentRefundResult = refundPayment(orderShipment);
                            paymentRefundResultList.add(paymentRefundResult);

                            // Updating the RefundAttempts
                            int attemptsMade = orderShipment.getRefundAttempts();
                            orderShipment.setRefundAttempts(attemptsMade + 1);
                            orderHome.update(order);
                            logger.debug("Attempts made to Refund order shipment with id: " +
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + " has been set to: " + attemptsMade+1);

                            if (amazonOrder) {
                                if (paymentRefundResult == null || paymentRefundResult.getAmazonResponse() == null) { // If the Refund request seems to have failed.
                                    String msg = "Amazon Refund request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +" for refund amount: "+order.getRefundAmount().getAsString();
                                    logger.error(msg);
                                    if(isRefundAmountExceed) {
                                        msg = "Amazon Refund request failed - Refund amount exceeds the actual captured amount.";
                                    }
                                    processRefundFailure(order, orderShipment, msg, attemptsMade + 1);
                                } else if (!paymentRefundResult.getAmazonResponse().isSuccess()) {
                                    String msg = "Refund request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +" for refund amount: "+order.getRefundAmount().getAsString() +
                                            " with error message: " + paymentRefundResult.getAmazonResponse().getResponseMessage();
                                    logger.error(msg);
                                    processRefundFailure(order, orderShipment, msg, attemptsMade + 1);
                                } else {
                                    String msg ="Refund request succeeded for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode()+
                                            " with Amazon refund amount of "+paymentRefundResult.getAmazonResponse().getAmountRefunded();

                                    logger.debug(msg);

                                    processRefundSuccess(order, msg);
                                }


                            } else if (affirmOrder) {
                                if (paymentRefundResult == null || ((AffirmPaymentRefundResult)paymentRefundResult).getAffirmResponse() == null) { // If the Refund request seems to have failed.
                                    String msg = "Amazon Refund request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +" for refund amount: "+order.getRefundAmount().getAsString();
                                    logger.error(msg);
                                    if(isRefundAmountExceed) {
                                        msg = "Affirm Refund request failed - Refund amount exceeds the actual captured amount.";
                                    }
                                    processRefundFailure(order, orderShipment, msg, attemptsMade + 1);
                                } else if (!((AffirmPaymentRefundResult)paymentRefundResult).getAffirmResponse().isSuccess()) {
                                    String msg = "Refund request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +" for refund amount: "+order.getRefundAmount().getAsString() +
                                            " with error message: " + ((AffirmPaymentRefundResult)paymentRefundResult).getAffirmResponse().getResponseMessage();
                                    logger.error(msg);
                                    processRefundFailure(order, orderShipment, msg, attemptsMade + 1);
                                } else {
                                    String msg ="Refund request succeeded for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();/*+
                                            " with Affirm refund amount of "+((AffirmPaymentRefundResult)paymentRefundResult).getAffirmResponse().getAmountRefunded();*/

                                    logger.debug(msg);

                                    processRefundSuccess(order, msg);
                                }
                            }
                        }


                }
            }
        }
        
        return paymentRefundResultList;
	}
	
	/**
	 * This method is responsible for finding and returning all {@link IOrderShipment}(s) that satisfy the following conditions:
	 * <ul>
	 *   <li>They have a status value that allows payment to be Refunded.</li>
	 *   <li>They were created during the specified date range.</li>
	 * </ul>
	 * 
	 * @return the set of {@link IOrderShipment} objects
	 */
	private Set<IOrderShipment> getOrderShipmentsToRefund() {
        Set<IOrderShipment> orderShipmentsToRefund = new HashSet<IOrderShipment>();
        
        // Get order shipment status value from configuration that allows payment to be refunded.
        String orderShipmentStatus = configurationManager.getAsString(ORDER_SHIPMENT_STATUS_REFUND_ALLOWED);
        
        // Get number of days from configuration.
        int days = configurationManager.getAsInt(DAYS);
        
     	// Get current time instance.
    	GregorianCalendar calendar = new GregorianCalendar();
    	
        // Subtract the configured number of days from today, to get the date range to look back at (in the ORDER_SHIPMENT table).
        calendar.add(Calendar.DAY_OF_MONTH, -days);

        // Find OrderShipment(s) that satisfy the following conditions:
        // (1) They have a status value that allows payment to be refunded.
        // (2) and they were created during the specified date range.
        List<IOrderShipment> orderShipments = orderManager.findOrderShipmentsByStatusAndDateCreated(orderShipmentStatus, calendar.getTime());
        if (orderShipments != null) {
        	orderShipmentsToRefund.addAll(orderShipments);
        }
            
        if (orderShipmentsToRefund == null || orderShipmentsToRefund.size() == 0) {
        	logger.debug("No shipments found in the ORDER_SHIPMENT table that have a STATUS value " + 
        			"that allows a payment amount to be Refunded for them, and that were created in the " +
        			"last " + days + " days");
        }
        
        return orderShipmentsToRefund;
    }
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public PaymentRefundResult refundPayment(IOrderShipment orderShipment) {
		PaymentRefundResult paymentRefundResult = new AffirmPaymentRefundResult();
        AmazonResponse amazonResponse = new AmazonResponse();
        AffirmResponse affirmResponse = new AffirmResponse();

        // Get the Order for this OrderShipment.
        IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());

        // Check if payment was through Amazon
        
        boolean affirmOrder = false;
        if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {
            affirmOrder = true;
        }

        if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())) {

            IAmount refundAmountRequested = order.getRefundAmount();

            // Get amount to Refund.
            IAmount amountToRefund = getAmountToRefund(orderShipment, order, refundAmountRequested);

            if(isRefundAmountExceed) {
                return null;
            }

            if (amountToRefund != null) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToRefund.toBigDecimal());
                amountToRefund = new Amount(formattedAmount);

                if (formattedAmount != null && amountToRefund != null && amountToRefund.compareTo(new Amount()) == 1) {

                    AmazonRequest amazonRequest = new AmazonRequest();
                    amazonRequest.setCurrency(order.getSite().getCurrency().getCurrencyCode());
                    amazonRequest.setAmount(formattedAmount);
                    amazonRequest.setSellerId(configurationManager.getAsString("biz.payment.amazonpayment.sellerId"));
                    String randomNumber = String.valueOf(Math.random()).substring(0, 9);
                    randomNumber = randomNumber.replace('.', '0');
                    amazonRequest.setRefundRefId(order.getPk().getAsString().concat(randomNumber));
                    String captureID = "";
                    List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
                    if (captures != null && captures.size() > 0) {
                        for (Capture capture : captures) {
                            captureID = capture.getCustomFields() != null ? capture.getCustomFields().get(AmazonPaymentConstants.AMAZON_CAPTURE_ID) : "";
                            if (!captureID.equals("")) {
                                amazonRequest.setAmazonCaptureId(captureID);

                                amazonResponse = amazonPaymentTransactionManager.refundPayment(amazonRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()));

                                if (amazonResponse.isSuccess()){
                                    try{
                                        String refundRequestID = amazonResponse.getRequestID();
                                        Refund refund = refundDAO.findRefundByRequestID(refundRequestID);
                                        if(refund != null) {
                                            refund.setCapture(capture);
                                            refundDAO.save(refund);
                                        }
                                        break;
                                    }catch(Exception e){
                                        logger.error("Failed to update the refund data for order id: " +order.getPk().getAsString() +
                                                " and orderReturn code : " + order.getCode() + e);
                                    }

                                }
                                break;
                            }
                        }
                    }
                }
            }

        } else if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {

            IAmount refundAmountRequested = order.getRefundAmount();

            // Get amount to Refund.
            IAmount amountToRefund = getAmountToRefund(orderShipment, order, refundAmountRequested);
            
            // Get authorizations for this Order.
            List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // Check if any of the authorizations are used.
            Authorization authorization = getUsedAuthorization(authorizations);

            if(isRefundAmountExceed) {
                return null;
            }

            if (amountToRefund != null) {
                NumberFormat format = NumberFormat.getInstance();
                format.setMinimumFractionDigits(2);
                format.setMaximumFractionDigits(2);
                format.setGroupingUsed(false);
                String formattedAmount = format.format(amountToRefund.toBigDecimal());
                amountToRefund = new Amount(formattedAmount);
                Amount affirmAmount = new Amount(amountToRefund);
                affirmAmount.multiply(100); // Get the amount formatted for AFFIRM API
                
                if (formattedAmount != null && amountToRefund != null && amountToRefund.compareTo(new Amount()) == 1) {

                    AffirmRequest affirmRequest = new AffirmRequest();
                    // for refund AFFIRM needs: {"amount": 500}
                    // and CHARGE_ID for the ServiceURL
                    affirmRequest.setRequestType(AffirmApiAction.REFUND);
                    affirmRequest.setChargeId(authorization.getRequestID());  // This is the charge ID returned by AFFIRM on the auth action
                    affirmRequest.setAmount(affirmAmount.toBigDecimal().toBigInteger().toString());
                    
                    /*String randomNumber = String.valueOf(Math.random()).substring(0, 9);
                    randomNumber = randomNumber.replace('.', '0');
                    affirmRequest.setRefundRefId(order.getPk().getAsString().concat(randomNumber));*/ // TODO: IS THIS USEFUL?
                    
                    String captureID = "";
                    List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
                    if (captures != null && captures.size() > 0) {
                        for (Capture capture : captures) {
                            //captureID = capture.getCustomFields() != null ? capture.getCustomFields().get(AmazonPaymentConstants.AMAZON_CAPTURE_ID) : "";
                            //if (!captureID.equals("")) {
                                //amazonRequest.setAmazonCaptureId(captureID);

                                affirmResponse = affirmPaymentTransactionManager.refundPayment(affirmRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()));

                                if (affirmResponse.isSuccess()){
                                    try {
                                        String refundRequestID = affirmResponse.getChargeId();
										Refund refund = refundDAO.findRefundByRequestID(refundRequestID);
										if (refund != null) {
											refund.setCapture(capture);
											refundDAO.save(refund);
										}
                                        break;
                                    } catch(Exception e) {
                                        logger.error("Failed to update the refund data for order id: " +order.getPk().getAsString() + " and orderReturn code : " + order.getCode() + e);
                                    }

                                }
                                break;
                            //}
                        }
                    }
                }
            }

        }

        if (amazonResponse.isSuccess()) {
            paymentRefundResult.setPaymentRefundResultCode(PaymentRefundResult.PaymentRefundResultCode.SUCCESS);
        } else if (affirmResponse.isSuccess()) {
        	paymentRefundResult.setPaymentRefundResultCode(PaymentRefundResult.PaymentRefundResultCode.SUCCESS);
        } else {
            paymentRefundResult.setPaymentRefundResultCode(PaymentRefundResult.PaymentRefundResultCode.FAILURE);
        }
        paymentRefundResult.setResultMessage((affirmOrder) ? affirmResponse.getResponseMessage() : amazonResponse.getResponseMessage());
        paymentRefundResult.setAmazonResponse(amazonResponse);
        ((AffirmPaymentRefundResult)paymentRefundResult).setAffirmResponse(affirmResponse);
        
        paymentRefundResult.setOrderShipmentId(orderShipment.getPk().getAsString());
        
        return paymentRefundResult;
	}
	
    /**
     * Calculates and returns the amount that needs to be Refund.
     * 
     * @param orderShipment the {@link IOrderShipment} object
     * @param order the {@link IOrder} object
     * @return the amount to Refund
     */
    private IAmount getAmountToRefund(IOrderShipment orderShipment, IOrder order, IAmount refundAmountRequested) {
        IAmount amountToRefund = refundAmountRequested;
        isRefundAmountExceed = false;
        IAmount totalAmountCapturedByAmazon = new Amount();
        IAmount totalAmazonAmountRefund = new Amount(amountToRefund);
        IAmount totalRefundedAmount = new Amount();
        IAmount totalRefundedPlusRefundAmount = new Amount();

        List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
        // Add up the amounts that has been captured for this Order.
        if (captures != null && captures.size() > 0) {
            for (Capture capture : captures) {
                totalAmountCapturedByAmazon.add(new Amount(capture.getAmountCaptured()));
            }
        }
        //Check the previous refunds, in case of multiple requests and make sure the refund is not exceeding total capture amount.
        if (captures != null && captures.size() > 0) {
            for (Capture capture : captures) {
                List<Refund> refunds = refundDAO.findRefundsByCapture(capture);
                if (refunds != null && refunds.size() > 0){
                    for (Refund refund : refunds){
                        totalRefundedAmount.add(new Amount(refund.getAmountRefunded()));
                    }
                }
            }
        }
        totalRefundedPlusRefundAmount.add(new Amount(totalRefundedAmount));
        totalRefundedPlusRefundAmount.add(new Amount(amountToRefund));

        //Check if the refunded amount is not more than captured amount
        if (totalRefundedAmount.compareTo(totalAmountCapturedByAmazon) > 0 ){
            isRefundAmountExceed = true;
        } else if (totalRefundedPlusRefundAmount.compareTo(totalAmountCapturedByAmazon) > 0){  //Check if the refunded amount plus new refund request is not more than captured amount
            isRefundAmountExceed = true;
        }
        List<IOrderPayment> orderPayments = order.getPayments();
        // if amount requested to refund is more than captured amount, then set amount to refund as captured amount.
        if (totalAmountCapturedByAmazon.compareTo(amountToRefund) == -1) {
                isRefundAmountExceed = true;
        }
        return totalAmazonAmountRefund;
    }

    @SuppressWarnings("unchecked")
    private void processRefundSuccess(IOrder order, String successMsg){
        String  comment1 = order.getComment1() != null ? order.getComment1()+"\n"+successMsg : successMsg;
        comment1 = comment1.length() > 500 ? comment1.substring(0, 500) : comment1;
        order.setComment1(comment1);

        order.setRefund(false);
        order.setRefundAmount(new Amount());
        orderHome.update(order);
    }

    @SuppressWarnings("unchecked")
    private void processRefundFailure(IOrder order, IOrderShipment orderShipment, String failureMsg, int attemptsMade){

        String  comment1 = order.getComment1() != null ? order.getComment1()+"\n"+failureMsg : failureMsg;
        comment1 = comment1.length() > 500 ? comment1.substring(0, 500) : comment1;
        order.setComment1(comment1);
        orderHome.update(order);
        
        if (attemptsMade >= configurationManager.getAsInt(ALLOWED_REFUND_ATTEMPTS)){

            updateOrderShipmentStatus(order, orderShipment, ORDER_STATUS_ERROR_HOLD );
            logger.error("Exceeded the number of attempts made to Refund order shipment with id: " +
                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + " and number of attempts: " +
                    attemptsMade + " Updating the order shipment status to " + ORDER_STATUS_ERROR_HOLD);
                
            if (ActiveSite.getSite()== null){
                ActiveSite.setSite(order.getSite());
            }else if (!order.getSite().getCode().equals(ActiveSite.getSite().getCode())){
                ActiveSite.setSite(order.getSite());
            }
            sendNotificationToJMSQueue(failureMsg);
        }
    }
    
	@SuppressWarnings("unchecked")
	private void updateOrderShipmentStatus(IOrder order, IOrderShipment orderShipment, String status) {
		// Set status of the OrderShipment
		orderShipment.setStatus(status);

		orderHome.update(order);
		
		// Set status of OrderItems of this OrderShipment
		List<IOrderItem> orderItems = orderShipment.getItems();
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
		
		// Set status of the Order if all of its OrderShipments have the updated status
		List<IOrderShipment> orderShipments = order.getShipments();
		boolean flag = true;
		for (IOrderShipment orderShipment1 : orderShipments) {
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
		orderHome.update(order);
	}

    
    private void sendNotificationToJMSQueue(String msg){
        sendNotificationToJMSQueue(msg, null);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void sendNotificationToJMSQueue(String msg, Exception ex){

        Map additionalMessageData = new HashMap();
        StompQueueMessage stompMessage = new StompQueueMessage ();      

        additionalMessageData.put("Process_ID", "PaymentRefundJob");
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
    
    /**
     * This method returns the used (means amount ALREADY captured against it) {@link Authorization}.
     * If used {@link Authorization} is not found then returns null.
     * 
     * @param authorizations the list of {@link Authorization}(s)
     * @return the used {@link Authorization} or null
     */
    private Authorization getUsedAuthorization(List<Authorization> authorizations) {
        if (authorizations != null) {
            for (Authorization authorization : authorizations) {
                if (authorization.isAuthorizationUsed()) {
                    return authorization;
                }
            }
        }
        
        return null;
    }
}
