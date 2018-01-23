package com.deplabs.biz.payment.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderItem;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.system.config.IConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.deplabs.biz.payment.AffirmPaymentCaptureResult;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentUtil;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.payment.AmazonResponse;
import com.marketlive.biz.payment.CreditCardResponse;
import com.marketlive.biz.payment.IPaymentCaptureManager;
import com.marketlive.biz.payment.PaymentCaptureResult;
import com.marketlive.biz.payment.PaymentCaptureResult.PaymentCaptureResultCode;
import com.marketlive.biz.payment.PaymentUtil;
import com.marketlive.biz.payment.impl.PaymentCaptureJobManager;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.system.annotation.PlatformService;
import com.marketlive.system.site.ActiveSite;

@PlatformService
@Primary
public class AffirmPaymentCaptureJobManager extends PaymentCaptureJobManager {
	
	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(PaymentCaptureJobManager.class);
	
	/** Constant holding the configurable property key name for 'order shipment status'. */
	private static final String ORDER_SHIPMENT_STATUS_CAPTURE_ALLOWED= "biz.payment.paymentCaptureJob.order_shipment_status";
    
    /** Reference to the {@link IConfigurationManager}. */
    @Autowired
	private IConfigurationManager configurationManager;
	
    /** Reference to the {@link IOrderManager}. */
    @Autowired
	private IOrderManager orderManager;

    /** Reference to the {@link IPaymentCaptureManager}. */
    @Autowired
    private IPaymentCaptureManager paymentCaptureManager;
    
    /** Reference to the {@link ICaptureDAO}. */
    @Autowired
    private ICaptureDAO captureDAO;
	
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public List<PaymentCaptureResult> capturePayment() {
		List<PaymentCaptureResult> paymentCaptureResultList = new ArrayList<PaymentCaptureResult>();
		
		Set<IOrderShipment> orderShipmentsToCapture = getOrderShipmentsToCapture();

        if (orderShipmentsToCapture != null && !orderShipmentsToCapture.isEmpty()) {
            // For each shipment, do a payment capture.
            for (IOrderShipment orderShipment : orderShipmentsToCapture) {
            	
            	// Get the order for this OrderShipment
                IOrder order = (IOrder) orderManager.findOrderByPk(orderShipment.getCart().getPk().getAsString());
                boolean amazonOrder = false;
                if (PaymentUtil.isPaymentThroughAmazon(order.getPayments())) {
                    amazonOrder = true;
                }
            	 boolean affirmOrder = false;
                 if (AffirmPaymentUtil.isPaymentThroughAffirm(order.getPayments())) {
                 	affirmOrder = true;
                 }
                 
                if (orderShipment != null && orderShipment.getPk() != null) {
                    if (captureDAO.findCaptureByOrderShipmentId(Long.parseLong(orderShipment.getPk().getAsString())) == null  || affirmOrder) { // If the payment was not already captured OR if it is an AFFIRM order 
                    	
                    	logger.debug("Processing capture for order shipment with id: " + orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode());
                        
                    	PaymentCaptureResult paymentCaptureResult = new AffirmPaymentCaptureResult();

                    	// Get order shipment status value from configuration that allows payment to be captured.
                        String orderShipmentStatus = configurationManager.getAsString(ORDER_SHIPMENT_STATUS_CAPTURE_ALLOWED);
                        if (!orderShipment.getStatus().equalsIgnoreCase(orderShipmentStatus)) { // We assume that shipment status is never null.
                        	logger.error("Capture request failed for order shipment with id: " + 
                                    orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() + 
                                    " with error message: " + "Cannot capture - because Order Shipment status does not match with the one defined in " +
                                    "'biz.paymentCaptureJob.order_shipment_status' properties file.");
                        	
                        	paymentCaptureResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                        	paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.FAILURE);
                        	paymentCaptureResult.setResultMessage("Cannot capture - because Order Shipment status does not match with the one defined in " +
                        			"'biz.paymentCaptureJob.order_shipment_status' properties file.");
							if (amazonOrder) {
								paymentCaptureResult.setAmazonResponse(new AmazonResponse());

							} else {
								if (affirmOrder){
									((AffirmPaymentCaptureResult)paymentCaptureResult).setAffirmResponse(new AffirmResponse());
								} else {
									paymentCaptureResult.setCreditCardResponse(new CreditCardResponse());
								}
							}

                        	paymentCaptureResultList.add(paymentCaptureResult);
                        	
                        	continue;
                        }

                        // Check whether all items within the order shipment have status "Canceled" or "Shipped"
                        List<IOrderItem> shipmentOrderItems = orderShipment.getItems();
                        if (shipmentOrderItems != null) {
                        	boolean isCaptureAllowed = true;
                            for (IOrderItem shipmentOrderItem : shipmentOrderItems) {
                                if(!shipmentOrderItem.getStatus().equalsIgnoreCase("Canceled") && !shipmentOrderItem.getStatus().equalsIgnoreCase("Shipped")){
                                    logger.error("Capture request failed for order shipment with id: " +
                                            orderShipment.getPk().getAsString() + " and code: " + orderShipment.getCode() +
                                            " with error message: " + "Cannot capture - because the status of all items within the Order Shipment is not Shipped or Canceled. " +
                                            "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + " have status of " + shipmentOrderItem.getStatus());

                                    paymentCaptureResult.setOrderShipmentId(orderShipment.getPk().getAsString());
                                    paymentCaptureResult.setPaymentCaptureResultCode(PaymentCaptureResultCode.FAILURE);
                                    paymentCaptureResult.setResultMessage("Cannot capture - because the status of all items within the Order Shipment is not Shipped or Canceled. " +
                                            "The Order Item with id: " + shipmentOrderItem.getPk().getAsString() + "have status of " + shipmentOrderItem.getStatus());
									if (amazonOrder) {
										paymentCaptureResult.setAmazonResponse(new AmazonResponse());
	                                } else {
	    								if (affirmOrder){
	    									((AffirmPaymentCaptureResult)paymentCaptureResult).setAffirmResponse(new AffirmResponse());
	    								} else {
	    									paymentCaptureResult.setCreditCardResponse(new CreditCardResponse());
	    								}
	    							}
                                    paymentCaptureResultList.add(paymentCaptureResult);
                                    isCaptureAllowed = false;
                                    break;
                                }
                            }
                            if(!isCaptureAllowed){
                            	continue;
                            }
                        }

                        // Check for Site information
                        if (ActiveSite.getSite()== null){
                            ActiveSite.setSite(order.getSite());
                        }else if (!order.getSite().getCode().equals(ActiveSite.getSite().getCode())){
                            ActiveSite.setSite(order.getSite());
                        }
                        
                        // Capture payment for this OrderShipment.
                    	paymentCaptureResult = paymentCaptureManager.capturePayment(orderShipment, false, true);
                    	paymentCaptureResultList.add(paymentCaptureResult);
                    }
                }
            }
        }
        
        return paymentCaptureResultList;
	}

}
