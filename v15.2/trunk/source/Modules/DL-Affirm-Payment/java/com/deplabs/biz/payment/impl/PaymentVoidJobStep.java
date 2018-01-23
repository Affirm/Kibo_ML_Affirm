/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

package com.deplabs.biz.payment.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.marketlive.system.config.IConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.deplabs.biz.payment.IPaymentVoidJobPostTask;
import com.deplabs.biz.payment.IPaymentVoidJobPreTask;
import com.deplabs.biz.payment.IPaymentVoidManager;
import com.deplabs.biz.payment.PaymentVoidResult;
import com.marketlive.scheduler.controller.IJobStep;
import com.marketlive.scheduler.controller.MLJobController;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * An implementation of {@link IJobStep} responsible for voiding amounts of order shipments.
 * 
 * <p>
 * This class delegates actual processing of the payment void job/task to {@link IPaymentVoidManager}.
 * 
 * <p>
 * This job step is provided as a default implementation for scheduling a job that voids the amount.
 * To change how amount is voided, create new implementation of {@link IPaymentVoidManager} and inject
 * it into this class.
 *
 *
 */
@PlatformComponent
public class PaymentVoidJobStep implements IJobStep {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(PaymentVoidJobStep.class);

    protected static final String PRE_TASK_CONFIG_PATH = "custom.payment_PaymentVoidJobStep_preTasks";
    protected static final String POST_TASK_CONFIG_PATH = "custom.payment_PaymentVoidJobStep_postTasks";

	/** Reference to {@link IPaymentVoidManager}. */
    @Autowired
	private IPaymentVoidManager paymentVoidManager;

    @Autowired
    IConfigurationManager configurationManager;
	
	/** List of {@link IPaymentVoidJobPreTask} instances. They will be executed before processing payment voids. */
    @Autowired
	private List<IPaymentVoidJobPreTask> paymentVoidJobPreTasks = new ArrayList<IPaymentVoidJobPreTask>();
	
	/** List of {@link IPaymentVoidJobPostTask} instances. They will be executed after processing payment voids. */
    @Autowired
	private List<IPaymentVoidJobPostTask> paymentVoidJobPostTasks = new ArrayList<IPaymentVoidJobPostTask>();
	

    @SuppressWarnings("unchecked")
	@PostConstruct
    public void init(){
        // validate the injected IPaymentVoidJobPreTask List against what has been configured and enforce the ordering
        Map<String, String> configuredPreTasks = (Map<String,String>)configurationManager.getProperty(PRE_TASK_CONFIG_PATH);
        if (configuredPreTasks != null) {
            List<IPaymentVoidJobPreTask> orderedPaymentVoidJobPreTasks = new ArrayList<IPaymentVoidJobPreTask>();
            for (IPaymentVoidJobPreTask injectedPreTask : paymentVoidJobPreTasks) {
                String pathAndClassName = injectedPreTask.getClass().getName();
                String className =  pathAndClassName.substring(pathAndClassName.lastIndexOf(".")+1);
                if ((configuredPreTasks.containsKey(className))) {
                    int index = Integer.parseInt(configuredPreTasks.get(className));
                    orderedPaymentVoidJobPreTasks.add(index-1, injectedPreTask);
                }
            }
            paymentVoidJobPreTasks = orderedPaymentVoidJobPreTasks;
        }

        // validate the injected IPaymentVoidJobPostTask List against what has been configured and enforce the ordering
        Map<String, String> configuredPostTasks = (Map<String,String>)configurationManager.getProperty(POST_TASK_CONFIG_PATH);
        if (configuredPostTasks != null) {
            List<IPaymentVoidJobPostTask> orderedPaymentVoidJobPostTasks = new ArrayList<IPaymentVoidJobPostTask>();
            for (IPaymentVoidJobPostTask injectedPostTask : paymentVoidJobPostTasks) {
                String pathAndClassName = injectedPostTask.getClass().getName();
                String className =  pathAndClassName.substring(pathAndClassName.lastIndexOf(".")+1);
                if ((configuredPostTasks.containsKey(className))) {
                    int index = Integer.parseInt(configuredPostTasks.get(className));
                    orderedPaymentVoidJobPostTasks.add(index-1, injectedPostTask);
                }
            }
            paymentVoidJobPostTasks = orderedPaymentVoidJobPostTasks;
        }
    }


	@SuppressWarnings("unchecked")
	@Override
	public Object executeStep(Object inputParm) {
		logger.debug("executeStep");
		logger.debug("BEGIN : Payment Void Job.");
		
		Hashtable<String, String> returnCode = new Hashtable<String, String>();
		
		List<PaymentVoidResult> paymentVoidResultList = null;
		
		try {
			// Execute payment Void job pre tasks/extensions.
			for (IPaymentVoidJobPreTask paymentVoidJobPreTask : paymentVoidJobPreTasks) {
				paymentVoidJobPreTask.executePaymentVoidJobPreTask();
			}
			
			paymentVoidResultList = paymentVoidManager.voidPayment();
			
			returnCode.put(MLJobController.NOTIFICATION_KEY_STATUS, MLJobController.NOTIFICATION_MSG_TYPE_SUCCESS);
	        returnCode.put(MLJobController.NOTIFICATION_KEY_MESSAGE, "Payment Void Job completed successfully.");
		} catch (Exception exception) {
			returnCode.put(MLJobController.NOTIFICATION_KEY_STATUS, MLJobController.NOTIFICATION_MSG_TYPE_ERROR);
	        returnCode.put(MLJobController.NOTIFICATION_KEY_MESSAGE, "An exception occurred while executing Payment Void Job - " + exception.getMessage());
	        logger.error(exception.getMessage());
	        exception.printStackTrace();
		}
        
		if (paymentVoidResultList != null) {
			logger.debug("Payment Void Job Result :: ");
			
			for (PaymentVoidResult paymentVoidResult : paymentVoidResultList) {
                if (paymentVoidResult != null){
                    logger.debug("Order Shipment Id : " + paymentVoidResult.getOrderShipmentId() +
                            " Result Code : " + paymentVoidResult.getPaymentVoidResultCode() +
                            " Result Message : " + paymentVoidResult.getResultMessage());
                }

			}
			
			// Execute payment Void job post tasks/extensions.
			for (IPaymentVoidJobPostTask paymentVoidJobPostTask : paymentVoidJobPostTasks) {
				paymentVoidJobPostTask.executePaymentVoidJobPostTask(paymentVoidResultList);
			}
		}
		
        logger.debug("END : Payment Void Job.");
        
        return returnCode;
	}
}
