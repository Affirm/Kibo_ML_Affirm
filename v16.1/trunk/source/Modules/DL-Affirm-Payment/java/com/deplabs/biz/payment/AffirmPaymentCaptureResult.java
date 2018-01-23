/**
 * 
 */
package com.deplabs.biz.payment;

import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.payment.PaymentCaptureResult;

/**
 * @author horacioa
 *
 */
public class AffirmPaymentCaptureResult extends PaymentCaptureResult {
	
	private AffirmResponse affirmResponse;

	public void setAffirmResponse(AffirmResponse affirmResponse) {
		this.affirmResponse = affirmResponse;
	}

	public AffirmResponse getAffirmResponse() {
		return affirmResponse;
	}

}
