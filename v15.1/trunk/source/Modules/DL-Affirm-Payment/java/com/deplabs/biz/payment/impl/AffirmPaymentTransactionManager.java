package com.deplabs.biz.payment.impl;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/


import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.deplabs.biz.payment.IAffirmPaymentTransactionManager;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.deplabs.biz.payment.gateway.affirm.IAffirmPaymentGateway;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.dao.payment.ICaptureDAO;
import com.marketlive.dao.payment.IPaymentTransactionDAO;
import com.marketlive.dao.payment.IRefundDAO;
import com.marketlive.dao.payment.IVoidDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.domain.payment.Capture;
import com.marketlive.domain.payment.PaymentTransaction;
import com.marketlive.domain.payment.Refund;
import com.marketlive.domain.payment.VoidTransaction;
import com.marketlive.system.annotation.PlatformService;

/**
 * Transaction Manager for Affirm Payment actions
 * 
 * @author horacioa
 * 
 */
@PlatformService
public class AffirmPaymentTransactionManager implements IAffirmPaymentTransactionManager {

	/** Logger for this class. */
	private static Logger logger = LoggerFactory.getLogger(AffirmPaymentTransactionManager.class);
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Autowired
    private IAffirmPaymentGateway affirmPaymentGateway;

	// Reference to the payment transaction DAO
	@Resource(name = "paymentTransactionDAO")
	private IPaymentTransactionDAO paymentTransactionDAO;

    // Reference to the authorizationDAO
	@Resource(name = "authorizationDAO")
	private IAuthorizationDAO authorizationDAO;

    // Reference to captureDAO
	@Resource(name = "captureDAO")
	private ICaptureDAO captureDAO;

    // Reference to refundDAO
	@Resource(name = "refundDAO")
	private IRefundDAO refundDAO;

    // Reference to voidDAO
	@Resource(name = "voidDAO")
	private IVoidDAO voidDAO;


	public AffirmResponse authorizePayment(AffirmRequest affirmRequest, int customerID) {
    	AffirmResponse affirmResponse = affirmPaymentGateway.authorizePayment(affirmRequest);

        if(affirmResponse != null) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();
            paymentTransaction.setMerchantReferenceCode(affirmResponse.getMerchantExternalReference());
            paymentTransaction.setTransactionType(affirmRequest.getRequestType().getName());
			if (customerID > 0) {
				paymentTransaction.setAccountCustomerID(customerID);
			}

            paymentTransaction.setRequestID(affirmResponse.getChargeId());
            paymentTransaction.setApproved(affirmResponse.isSuccess());
            paymentTransaction.setPaymentGateway(affirmResponse.getPaymentGateway());
            paymentTransaction.setResponseCode(affirmResponse.getResponseCode());
            paymentTransaction.setReasonCode(affirmResponse.getReasonCode());
            Long transactionID = (Long)paymentTransactionDAO.save(paymentTransaction);

			// if the auth was successful, write auth data to the AUTH table
			if (affirmResponse.isSuccess()) {
				
				// Special Case for Status 200: If authorized amount is invalid
				// Message to the user that the payment failed
				// Redirect user to payment method selection screen in the checkout flow
				if (affirmResponse.getAmount() != null && !affirmResponse.getAmount().equals(affirmResponse.getAuthHold())) {
					affirmResponse.setSuccess(false);
					affirmResponse.setResponseMessage("The authorized amount is invalid");
					return affirmResponse;
				}

                Authorization authorization = new Authorization();

                authorization.setRequestID(affirmResponse.getChargeId());

				if (customerID > 0) {
					authorization.setAccountCustomerID(customerID);
				}

				// get the authorized amount from the response object, if available
				if (affirmResponse.getAuthHold() != null) {
					authorization.setAmountAuthorized((new Float(affirmResponse.getAuthHold())/100) + "");
				}

				// get the auth amount requested, from the response object, if available
				if (affirmResponse.getAmount() != null) {
					authorization.setAuthorizationAmountRequested(new Float(affirmResponse.getAmount())/100 + "");
				}
                
                // get the currency of the auth amount from the response object, if available
                if(affirmResponse.getCurrency() != null) {
                    authorization.setAuthorizationRequestCurrency(affirmResponse.getCurrency());
                }

                //String authCode = affirmResponse.getAuthorizationCode();
                //authorization.setAuthorizationCode(authCode);

                //authorization.setAuthorizationReconciliationID(affirmResponse.getAuthorizationReconciliationID());

                // this auth has not been used for a capture yet (since we
                // just did the auth request now)
                authorization.setAuthorizationUsed(false);

                paymentTransaction = paymentTransactionDAO.findById(transactionID, false);
                authorization.setPaymentTransaction(paymentTransaction);
                authorizationDAO.save(authorization);
            }
        }
        return affirmResponse;
    }

	public AffirmResponse capturePayment(AffirmRequest affirmRequest, int customerID, long orderId, long orderShipmentId) {

        AffirmResponse affirmResponse = affirmPaymentGateway.capturePayment(affirmRequest);

        if(affirmResponse != null) {

            PaymentTransaction paymentTransaction = new PaymentTransaction();
            //paymentTransaction.setMerchantReferenceCode(affirmRequest.getCheckoutToken());
            paymentTransaction.setTransactionType(affirmRequest.getRequestType().getName());
            if(customerID > 0) {
                paymentTransaction.setAccountCustomerID(customerID);
            }
            paymentTransaction.setRequestID(affirmResponse.getChargeId());

            paymentTransaction.setApproved(affirmResponse.isSuccess());
            paymentTransaction.setPaymentGateway(affirmPaymentGateway.getPaymentGatewayName());
            paymentTransaction.setTransactionType(affirmRequest.getRequestType().getName());
            paymentTransaction.setResponseCode(affirmResponse.getResponseCode());
            paymentTransaction.setReasonCode(affirmResponse.getReasonCode());
            Long transactionID = (Long)paymentTransactionDAO.save(paymentTransaction);

            // if the capture was successful, write capture data to the CAPTURE table
            if(affirmResponse.isSuccess()) {
                Capture capture = new Capture();

                capture.setRequestID(affirmResponse.getChargeId());
				if (customerID > 0) {
					capture.setAccountCustomerID(customerID);
				}
				if (affirmResponse.getAmount() != null) {
					capture.setAmountCaptured((new Float(affirmResponse.getAmount())/100) + "");
				}

                //capture.setAuthorizationCode(affirmRequest.getCheckoutToken());
                capture.setCaptureReconciliationID(affirmResponse.getTransactionId());

                // set order and shipment IDs
                capture.setOrderId(orderId);
                capture.setOrderShipmentId(orderShipmentId);

                paymentTransaction = paymentTransactionDAO.findById(transactionID, false);
                capture.setPaymentTransaction(paymentTransaction);
                captureDAO.save(capture);
            }
        }

        return affirmResponse;
    }

	public AffirmResponse refundPayment(AffirmRequest affirmRequest, int customerID) {

        AffirmResponse affirmResponse = affirmPaymentGateway.refundPayment(affirmRequest);

        if(affirmResponse != null) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();
            //paymentTransaction.setMerchantReferenceCode(affirmRequest.getCheckoutToken());
            paymentTransaction.setTransactionType(affirmRequest.getRequestType().getName());
            if(customerID > 0) {
                paymentTransaction.setAccountCustomerID(customerID);
            }

            paymentTransaction.setRequestID(affirmResponse.getChargeId());
            paymentTransaction.setApproved(affirmResponse.isSuccess());
            paymentTransaction.setResponseCode(affirmResponse.getResponseCode());
            paymentTransaction.setReasonCode(affirmResponse.getReasonCode());
            paymentTransaction.setPaymentGateway(affirmResponse.getPaymentGateway());
            Long transactionID = (Long)paymentTransactionDAO.save(paymentTransaction);

            // if the refund was successful, write refund data to the REFUND table
            if(affirmResponse.isSuccess()) {

                Refund refund = new Refund();

                refund.setRequestID(affirmResponse.getChargeId());
                if(customerID > 0) {
                    refund.setAccountCustomerID(customerID);
                }
                if (affirmResponse.getAmount() != null) {
                	refund.setAmountRefunded((new Float(affirmResponse.getAmount())/100) + "");
                }
                refund.setRefundReconciliationID(affirmResponse.getTransactionId());

                paymentTransaction = paymentTransactionDAO.findById(transactionID, false);
                refund.setPaymentTransaction(paymentTransaction);
                refundDAO.save(refund);
            }
        }

        return affirmResponse;
    }
    
	public AffirmResponse voidPayment(AffirmRequest affirmRequest, int customerID) {
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        //paymentTransaction.setMerchantReferenceCode(affirmRequest.getRequestType().getName());
        paymentTransaction.setTransactionType(affirmRequest.getRequestType().getName());
        if(customerID > 0) {
            paymentTransaction.setAccountCustomerID(customerID);
        }
        AffirmResponse affirmResponse = affirmPaymentGateway.voidPayment(affirmRequest);

        if(affirmResponse != null) {
        	paymentTransaction.setRequestID(affirmResponse.getChargeId());
            paymentTransaction.setApproved(affirmResponse.isSuccess());
            paymentTransaction.setResponseCode(affirmResponse.getResponseCode());
            paymentTransaction.setReasonCode(affirmResponse.getReasonCode());
            paymentTransaction.setPaymentGateway(affirmResponse.getPaymentGateway());
            Long transactionID = (Long)paymentTransactionDAO.save(paymentTransaction);

            // if the void request was successful, write void data to the VOID table
            if(affirmResponse.isSuccess()) {
                VoidTransaction voidTransaction = new VoidTransaction();

                voidTransaction.setRequestID(affirmResponse.getTransactionId());
                if(customerID > 0) {
                    voidTransaction.setAccountCustomerID(customerID);
                }

                voidTransaction.setVoidType(affirmRequest.getRequestType().getName());
                voidTransaction.setAmountVoided(affirmRequest.getAmount());
                voidTransaction.setVoidDateTime(getDate(affirmResponse.getCreated()));

                paymentTransaction = paymentTransactionDAO.findById(transactionID, false);
                voidTransaction.setPaymentTransaction(paymentTransaction);
                voidDAO.save(voidTransaction);
            }
        }
        return affirmResponse;
    }
	
	/**
	 * Get the date in the format "2014-03-18T19:20:30Z" in a
	 * {@link java.util.Date}
	 * 
	 * @param created
	 * @return
	 */
	private Date getDate(String created) {
		if (StringUtils.isNotBlank(created)) {
			try {
				sdf.parse(created);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

}