package com.deplabs.app.service.checkout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.marketlive.app.service.checkout.IPaymentService;
import com.marketlive.app.service.checkout.IPaymentServiceContext;
import com.marketlive.app.service.checkout.impl.PaymentServiceResponse;

public interface IAffirmPaymentService extends IPaymentService {

	public static final String AFFIRM_CHECKOUT_TOKEN = "checkout_token";
	public static final String AFFIRM_CHECKOUT_SUCCESS = "CHECKOUT_SUCCESS";
	public static final String AFFIRM_CHECKOUT_FAIL = "CHECKOUT_FAIL";
	public static final String AFFIRM_CHECKOUT_CANCEL = "CHECKOUT_CANCEL";
	public static final String AFFIRM_PAYMENT_TYPE =  "AFFIRM";
	public static final String AFFIRM_PAYMENT_METHOD =  "AFFIRM_YES";
	public static final String AFFIRM_CHEKOUT_INFO = "AFFIRM_CHEKOUT_INFO";
	public static final String AFFIRM_FORWARD ="AFFIRM_FORWARD";
	
	public static final String STEP_ERROR = "AFFIRM_STEP_ERROR";
    public static final String STEP_ERROR_CODE = "AFFIRM_STEP_ERROR_CODE";
	
	
	public PaymentServiceResponse processConfirm(HttpServletRequest request, HttpServletResponse response, IPaymentServiceContext paymentServiceContext) throws Exception;
	public PaymentServiceResponse processCancel(HttpServletRequest request, HttpServletResponse response, IPaymentServiceContext paymentServiceContext) throws Exception;
}
