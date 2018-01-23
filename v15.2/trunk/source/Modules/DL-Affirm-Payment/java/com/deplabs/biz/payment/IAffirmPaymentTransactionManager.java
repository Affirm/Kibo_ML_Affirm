package com.deplabs.biz.payment;

import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;

/*
(C) Copyright MarketLive. 2014. All rights reserved.
MarketLive is a trademark of MarketLive, Inc.
Warning: This computer program is protected by copyright law and international treaties.
Unauthorized reproduction or distribution of this program, or any portion of it, may result
in severe civil and criminal penalties, and will be prosecuted to the maximum extent
possible under the law.
*/

public interface IAffirmPaymentTransactionManager {

    /**
     * Executes the Affirm authorization request and returns a response object
     * containing response data received from the Affirm
     *
     * @param   affirmRequest
     * @param   customerID
     *
     * @return  AffirmResponse
     */
    AffirmResponse authorizePayment(AffirmRequest affirmRequest, int customerID);

    /**
     * Executes a capture request and returns a response object
     * containing response data received from the Affirm
     *
     * @param affirmRequest
     * @param customerID
     *
     * @return  AffirmResponse
     */
    AffirmResponse capturePayment(AffirmRequest affirmRequest, int customerID, long orderId, long orderShipmentId);

    /**
     * Executes a refund request of a payment and returns a response object
     * containing response data received from the Affirm
     *
     * @param affirmRequest
     * @param customerID
     *
     * @return  AffirmResponse
     */
    AffirmResponse refundPayment(AffirmRequest affirmRequest, int customerID);
    
    /**
     * Executes a refund request of a payment and returns a response object
     * containing response data received from the Affirm
     *
     * @param affirmRequest
     * @param customerID
     *
     * @return  AffirmResponse
     */
    AffirmResponse voidPayment(AffirmRequest affirmRequest, int customerID);

}
