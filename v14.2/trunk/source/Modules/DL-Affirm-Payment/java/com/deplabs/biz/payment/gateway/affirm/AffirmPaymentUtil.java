/**
 * 
 */
package com.deplabs.biz.payment.gateway.affirm;

import java.util.List;

import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.marketlive.entity.cart.order.IOrderPayment;

/**
 * Utility class for processing Affirm Payments
 * @author horacioa
 *
 */
public class AffirmPaymentUtil {
	
	public static String AFFIRM_PAYMENT_TYPE = "AFFIRM";

    /**
     * Returns true if payment is through Affirm Payment Gateway else returns false.
     *
     * @param orderPaymentList list of {@link IOrderPayment} object
     * @return
     */
    public static boolean isPaymentThroughAffirm(List<IOrderPayment> orderPaymentList) {
        if (orderPaymentList == null) {
            throw new IllegalArgumentException("Null orderPaymentList passed to isPaymentThroughAffirm method.");
        }

        for (IOrderPayment orderPayment : orderPaymentList) {
            if (orderPayment != null && orderPayment.getPaymentType() != null) {
                if (AFFIRM_PAYMENT_TYPE.equals(orderPayment.getPaymentType().getName())) {
                    return true;
                }
            }
        }

        return false;
    }

	/**
	 * Returns {@link IOrderPayment} object from the list of given
	 * {@link IOrderPayment} objects. If payment is not from Affirm then this
	 * method returns null.
	 * 
	 * @param orderPaymentList
	 *            list of {@link IOrderPayment} object
	 * @return
	 * 
	 */
	public static IOrderPaymentAffirm getAffirmPayment(List<IOrderPayment> orderPaymentList) {
		if (orderPaymentList == null) {
			throw new IllegalArgumentException("Null orderPaymentList passed to getAffirmPayment method.");
		}

		for (IOrderPayment orderPayment : orderPaymentList) {
			if (orderPayment.getPaymentType().getName().equals(AFFIRM_PAYMENT_TYPE)) {
				return (IOrderPaymentAffirm) orderPayment;
			}
		}

		return null;
	}

}
