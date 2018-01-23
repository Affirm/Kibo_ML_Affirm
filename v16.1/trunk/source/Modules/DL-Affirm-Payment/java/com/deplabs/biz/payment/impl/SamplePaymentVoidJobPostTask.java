/**
 * 
 */
package com.deplabs.biz.payment.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deplabs.biz.payment.IPaymentVoidJobPostTask;
import com.deplabs.biz.payment.PaymentVoidResult;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * Sample implementation of {@link IPaymentVoidJobPostTask} that does nothing
 * 
 * @author horacio
 */
@PlatformComponent
public class SamplePaymentVoidJobPostTask implements IPaymentVoidJobPostTask {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(SamplePaymentVoidJobPreTask.class);
	
	public void executePaymentVoidJobPostTask(List<PaymentVoidResult> paymentVoidList) {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing sample payment void job post task/extension.");
		}

	}

}
