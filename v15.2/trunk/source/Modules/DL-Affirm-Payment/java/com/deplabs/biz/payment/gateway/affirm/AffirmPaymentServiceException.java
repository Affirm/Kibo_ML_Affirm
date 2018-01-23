package com.deplabs.biz.payment.gateway.affirm;

/**
 * 
 * @author horacioa
 *
 */
public class AffirmPaymentServiceException extends RuntimeException {

	private static final long serialVersionUID = 4508629767148291984L;

	public AffirmPaymentServiceException() {
		super();
	}

	public AffirmPaymentServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public AffirmPaymentServiceException(String message) {
		super(message);
	}

	public AffirmPaymentServiceException(Throwable cause) {
		super(cause);
	}
	
}
