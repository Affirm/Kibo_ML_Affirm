/**
 * 
 */
package com.deplabs.biz.payment.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deplabs.biz.payment.IPaymentVoidJobPreTask;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * Sample implementation of {@link IPaymentVoidJobPreTask} that does nothing
 * 
 * @author horacio
 */
@PlatformComponent
public class SamplePaymentVoidJobPreTask implements IPaymentVoidJobPreTask {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(SamplePaymentVoidJobPreTask.class);
	
	public void executePaymentVoidJobPreTask() {
		if (logger.isDebugEnabled()) {
			logger.debug("Executing sample payment void job pre task/extension.");
		}
	}

}
