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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderHome;
import org.marketlive.entity.cart.order.IOrderItem;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.currency.IAmount;
import org.marketlive.system.config.IConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.IPaymentVoidManager;
import com.deplabs.biz.payment.PaymentVoidResult;
import com.deplabs.biz.payment.gateway.affirm.AffirmApiAction;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.dao.payment.IRefundDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.domain.payment.Capture;
import com.marketlive.entity.currency.Amount;
import com.marketlive.system.annotation.PlatformService;

/**
 * An implementation of {@link IPaymentVoidManager} responsible for voiding amounts of order shipments.
 *
 */
@PlatformService
public class AffirmPaymentVoidManager implements IPaymentVoidManager {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(AffirmPaymentVoidManager.class);

	
	/** Constant holding the configurable property key name for 'order shipment status'. */
	private static final String ORDER_SHIPMENT_STATUS_VOID_ALLOWED= "custom.payment_paymentVoidJob_order_shipment_status"; // Canceled
	
	/** Constant holding the configurable property key name for 'days'. */
	private static final String DAYS = "custom.payment_paymentVoidJob_days";  // 30

	/** Constants representing 'Complete' Order status. */
	public static final String ORDER_STATUS_ERROR_HOLD = "Error Hold";

    /** Constants representing 'Canceled' Order status. */
    public static final String ORDER_STATUS_CANCELED = "Canceled";
    
    private boolean isVoidAmountExceed = false;

    /** Reference to the {@link IConfigurationManager}. */
    @Autowired
	private IConfigurationManager configurationManager;
	
    /** Reference to the {@link IOrderManager}. */
    @Autowired
	private IOrderManager orderManager;
	
	/** Reference to the {@link IOrderHome}. */
    @Autowired
	private IOrderHome orderHome;

    /** Reference to the {@link IAuthorizationDAO}. */
    @Autowired
    private IAuthorizationDAO authorizationDAO;
    
    /** Reference to the {@link ICaptureDAO}. */
    @Autowired
    private ICaptureDAO captureDAO;

    /** Reference to the {@link IRefundDAO}. */
    @Autowired
    private IRefundDAO refundDAO;
    
    @Autowired
    private IAffirmPaymentTransactionManager affirmPaymentTransactionManager;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<PaymentVoidResult> voidPayment() {
		List<PaymentVoidResult> paymentVoidResultList = new ArrayList<PaymentVoidResult>();
		
		Set<IOrderShipment> orderShipmentsToVoid = getOrderShipmentsToVoid();

        if (orderShipmentsToVoid != null && !orderShipmentsToVoid.isEmpty()) {
            // For each shipment, do a payment Void.
            for (IOrderShipment orderShipment : orderShipmentsToVoid) {
                if (orderShipment != null && orderShipment.getPk() != null) {
                	logger.debug("Processing Void for order shipment with id: " + orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
                    
                	PaymentVoidResult paymentVoidResult = new PaymentVoidResult();

                    // Get the order for this OrderShipment
                    IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());
                    boolean affirmOrder = false;
                    if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {
                    	affirmOrder = true;
                    }
                	
                	// Get order shipment status value from configuration that allows payment to be Voided.
                    String orderShipmentStatus = configurationManager.getAsString(ORDER_SHIPMENT_STATUS_VOID_ALLOWED);
                    if (!orderShipment.getStatus().equalsIgnoreCase(orderShipmentStatus)) { // We assume that shipment status is never null.
                    	logger.error("Void request failed for order shipment with id: " +
                                orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + 
                                " with error message: " + "Cannot Void - because Order Shipment status does not match with the one defined in " +
                                "'custom.payment_paymentVoidJob_order_shipment_status' properties file.");
                    	
                    	paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                    	paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.FAILURE);
                    	paymentVoidResult.setResultMessage("Cannot Void - because Order Shipment status does not match with the one defined in " +
                    			"'custom.payment_paymentVoidJob_order_shipment_status' properties file.");
                        if (affirmOrder){
                            paymentVoidResult.setAffirmResponse(new AffirmResponse());
                        }

                    	paymentVoidResultList.add(paymentVoidResult);
                    	
                    	continue;
                    }
                    
                    // Check whether all items within the order shipment have status "Canceled"
                    List<IOrderItem> shipmentOrderItems = orderShipment.getItems();
                    if (shipmentOrderItems != null) {
                    	boolean isVoidAllowed = true;
                        for (IOrderItem shipmentOrderItem : shipmentOrderItems) {
                            if( !shipmentOrderItem.getStatus().equalsIgnoreCase("Canceled")) {
                                logger.error("Void request failed for order shipment with id: " +
                                        orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                                        " with error message: " + "Cannot Void - because the status of all items within the Order Shipment is not Canceled. " +
                                        "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + " have status of " + shipmentOrderItem.getStatus());

                                paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                                paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.FAILURE);
                                paymentVoidResult.setResultMessage("Cannot PaymentVoidResult - because the status of all items within the Order Shipment is not Canceled. " +
                                        "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + "have status of " + shipmentOrderItem.getStatus());
                                if (affirmOrder){
                                    paymentVoidResult.setAffirmResponse(new AffirmResponse());
                                }
                                paymentVoidResultList.add(paymentVoidResult);
                                isVoidAllowed = false;
                                break;
                            }
                        }
                        if(!isVoidAllowed){
                        	continue;
                        }
                    }
                    
                    if (affirmOrder) {
                    	boolean isVoidAllowed = true;
                    	// If Affirm order, check whether there wasn't Captures performed for his order
                    	List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
                        if (captures != null && captures.size() > 0) {
                        	logger.error("Void request failed for order with id: " +
                        			order.getPk().getAsString() + " and code: " + order.getCode() +
                                    " with error message: " + "Cannot Void - because the affirm payment was already captured.");

                            paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                            paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.FAILURE);
                            paymentVoidResult.setResultMessage("Cannot Void - because the affirm payment was already captured.");
                            paymentVoidResult.setAffirmResponse(new AffirmResponse());
                            paymentVoidResultList.add(paymentVoidResult);
                            isVoidAllowed = false;
                        }
                        if(!isVoidAllowed){
                        	continue;
                        }
                        
                        // Check whether this void action was performed before
                        // Get authorizations for this Order.
                        List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

                        // Check if any of the authorizations are unused.
                        Authorization authorization = getUnusedAuthorization(authorizations);
                        if (null == authorization) {
                        	logger.error("Void request failed for order with id: " +
                        			order.getPk().getAsString() + " and code: " + order.getCode() +
                                    " with error message: " + "Cannot Void - because the affirm authorization was already used (it was voided before?)");

                            paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                            paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.FAILURE);
                            paymentVoidResult.setResultMessage("Cannot Void - because the because the affirm authorization was already used (it was voided before?)");
                            paymentVoidResult.setAffirmResponse(new AffirmResponse());
                            paymentVoidResultList.add(paymentVoidResult);
                            isVoidAllowed = false;
                        }
                        if(!isVoidAllowed){
                        	continue;
                        }
                    }
                    
                    // Void payment for this OrderShipment.
                    paymentVoidResult = voidPayment(orderShipment);
                    paymentVoidResultList.add(paymentVoidResult);

                    // Updating the VoidAttempts --> TODO: there's no "Void attemps" column in ORDER_SHIPMENT table, should we add it?
                    /*
                    int attemptsMade = orderShipment.getVoidAttempts();
                    orderShipment.setRefundAttempts(attemptsMade + 1);
                    orderHome.update(order);
                    logger.debug("Attempts made to Refund order shipment with id: " +
                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + " has been set to: " + attemptsMade+1);
                    */

                    if (affirmOrder) {
                        if (paymentVoidResult == null || paymentVoidResult.getAffirmResponse() == null) { // If the Void request seems to have failed.
                            String msg = "Affirm Void request failed for order shipment with id: " +
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();
                            logger.error(msg);
                            processVoidFailure(order, orderShipment, msg);
                        } else if (!paymentVoidResult.getAffirmResponse().isSuccess()) {
                            String msg = "Void request failed for order shipment with id: " +
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                                    " with error message: " + paymentVoidResult.getAffirmResponse().getResponseMessage();
                            logger.error(msg);
                            processVoidFailure(order, orderShipment, msg);
                        } else {
                            String msg = "Void request succeeded for order shipment with id: " +
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode();

                            logger.debug(msg);

                            processVoidSuccess(order, msg);
                        }
                    }
                }
            }
        }
        
        return paymentVoidResultList;
	}
	
	/**
	 * This method is responsible for finding and returning all {@link IOrderShipment}(s) that satisfy the following conditions:
	 * <ul>
	 *   <li>They have a status value that allows payment to be Voided.</li>
	 *   <li>They were created during the specified date range.</li>
	 * </ul>
	 * 
	 * @return the set of {@link IOrderShipment} objects
	 */
	private Set<IOrderShipment> getOrderShipmentsToVoid() {
        Set<IOrderShipment> orderShipmentsToVoid= new HashSet<IOrderShipment>();
        
        // Get order shipment status value from configuration that allows payment to be voided.
        String orderShipmentStatus = configurationManager.getAsString(ORDER_SHIPMENT_STATUS_VOID_ALLOWED);
        
        // Get number of days from configuration.
        int days = configurationManager.getAsInt(DAYS, 10);
        
     	// Get current time instance.
    	GregorianCalendar calendar = new GregorianCalendar();
    	
        // Subtract the configured number of days from today, to get the date range to look back at (in the ORDER_SHIPMENT table).
        calendar.add(Calendar.DAY_OF_MONTH, -days);

        // Find OrderShipment(s) that satisfy the following conditions:
        // (1) They have a status value that allows payment to be voided.
        // (2) and they were created during the specified date range.
        List<IOrderShipment> orderShipments = orderManager.findOrderShipmentsByStatusAndDateCreated(orderShipmentStatus, calendar.getTime());
        if (orderShipments != null) {
        	orderShipmentsToVoid.addAll(orderShipments);
        }
            
        if (orderShipmentsToVoid == null || orderShipmentsToVoid.size() == 0) {
        	logger.debug("No shipments found in the ORDER_SHIPMENT table that have a STATUS value " + 
        			"that allows a payment amount to be Voided for them, and that were created in the " +
        			"last " + days + " days");
        }
        
        return orderShipmentsToVoid;
    }
	
	@Override
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public PaymentVoidResult voidPayment(IOrderShipment orderShipment) {
		PaymentVoidResult paymentVoidResult = new PaymentVoidResult();
        AffirmResponse affirmResponse = new AffirmResponse();

        // Get the Order for this OrderShipment.
        IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());
        
        Authorization authorization = null;
        
        if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {
            IAmount orderTotal = order.getTotal();

            // Get amount to Void.
            IAmount amountToVoid = getAffirmAmountToVoid(orderShipment, order, orderTotal); // TODO: Test & remove if not needed. 
            
            // Get authorizations for this Order.
            List<Authorization> authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(order.getPk().getAsString()));

            // Check if any of the authorizations are unused.
            authorization = getUnusedAuthorization(authorizations);
            
            if (authorization != null) {
	            // get the amount to void based on the authorized amount
	            amountToVoid = new Amount(authorization.getAmountAuthorized());
	
	            if (amountToVoid != null) {
	                NumberFormat format = NumberFormat.getInstance();
	                format.setMinimumFractionDigits(2);
	                format.setMaximumFractionDigits(2);
	                format.setGroupingUsed(false);
	                String formattedAmount = format.format(amountToVoid.toBigDecimal());
	                amountToVoid = new Amount(formattedAmount);
	                Amount affirmAmount = new Amount(amountToVoid);
	                affirmAmount.multiply(100); // Get the amount formatted for AFFIRM API
	                
	                if (formattedAmount != null && amountToVoid != null && amountToVoid.compareTo(new Amount()) == 1) {
	                    AffirmRequest affirmRequest = new AffirmRequest();
	                    // for void AFFIRM needs: the CHARGE_ID for the ServiceURL
	                    affirmRequest.setRequestType(AffirmApiAction.VOID);
	                    affirmRequest.setChargeId(authorization.getRequestID());  // This is the charge ID returned by AFFIRM on the auth action
	                    affirmRequest.setAmount(formattedAmount);
	                    affirmResponse = affirmPaymentTransactionManager.voidPayment(affirmRequest, Integer.parseInt(order.getCustomer().getPk().getAsString()));
	                }
	            }
            } else {
            	paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
            	affirmResponse.setResponseMessage("Cannot Void - because the affirm authorization was used.");
            }
        }

        if (affirmResponse.isSuccess()) {
            paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.SUCCESS);
            
            // update the authorization used to avoid mutiple 'void' actions
            authorization.setAuthorizationUsed(true);
            authorizationDAO.update(authorization);
        } else {
            paymentVoidResult.setPaymentVoidResultCode(PaymentVoidResult.PaymentVoidResultCode.FAILURE);
        }
        paymentVoidResult.setResultMessage(affirmResponse.getResponseMessage());
        paymentVoidResult.setAffirmResponse(affirmResponse);
        
        paymentVoidResult.setOrderShipmentId(orderShipment.getPk().getAsString());
        
        return paymentVoidResult;
	}
	
	/**
	 * This method returns the unused (means amount is not Voided against it) {@link Authorization}.
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
     * Calculates and returns the amount that needs to be Voided for an Affirm transaction
     * 
     * @param orderShipment the {@link IOrderShipment} object
     * @param order the {@link IOrder} object
     * @return the amount to Void
     */
	private IAmount getAffirmAmountToVoid(IOrderShipment orderShipment, IOrder order, IAmount voidAmountRequested) {
    	return voidAmountRequested;

    	/*
    	IAmount amountToVoid = voidAmountRequested;
    	isVoidAmountExceed = false;
        IAmount totalAmountCapturedByAffirm = new Amount();
        IAmount totalAffirmAmountRefund = new Amount(amountToVoid);
        IAmount totalRefundedAmount = new Amount();
        IAmount totalRefundedPlusRefundAmount = new Amount();

        List<Capture> captures = captureDAO.findCapturesByOrderId(Long.parseLong(order.getPk().getAsString()));
        // Add up the amounts that has been captured for this Order.
        if (captures != null && captures.size() > 0) {
            for (Capture capture : captures) {
                totalAmountCapturedByAffirm.add(new Amount(capture.getAmountCaptured()));
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
        totalRefundedPlusRefundAmount.add(new Amount(amountToVoid));

        //Check if the refunded amount is not more than captured amount
        if (totalRefundedAmount.compareTo(totalAmountCapturedByAffirm) > 0 ){
        	isVoidAmountExceed = true;
        } else if (totalRefundedPlusRefundAmount.compareTo(totalAmountCapturedByAffirm) > 0){  //Check if the refunded amount plus new refund request is not more than captured amount
        	isVoidAmountExceed = true;
        }
        List<IOrderPayment> orderPayments = order.getPayments();
        // if amount requested to refund is more than captured amount, then set amount to refund as captured amount.
        if (totalAmountCapturedByAffirm.compareTo(amountToVoid) == -1) {
        	isVoidAmountExceed = true;
        }
        return totalAffirmAmountRefund;
        */
    }

    private void processVoidSuccess(IOrder order, String successMsg){
        String  comment1 = order.getComment1() != null ? order.getComment1()+"\n"+successMsg : successMsg;
        comment1 = comment1.length() > 500 ? comment1.substring(0, 500) : comment1;
        order.setComment1(comment1);

        orderHome.update(order);
    }

    private void processVoidFailure(IOrder order, IOrderShipment orderShipment, String failureMsg){

        String  comment1 = order.getComment1() != null ? order.getComment1()+"\n"+failureMsg : failureMsg;
        comment1 = comment1.length() > 500 ? comment1.substring(0, 500) : comment1;
        order.setComment1(comment1);
        orderHome.update(order);
        
        ////// TODO: OOB the orderShipment does not have a "voidAttempts" column to work with this logic
        
        /*
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
        */
    }
    
    /*
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
    */
}
