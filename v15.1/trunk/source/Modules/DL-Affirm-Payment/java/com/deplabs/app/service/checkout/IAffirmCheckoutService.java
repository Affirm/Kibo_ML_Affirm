package com.deplabs.app.service.checkout;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.deplabs.affirm.app.b2c.checkout.IAffirmCheckoutModel;
import com.marketlive.app.b2c.checkout.ICheckoutModel;
import com.marketlive.app.service.IServiceContext;
import com.marketlive.app.service.checkout.ICheckoutService;
import com.marketlive.app.service.checkout.ICheckoutServiceContext;

public interface IAffirmCheckoutService extends ICheckoutService {

	public IAffirmCheckoutModel getAffirmCheckoutModel(ICheckoutServiceContext serviceContext);
	public void getAffirmPaymentTargetData(HttpServletRequest request, HttpServletResponse response, List targetDataForUpdate, ICheckoutServiceContext serviceContext) throws Exception;
	public List<Object> getPaymentInfo(IServiceContext serviceContext);
	public ICheckoutModel processViewAffirmError(HttpServletRequest request, HttpServletResponse response, ICheckoutServiceContext serviceContext) throws Exception;
}
