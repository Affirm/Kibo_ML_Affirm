/**
 * 
 */
package com.deplabs.biz.payment;

import java.util.List;

import org.marketlive.entity.cart.order.IOrderShipment;

/**
 * @author horacioa
 *
 */
public interface IPaymentVoidManager {
	
	String NAME = "paymentVoidManager";

	/**
	 * Void payment for all order shipments that satisfy the following conditions:
	 * <ul>
	 *   <li>OrderShipment have a status value that allows payment to be voided.</li>
	 *   <li>OrderShipment were created during the specified date range.</li>
	 * </ul>
	 * 
	 * @return the list of {@link PaymentVoidResult} objects
	 */
	List<PaymentVoidResult> voidPayment();
	
	/**
	 * Void payment for the given {@link IOrderShipment}.
	 * 
	 * @param orderShipment the {@link IOrderShipment} object
	 * @return the {@link PaymentVoidResult} object
	 */
	PaymentVoidResult voidPayment(IOrderShipment orderShipment);

}
