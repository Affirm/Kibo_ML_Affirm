/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
 */

package com.deplabs.biz.payment;

import java.util.List;

/**
 * Interface for extensions that needs to be executed after executing payment
 * void job.
 * 
 * @author horacioa
 * 
 */
public interface IPaymentVoidJobPostTask {

	/**
	 * Represents an extension that needs to be executed after executing payment
	 * void job.
	 * 
	 * @param paymentVoidList
	 *            the list of {@link PaymentVoidResult} objects
	 */
	void executePaymentVoidJobPostTask(List<PaymentVoidResult> paymentVoidList);
}
