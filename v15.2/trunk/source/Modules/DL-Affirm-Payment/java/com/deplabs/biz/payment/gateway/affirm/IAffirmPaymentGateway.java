/**
 * 
 */
package com.deplabs.biz.payment.gateway.affirm;

/**
 * Interface for payment gateway operations against Affirm Platform.
 * 
 * @author horacioa
 * 
 */
public interface IAffirmPaymentGateway {

	/**
	 * Processes the specified authorization request and returns corresponding
	 * response information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse authorizePayment(AffirmRequest request);

	/**
	 * Processes the specified payment capture request and returns corresponding
	 * response information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse capturePayment(AffirmRequest request);

	/**
	 * Processes the specified refund request and returns corresponding response
	 * information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse refundPayment(AffirmRequest request);

	/**
	 * Processes the specified void request (of a previous authorized
	 * transaction) and returns corresponding response information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse voidPayment(AffirmRequest request);

	/**
	 * Processes the specified Update Shipping Information Request and returns
	 * corresponding response information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse update(AffirmRequest request);

	/**
	 * Processes the specified Reading a Charge Request and returns
	 * corresponding response information
	 * 
	 * @param request
	 * @return AffirmResponse
	 */
	AffirmResponse read(AffirmRequest request);

	/**
	 * Returns the name of the payment gateway that processes these payment
	 * operations
	 * 
	 * @return String
	 */
	String getPaymentGatewayName();

}
