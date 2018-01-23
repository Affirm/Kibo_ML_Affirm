/**
 * 
 */
package com.deplabs.biz.payment.impl;

import org.marketlive.entity.cart.order.IOrderShipment;

import com.marketlive.biz.payment.PaymentCaptureResult;

/**
 * @author horacioa
 *
 */
public interface IAffirmPaymentCaptureManager {
	
	PaymentCaptureResult capturePayment(IOrderShipment orderShipment, boolean isCCOMsettlement, boolean isJobProcessing, boolean isRealTimeCapture, String affirmRequestId);

}
