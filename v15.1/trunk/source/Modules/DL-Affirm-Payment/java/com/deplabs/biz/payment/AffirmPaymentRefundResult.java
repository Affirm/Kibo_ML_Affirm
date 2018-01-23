/**
 * 
 */
package com.deplabs.biz.payment;

import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.payment.PaymentRefundResult;

/**
 * @author horacioa
 *
 */
public class AffirmPaymentRefundResult extends PaymentRefundResult {
	
	private AffirmResponse affirmResponse;

	public void setAffirmResponse(AffirmResponse affirmResponse) {
		this.affirmResponse = affirmResponse;
	}

	public AffirmResponse getAffirmResponse() {
		return affirmResponse;
	}
	
}
