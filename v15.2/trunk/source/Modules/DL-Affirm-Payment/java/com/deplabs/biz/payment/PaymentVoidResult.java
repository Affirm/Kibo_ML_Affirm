/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

package com.deplabs.biz.payment;

import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.marketlive.biz.payment.AmazonResponse;
import com.marketlive.biz.payment.CreditCardResponse;

/**
 * This class holds the response of the void transaction along with custom result code and result message.
 *
 */
public class PaymentVoidResult {
	
	/**
	 * Enumeration representing result code for the void transaction.
	 */
	public enum PaymentVoidResultCode {
		SUCCESS,
		FAILURE
	}

	private String orderShipmentId;
	
	private PaymentVoidResultCode paymentVoidResultCode;
	
	private String resultMessage;
	
	private CreditCardResponse creditCardResponse;

    private AmazonResponse amazonResponse;
    
    private AffirmResponse affirmResponse;

	/**
	 * @return the orderShipmentId
	 */
	public String getOrderShipmentId() {
		return orderShipmentId;
	}

	/**
	 * @param orderShipmentId the orderShipmentId to set
	 */
	public void setOrderShipmentId(String orderShipmentId) {
		this.orderShipmentId = orderShipmentId;
	}

	/**
	 * @return the resultMessage
	 */
	public String getResultMessage() {
		return resultMessage;
	}

	/**
	 * @param resultMessage the resultMessage to set
	 */
	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	/**
	 * @return the creditCardResponse
	 */
	public CreditCardResponse getCreditCardResponse() {
		return creditCardResponse;
	}

	/**
	 * @param creditCardResponse the creditCardResponse to set
	 */
	public void setCreditCardResponse(CreditCardResponse creditCardResponse) {
		this.creditCardResponse = creditCardResponse;
	}

    /**
     * @return the amazonResponse
     */
    public AmazonResponse getAmazonResponse(){
        return amazonResponse;
    }

    /**
     * @param amazonResponse the amazonResponse to set
     */
    public void setAmazonResponse(AmazonResponse amazonResponse) {
        this.amazonResponse = amazonResponse;
    }

	public void setPaymentVoidResultCode(PaymentVoidResultCode paymentVoidResultCode) {
		this.paymentVoidResultCode = paymentVoidResultCode;
	}

	public PaymentVoidResultCode getPaymentVoidResultCode() {
		return paymentVoidResultCode;
	}

	public void setAffirmResponse(AffirmResponse affirmResponse) {
		this.affirmResponse = affirmResponse;
	}

	public AffirmResponse getAffirmResponse() {
		return affirmResponse;
	}
}
