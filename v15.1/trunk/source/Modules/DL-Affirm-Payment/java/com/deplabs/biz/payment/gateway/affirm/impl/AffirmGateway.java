package com.deplabs.biz.payment.gateway.affirm.impl;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.marketlive.system.config.multisite.ISiteAwareConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentServiceClient;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentServiceConfig;
import com.deplabs.biz.payment.gateway.affirm.AffirmPaymentServiceException;
import com.deplabs.biz.payment.gateway.affirm.AffirmRequest;
import com.deplabs.biz.payment.gateway.affirm.AffirmResponse;
import com.deplabs.biz.payment.gateway.affirm.IAffirmPaymentGateway;
import com.marketlive.system.annotation.PlatformComponent;

/**
 * Implementation of different types of payment transactions with the Affirm payment gateway.
 * @author horacioa
 *
 */
@PlatformComponent
public class AffirmGateway implements IAffirmPaymentGateway {
	
	 /** Logger for this class. */
    private static Logger logger = LoggerFactory.getLogger(AffirmGateway.class);
    
    public static final String PROPERTIES_PATH = "custom.affirmpayment_";
    public static final String PREFIX_TO_REMOVE = "custom.affirmpayment_prefix_to_remove"; //For 16.1 is 'custom.' | For 14.2 is 'custom.affirmpayment_'
    public static final String GATEWAY_NAME = "custom.affirmpayment_gateway_name";

    /** The name of the payment gateway. */
    private String paymentGatewayName;
    
    @Autowired
    protected ISiteAwareConfigurationManager configurationManager;
    
    protected AffirmPaymentServiceConfig serviceConfig;
    protected AffirmPaymentServiceClient serviceClient;

    @SuppressWarnings("rawtypes")
	protected Properties getServiceConfigProperties() {
        Properties properties = new Properties();
        Map map = configurationManager.getPropertiesForPartialPath(PROPERTIES_PATH);
        String prefixToRemove = configurationManager.getAsString(PREFIX_TO_REMOVE);
        if (StringUtils.isEmpty(prefixToRemove)) {
        	prefixToRemove = PROPERTIES_PATH;
        }
        for (Object object : map.keySet()) {
            Object value = map.get(object);
            if (StringUtils.isNotBlank(value.toString())) {
                String property = object.toString().replaceFirst(prefixToRemove, "");
                properties.setProperty(property, value.toString());
            }
        }
        return properties;
    }

    @PostConstruct
    public void init() {
        if (configurationManager != null) {
            paymentGatewayName = configurationManager.getAsString(GATEWAY_NAME);
        }

        serviceConfig = new AffirmPaymentServiceConfig(getServiceConfigProperties());
        serviceClient = new AffirmPaymentServiceClient(this.serviceConfig);
    }

	@Override
	public AffirmResponse authorizePayment(AffirmRequest request) {
		AffirmResponse authorizationResponse = performAuthorization(request);
		return authorizationResponse;
	}

	private AffirmResponse performAuthorization(AffirmRequest affirmRequest) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.

        try {
        	affirmResponse = serviceClient.authorize(affirmRequest);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Authorize Payment Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        

        if (affirmResponse != null){
            logger.debug("The authorization response is :" + affirmResponse.toString());

            prepareAuthorizationResponse(affirmRequest, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareAuthorizationResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());
		
		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		} else {
			if (StringUtils.isNotBlank((String) affirmResponse.getField(AffirmResponse.STATUS))) {
				affirmResponse.setReasonCode((String) affirmResponse.getField(AffirmResponse.STATUS));
			}
		}
	}

	protected void prepareErrorResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		if (StringUtils.isNotBlank((String) affirmResponse.getField(AffirmResponse.ERROR_CODE))) {
			affirmResponse.setResponseCode((String) affirmResponse.getField(AffirmResponse.ERROR_CODE));
		}
		if (StringUtils.isNotBlank((String) affirmResponse.getField(AffirmResponse.ERROR_MESSAGE))) {
			affirmResponse.setResponseMessage((String) affirmResponse.getField(AffirmResponse.ERROR_MESSAGE));
		}
		if (StringUtils.isNotBlank((String) affirmResponse.getField(AffirmResponse.ERROR_TYPE))) {
			affirmResponse.setReasonCode((String) affirmResponse.getField(AffirmResponse.ERROR_TYPE));
		}
	}

	@Override
	public AffirmResponse capturePayment(AffirmRequest request) {
		AffirmResponse affirmResponse = performCapture(request);
		return affirmResponse;
	}

	private AffirmResponse performCapture(AffirmRequest request) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

        try {
        	affirmResponse = serviceClient.capture(request);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Capture Payment Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        
        if (affirmResponse != null){
            logger.debug("The capture response is :" + affirmResponse.toString());

            prepareCaptureResponse(request, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareCaptureResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());
		
		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		}
	}

	@Override
	public AffirmResponse refundPayment(AffirmRequest request) {
		AffirmResponse affirmResponse = performRefundPayment(request);
		return affirmResponse;
	}

	private AffirmResponse performRefundPayment(AffirmRequest request) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

        try {
        	affirmResponse = serviceClient.refundPayment(request);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Refund Payment Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        
        if (affirmResponse != null){
            logger.debug("The refund response is :" + affirmResponse.toString());

            prepareRefundResponse(request, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareRefundResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());
		
		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		}
	}

	@Override
	public AffirmResponse voidPayment(AffirmRequest request) {
		AffirmResponse affirmResponse = performVoidPayment(request);
		return affirmResponse;
	}
	
	private AffirmResponse performVoidPayment(AffirmRequest request) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

        try {
        	affirmResponse = serviceClient.voidPayment(request);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Void Payment Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        
        if (affirmResponse != null){
            logger.debug("The void response is :" + affirmResponse.toString());

            prepareVoidResponse(request, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareVoidResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());
		
		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		}
	}

	@Override
	public AffirmResponse update(AffirmRequest request) {
		AffirmResponse affirmResponse = performUpdatePayment(request);
		return affirmResponse;
	}

	private AffirmResponse performUpdatePayment(AffirmRequest request) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

        try {
        	affirmResponse = serviceClient.update(request);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Update Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        
        if (affirmResponse != null){
            logger.debug("The update response is :" + affirmResponse.toString());

            prepareUpdateResponse(request, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareUpdateResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		}
	}

	@Override
	public AffirmResponse read(AffirmRequest request) {
		AffirmResponse affirmResponse = performReadPayment(request);
		return affirmResponse;
	}

	private AffirmResponse performReadPayment(AffirmRequest request) {
		AffirmResponse affirmResponse = new AffirmResponse();
		affirmResponse.setSuccess(false); // Initially set to false and assume that transaction failed.
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());

        try {
        	affirmResponse = serviceClient.read(request);
        } catch (AffirmPaymentServiceException e) {
			logger.error("Exception from Affirm Read Service :: ", e);
            affirmResponse.setSuccess(false);
        }
        
        if (affirmResponse != null){
            logger.debug("The read response is :" + affirmResponse.toString());

            prepareReadResponse(request, affirmResponse);
        }

        return affirmResponse;
	}

	protected void prepareReadResponse(AffirmRequest affirmRequest, AffirmResponse affirmResponse) {
		affirmResponse.setPaymentGateway(this.getPaymentGatewayName());
		
		if (!affirmResponse.isSuccess()) {
			this.prepareErrorResponse(affirmRequest, affirmResponse);
		}
	}

	@Override
	public String getPaymentGatewayName() {
		return paymentGatewayName;
	}


}
