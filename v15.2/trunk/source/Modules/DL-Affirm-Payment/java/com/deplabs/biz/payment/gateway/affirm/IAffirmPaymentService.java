/**
 * 
 */
package com.deplabs.biz.payment.gateway.affirm;

/**
 * @author horacioa
 * 
 */
public interface IAffirmPaymentService {

	/**
	 * This method creates and authorizes a Charge Request. To create and
	 * authorize a charge you will need to use the checkout_token that was
	 * POSTed to the user_confirmation_url. The response is a newly created JSON
	 * charge object, which is identified by its id field. This id will be used
	 * as the charge_id parameter in subsequent requests. Upon receiving an
	 * authorized charge, you should check that the charge object corresponds to
	 * a valid order in your system. If it is valid, you may process the order
	 * and capture the charge. Otherwise, you should void the charge.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws Affirmpaymentserviceexception
	 */
	AffirmResponse authorize(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;

	/**
	 * Captures a charge. Here chargeID denotes the id of the charge object you
	 * received in the authorization request. The response is a charge capture
	 * event.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws AffirmPaymentServiceException
	 */
	AffirmResponse capture(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;

	/**
	 * Voids an authorized but uncaptured charge. Here chargeID denotes the id
	 * of the charge object you received in the authorization request. The
	 * response is a charge void event.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws AffirmPaymentServiceException
	 */
	AffirmResponse voidPayment(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;

	/**
	 * Refunds a captured charge. Here chargeID denotes the id of the charge
	 * object you received in the authorization request. The response is a
	 * charge refund event.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws AffirmPaymentServiceException
	 */
	AffirmResponse refundPayment(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;
	
	/**
	 * Updates order_id and shipping confirmation data on a charge. Here
	 * charge_id denotes the id of the charge object you received in the
	 * authorization request. The response is a charge update event. Supplying
	 * shipping confirmation and carrier details helps reduce chargebacks in
	 * cases where customers dispute receipt of their order. The orderID field
	 * is useful when generating settlement reports.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws AffirmPaymentServiceException
	 */
	AffirmResponse update(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;
	
	/**
	 * Reads a charge. Here chargeID denotes the id of the charge object you
	 * received in the authorization request. The response is a JSON charge
	 * object.
	 * 
	 * @param affirmRequest
	 * @return
	 * @throws AffirmPaymentServiceException
	 */
	AffirmResponse read(AffirmRequest affirmRequest) throws AffirmPaymentServiceException;
	
}
