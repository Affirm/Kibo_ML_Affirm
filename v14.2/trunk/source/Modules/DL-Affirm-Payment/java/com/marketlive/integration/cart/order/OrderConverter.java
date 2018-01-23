package com.marketlive.integration.cart.order;

/*
 (C) Copyright MarketLive. 2006. All rights reserved.
 MarketLive is a trademark of MarketLive, Inc.
 Warning: This computer program is protected by copyright law and international treaties.
 Unauthorized reproduction or distribution of this program, or any portion of it, may result
 in severe civil and criminal penalties, and will be prosecuted to the maximum extent
 possible under the law.
*/

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.marketlive.biz.cart.order.IOrderManager;
import org.marketlive.biz.cart.order.IOrderQueueManager;
import org.marketlive.entity.IEntity;
import org.marketlive.entity.IPrimaryKey;
import org.marketlive.entity.account.ICustomer;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderBillShipInfo;
import org.marketlive.entity.cart.order.IOrderBillShipInfoHome;
import org.marketlive.entity.cart.order.IOrderDiscount;
import org.marketlive.entity.cart.order.IOrderHome;
import org.marketlive.entity.cart.order.IOrderPayment;
import org.marketlive.entity.cart.order.IOrderPaymentCheck;
import org.marketlive.entity.cart.order.IOrderPaymentCreditCard;
import org.marketlive.entity.cart.order.IOrderPaymentGiftCertificate;
import org.marketlive.entity.cart.order.IOrderPaymentHome;
import org.marketlive.entity.cart.order.IOrderPaymentPayPal;
import org.marketlive.entity.cart.order.IOrderQueue;
import org.marketlive.entity.cart.order.IOrderShipment;
import org.marketlive.entity.cart.order.IOrderShipmentHome;
import org.marketlive.entity.cart.order.IOrderTracking;
import org.marketlive.entity.site.ISite;
import org.marketlive.entity.site.ISiteHome;
import org.marketlive.system.config.IConfigurationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deplabs.affirm.integration.cart.order.OrderPaymentAffirmConverter;
import com.marketlive.biz.cart.order.ILineItemCancellationManager;
import com.marketlive.biz.cart.order.IOrderStatusUpdateEmailProcessor;
import com.marketlive.dao.payment.IAuthorizationDAO;
import com.marketlive.domain.payment.Authorization;
import com.marketlive.entity.cart.order.OrderTracking;
import com.marketlive.entity.cart.order.OrderUpdatePage;
import com.marketlive.entity.currency.Amount;
import com.marketlive.integration.Constants;
import com.marketlive.integration.EntityConverter;
import com.marketlive.integration.IntegrationException;
import com.marketlive.integration.account.CustomerConverter;
import com.marketlive.integration.cart.order.OrderBillShipInfoConverter;
import com.marketlive.integration.cart.order.OrderDiscountConverter;
import com.marketlive.integration.cart.order.OrderPaymentCheckConverter;
import com.marketlive.integration.cart.order.OrderPaymentConverter;
import com.marketlive.integration.cart.order.OrderPaymentCreditCardConverter;
import com.marketlive.integration.cart.order.OrderPaymentGiftCertificateConverter;
import com.marketlive.integration.cart.order.OrderPaymentPayPalConverter;
import com.marketlive.integration.cart.order.OrderShipmentConverter;
import com.marketlive.integration.cart.order.OrderTrackingConverter;
import com.marketlive.integration.xmlbean.OrderBillShipInfoXBean;
import com.marketlive.integration.xmlbean.OrderDiscountXBean;
import com.marketlive.integration.xmlbean.OrderPaymentAffirmXBean;
import com.marketlive.integration.xmlbean.OrderPaymentCheckXBean;
import com.marketlive.integration.xmlbean.OrderPaymentCreditCardXBean;
import com.marketlive.integration.xmlbean.OrderPaymentGiftCertificateXBean;
import com.marketlive.integration.xmlbean.OrderPaymentPayPalXBean;
import com.marketlive.integration.xmlbean.OrderShipmentXBean;
import com.marketlive.integration.xmlbean.OrderTrackingXBean;
import com.marketlive.integration.xmlbean.OrderXBean;
import com.marketlive.system.locale.LocaleManager;

/**
 * Helper class for transfering data between an Order XML bean object
 * and an Order entity object.
 *
 * @author chwang
 */
@Component
public class OrderConverter extends EntityConverter {
    private static Log log = LogFactory.getLog(OrderConverter.class);

    @Autowired
    private ISiteHome siteHome;
    @Autowired
    private IOrderHome orderHome;
    @Autowired
    private IOrderBillShipInfoHome billToHome;
    @Autowired
    private IOrderShipmentHome orderShipmentHome;
    @Autowired
    private IOrderPaymentHome orderPaymentHome;
    @Autowired
    private OrderBillShipInfoConverter billInfoConverter;
    @Autowired
    private OrderShipmentConverter shipmentConverter;
    @Autowired
    private OrderPaymentCreditCardConverter paymentCreditCardConverter;
    @Autowired
    private OrderPaymentPayPalConverter paymentPayPalConverter;
    @Autowired
    private OrderPaymentGiftCertificateConverter paymentGiftCertificateConverter;
    @Autowired
    private OrderPaymentCheckConverter paymentCheckConverter;
    @Autowired
    private OrderPaymentConverter paymentConverter;
    @Autowired
    private OrderPaymentAffirmConverter paymentAffirmConverter;
    @Autowired
    private CustomerConverter customerConverter;
    /** dependency injected. */
    private int amountTypePrecision = 2;
    @Autowired
    private OrderTrackingConverter orderTrackingConverter;
    @Autowired
    private OrderDiscountConverter orderDiscountConverter;
    @Autowired
    private IOrderManager orderManager;
    @Autowired
    private IAuthorizationDAO authorizationDAO;
    @Autowired
    private IOrderQueueManager orderQueueManager;
    @Autowired
    private ILineItemCancellationManager lineItemCancellationManager;
    @Autowired
    private IOrderStatusUpdateEmailProcessor orderUpdateConfirmationProcessor;
    @Autowired
    IConfigurationManager configurationManager;

    @PostConstruct
    public void init(){
        amountTypePrecision = configurationManager.getAsInt("system.integration.amountType.display.precision", 2);
    }

    /**
     * Return an reference to the OrderQueueManager.
     * @return an instance of OrderQueueManager
     */
    public IOrderQueueManager getOrderQueueManager() {
        return orderQueueManager;
    }
    /**
     *  Setter for the OrderQueueManager.
      * @param orderQueueManager - injection done via Spring.
     */
    public void setOrderQueueManager(IOrderQueueManager orderQueueManager) {
        this.orderQueueManager = orderQueueManager;
    }
    /** Dependency injection. */
    public void setSiteHome(final ISiteHome pSiteHome) {
        this.siteHome = pSiteHome;
    }

    /**
     * Sets the OrderHome object.
     *
     * @param pOrderHome OrderHome object to set.
     */
    public void setOrderHome(final IOrderHome pOrderHome) {
        this.orderHome = pOrderHome;
    }

    /**
     * Sets the OrderBillShipInfoHome object.
     *
     * @param pBillToHome OrderBillShipInfoHome to set
     */
    public void setBillToHome(final IOrderBillShipInfoHome pBillToHome) {
        this.billToHome = pBillToHome;
    }

    /**
     * Sets the OrderShipmentHome object.
     *
     * @param pOrderShipmentHome OrderShipmentHome object to set
     */
    public void setOrderShipmentHome(final IOrderShipmentHome pOrderShipmentHome) {
        this.orderShipmentHome = pOrderShipmentHome;
    }

    /**
     * Sets the OrderPaymentHome object.
     *
     * @param pOrderPaymentHome OrderPaymentHome object to set
     */
    public void setOrderPaymentHome(final IOrderPaymentHome pOrderPaymentHome) {
        this.orderPaymentHome = pOrderPaymentHome;
    }

    /**
     * Sets the OrderBillShipInfoConverter object.
     *
     * @param pBillInfoConverter OrderBillShipInfoConverter object to set
     */
    public void setBillInfoConverter(final OrderBillShipInfoConverter pBillInfoConverter) {
        this.billInfoConverter = pBillInfoConverter;
    }

    /**
     * Sets the OrderShipmentConverter object.
     *
     * @param pShipmentConverter OrderShipmentConverter to set
     */
    public void setShipmentConverter(final OrderShipmentConverter pShipmentConverter) {
        this.shipmentConverter = pShipmentConverter;
    }

    /**
     * Sets the OrderPaymentCheckConverter object.
     *
     * @param pPaymentCheckConverter OrderPaymentCheckConverter to set
     */
    public void setPaymentCheckConverter(final OrderPaymentCheckConverter pPaymentCheckConverter) {
        this.paymentCheckConverter = pPaymentCheckConverter;
    }

    /**
     * Sets the OrderPaymentCreditCardConverter object.
     *
     * @param pPaymentCreditCardConverter OrderPaymentCreditCardConverter to set
     */
    public void setPaymentCreditCardConverter(final OrderPaymentCreditCardConverter pPaymentCreditCardConverter) {
        this.paymentCreditCardConverter = pPaymentCreditCardConverter;
    }

    /**
     * Sets the OrderPaymentPayPalConverter object.
     *
     * @param pPaymentPayPalConverter OrderPaymentPayPalConverter to set
     */
    public void setPaymentPayPalConverter(final OrderPaymentPayPalConverter pPaymentPayPalConverter) {
        this.paymentPayPalConverter = pPaymentPayPalConverter;
    }

    /**
     * Sets the OrderPaymentGiftCertificateConverter object.
     *
     * @param pPaymentGiftCertificateConverter OrderPaymentGiftCertificateConverter to set
     */
    public void setPaymentGiftCertificateConverter(final OrderPaymentGiftCertificateConverter pPaymentGiftCertificateConverter) {
        this.paymentGiftCertificateConverter = pPaymentGiftCertificateConverter;
    }

    /**
     * Sets the OrderPaymentConverter object.
     *
     * @param pPaymentConverter OrderPaymentConverter to set
     */
    public void setPaymentConverter(final OrderPaymentConverter pPaymentConverter) {
        this.paymentConverter = pPaymentConverter;
    }

    /**
     * Sets the CustomerConverter object.
     *
     * @param pCustomerConverter CustomerConverter to set
     */
    public void setCustomerConverter(final CustomerConverter pCustomerConverter) {
        this.customerConverter = pCustomerConverter;
    }

    /**
     * Sets the amount type precision.
     *
     * @param pAmountTypePrecision the amount type precision to set
     */
    public void setAmountTypePrecision(int pAmountTypePrecision) {
        this.amountTypePrecision = pAmountTypePrecision;
    }

    /**
     * Sets the OrderTrackingConverter object.
     *
     * @param pOrderTrackingConverter the orderTrackingConverter to set
     */
    public void setOrderTrackingConverter(final OrderTrackingConverter pOrderTrackingConverter) {
        this.orderTrackingConverter = pOrderTrackingConverter;
    }

    /**
     * Sets the OrderDiscountConverter object.
     *
     * @param pOrderDiscountConverter The orderDiscountConverter to set.
     */
    public void setOrderDiscountConverter(final OrderDiscountConverter pOrderDiscountConverter) {
        this.orderDiscountConverter = pOrderDiscountConverter;
    }

    /**
     * Sets the OrderManager object.
     *
     * @param pOrderManager The orderManager to set.
     */
    public void setOrderManager(final IOrderManager pOrderManager) {
        this.orderManager = pOrderManager;
    }

    /**
     * Sets the <code>AuthorizationDAO</code> reference.
     * 
     * @param authorizationDAO the authorizationDAO to set
     */
    public void setAuthorizationDAO(IAuthorizationDAO authorizationDAO) {
        this.authorizationDAO = authorizationDAO;
    }    
    
    /**
     * Sets the {@link ILineItemCancellationManager} object.
     *
     * @param lineItemCancellationManager the {@link ILineItemCancellationManager}
     */
    public void setLineItemCancellationManager(ILineItemCancellationManager lineItemCancellationManager) {
		this.lineItemCancellationManager = lineItemCancellationManager;
	}

    /**
     * Sets the {@link IOrderStatusUpdateEmailProcessor} property.
     *
     * @param orderUpdateConfirmationProcessor the {@link IOrderStatusUpdateEmailProcessor}
     */
    public void setOrderUpdateConfirmationProcessor(IOrderStatusUpdateEmailProcessor orderUpdateConfirmationProcessor) {
        this.orderUpdateConfirmationProcessor = orderUpdateConfirmationProcessor;
    }

    /**
     * Copies data from the given entity object to the XBean object.
     *
     * @param pEntity the source entity
     * @param pXBean the target XmlBean
     */
    @Override
    public void entityToXBean(final IEntity pEntity,
            final XmlObject pXBean, final Locale pLocale) {

        //
        // Export inherited Entity properties.
        //
        super.entityToXBean(pEntity, pXBean, pLocale);

        //  Type cast entity and XML bean
        IOrder pOrderEntity = (IOrder) pEntity;
        OrderXBean pOrderXBean = (OrderXBean) pXBean;
        Calendar c = null;

        //
        // Order properties.
        //
        if ((pOrderEntity.getSite()) != null && (pOrderEntity.getSite().getCode() != null)) {
            pOrderXBean.setSiteCode(pOrderEntity.getSite().getCode());
        }

        pOrderXBean.setLocale(pOrderEntity.getLocale().toString());

        if ((pOrderEntity.getCustomer()) != null && (pOrderEntity.getCustomer().getCode() != null)) {
            pOrderXBean.setCustomerCode(pOrderEntity.getCustomer().getCode());
        }else {
            //throw new IntegrationException("Export Guard Rail:  No customer found for order with code = " + pOrderEntity.getCode());
        	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_CUSTOMER, "Export Guard Rail:  No customer found for order with code = " + pOrderEntity.getCode());
        	ie.getErrorFields().put("Customer","");
        	throw ie;
        	
        }

        if (pOrderEntity.getStatus() != null) {
            pOrderXBean.setStatus(pOrderEntity.getStatus());
        }

        if (pOrderEntity.getBillToInfo() != null) {
            OrderBillShipInfoXBean billToInfoXBean = pOrderXBean.addNewBillToInfo();
            billInfoConverter.entityToXBean(pOrderEntity.getBillToInfo(), billToInfoXBean);
        }else {
            //throw new IntegrationException("Export Guard Rail:  No billing info found for order with code = " + pOrderEntity.getCode());
        	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_BILLING, "Export Guard Rail:  No billing info found for order with code = " + pOrderEntity.getCode());
        	ie.getErrorFields().put("BillToInfo","");
        	throw ie;
        }

        if (pOrderEntity.getSubTotal() != null) {
            pOrderXBean.setSubTotal(pOrderEntity.getSubTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getTaxTotal() != null) {
            pOrderXBean.setTaxTotal(pOrderEntity.getTaxTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }
        if (pOrderEntity.getShippingTotal() != null) {
            pOrderXBean.setShippingTotal(pOrderEntity.getShippingTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }
        if (pOrderEntity.getShippingCostTotal() != null) {
            pOrderXBean.setShippingCostTotal(pOrderEntity.getShippingCostTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getShippingMethodTotal() != null) {
            pOrderXBean.setShippingMethodTotal(pOrderEntity.getShippingMethodTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getShippingWeightTotal() != null) {
            pOrderXBean.setShippingWeightTotal(pOrderEntity.getShippingWeightTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getShippingLocationTotal() != null) {
            pOrderXBean.setShippingLocationTotal(pOrderEntity.getShippingLocationTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getShippingStateTotal() != null) {
            pOrderXBean.setShippingStateTotal(pOrderEntity.getShippingStateTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getGiftWrapTotal() != null) {
            pOrderXBean.setGiftWrapTotal(pOrderEntity.getGiftWrapTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getAdditionalAddressTotal() != null) {
            pOrderXBean.setAdditionalAddressTotal(pOrderEntity.getAdditionalAddressTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getAdditionalChargesTotal() != null) {
            pOrderXBean.setAdditionalChargesTotal(pOrderEntity.getAdditionalChargesTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getWeightSurchargeTotal() != null) {
            pOrderXBean.setWeightSurchargeTotal(pOrderEntity.getWeightSurchargeTotal().toBigDecimal().setScale(
                    amountTypePrecision, BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getTotal() != null) {
            pOrderXBean.setTotal(pOrderEntity.getTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getMerchandiseTotal() != null) {
            pOrderXBean.setMerchandiseTotal(pOrderEntity.getMerchandiseTotal().toBigDecimal().setScale(amountTypePrecision,
                    BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getInvoiceNumber() != null) {
            pOrderXBean.setInvoiceNumber(pOrderEntity.getInvoiceNumber());
        }

        if (pOrderEntity.getComment1() != null) {
            pOrderXBean.setComment1(pOrderEntity.getComment1());
        }

        if (pOrderEntity.getComment2() != null) {
            pOrderXBean.setComment2(pOrderEntity.getComment2());
        }

        pOrderXBean.setDeferred(pOrderEntity.isDeferred());
        pOrderXBean.setMultipleAddresses(pOrderEntity.isMultipleAddresses());

        if (pOrderEntity.getSourceCodesAsString() != null) {
            pOrderXBean.setSourceCodesAsString(pOrderEntity.getSourceCodesAsString());
        }
        
        if (pOrderEntity.getSingleUseCouponsAsString() != null) {
            pOrderXBean.setSingleUseCouponsAsString(pOrderEntity.getSingleUseCouponsAsString());
        }        

        if (pOrderEntity.getCampaignSourceCode() != null) {
            pOrderXBean.setCampaignSourceCode(pOrderEntity.getCampaignSourceCode());
        }

        pOrderXBean.setDeleted(pOrderEntity.isDeleted());

        if (pOrderEntity.getDateOrdered() != null) {
            if (c == null) {
                c = Calendar.getInstance();
            }
            c.setTime(pOrderEntity.getDateOrdered());
            pOrderXBean.setDateOrdered(c);
        }

        if (pOrderEntity.getDateDeleted() != null) {
            if (c == null) {
                c = Calendar.getInstance();
            }
            c.setTime(pOrderEntity.getDateDeleted());
            pOrderXBean.setDateDeleted(c);
        }

        pOrderXBean.setRefund(pOrderEntity.isRefund());
        if(pOrderEntity.getRefundAmount() != null) {
        	pOrderXBean.setRefundAmount(pOrderEntity.getRefundAmount().toBigDecimal().setScale(amountTypePrecision,
                BigDecimal.ROUND_HALF_UP));
        }

        if (pOrderEntity.getShipments() != null && pOrderEntity.getShipments().size() > 0) {
            OrderXBean.Shipments shipmentsXBean = pOrderXBean.addNewShipments();
            for (Iterator iter = pOrderEntity.getShipments().iterator(); iter.hasNext();) {
                IOrderShipment shipment = (IOrderShipment) iter.next();
                if (shipment == null) {
            		//throw new IntegrationException("Export Guard Rail:  No shipment found for order with code = " + pOrderEntity.getCode());
                	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_SHIPMENT, "Export Guard Rail:  No shipment found for order with code = " + pOrderEntity.getCode());
                	ie.getErrorFields().put("Shipment","");
                	throw ie;
                }
                if (shipment.getItems() == null || shipment.getItems().size() == 0) {
            		//throw new IntegrationException("Export Guard Rail:  No shipment items found for order with code = " + pOrderEntity.getCode() + " and shipment = " + shipment.getCode());
                	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_SHIPMENT_ITEMS, "Export Guard Rail:  No shipment items found for order with code = " + pOrderEntity.getCode() + " and shipment = " + shipment.getCode());
                	ie.getErrorFields().put("Shipment", shipment.getCode());
                	throw ie;
                }
                if (shipment.getShippingMethod() == null) {
            		//throw new IntegrationException("Export Guard Rail:  No shipping method found for order with code = " + pOrderEntity.getCode() + " and shipment = " + shipment.getCode());
                	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_SHIPPING_METHOD, "Export Guard Rail:  No shipping method found for order with code = " + pOrderEntity.getCode() + " and shipment = " + shipment.getCode());
                	ie.getErrorFields().put("Shipment", shipment.getCode());
                	throw ie;
                }
                OrderShipmentXBean shipmentXBean = shipmentsXBean.addNewOrderShipment();
                shipmentConverter.entityToXBean(shipment, shipmentXBean, pLocale);
            }
        }else {
            //throw new IntegrationException("Export Guard Rail:  No shipments found for order with code = " + pOrderEntity.getCode());
        	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_SHIPMENT, "Export Guard Rail:  No shipment found for order with code = " + pOrderEntity.getCode());
        	ie.getErrorFields().put("Shipment","");
        	throw ie;
        }

        if (pOrderEntity.getPayments() != null && pOrderEntity.getPayments().size() > 0) {
            OrderXBean.Payments paymentsXBean = pOrderXBean.addNewPayments();
            for (Iterator iter = pOrderEntity.getPayments().iterator(); iter.hasNext();) {
                IOrderPayment payment = (IOrderPayment) iter.next();
                if (payment == null) {
                	//throw new IntegrationException("Export Guard Rail:  No payment found for order with code = " + pOrderEntity.getCode());
                	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_PAYMENT, "Export Guard Rail:  No payment found for order with code = " + pOrderEntity.getCode());
                	ie.getErrorFields().put("Payment","");
                	throw ie;
                }
                OrderXBean.Payments.Payment paymentXBean = paymentsXBean.addNewPayment();

                if (payment instanceof IOrderPaymentCreditCard) {
                    OrderPaymentCreditCardXBean xBean = paymentXBean.addNewOrderPaymentCreditCard();
                    xBean = paymentCreditCardConverter.entityToXBean((IOrderPaymentCreditCard) payment, xBean);
                } else if (payment instanceof IOrderPaymentGiftCertificate) {
                    OrderPaymentGiftCertificateXBean xBean = paymentXBean.addNewOrderPaymentGiftCertificate();
                    xBean = paymentGiftCertificateConverter.entityToXBean((IOrderPaymentGiftCertificate) payment, xBean);
                } else if (payment instanceof IOrderPaymentCheck) {
                    OrderPaymentCheckXBean xBean = paymentXBean.addNewOrderPaymentCheck();
                    xBean = paymentCheckConverter.entityToXBean((IOrderPaymentCheck) payment, xBean);
                } else if (payment instanceof IOrderPaymentPayPal) {
                    OrderPaymentPayPalXBean xBean = paymentXBean.addNewOrderPaymentPayPal();
                    xBean = paymentPayPalConverter.entityToXBean((IOrderPaymentPayPal) payment, xBean);
                } else if(payment instanceof IOrderPaymentAffirm){
                    OrderPaymentAffirmXBean xBean = paymentXBean.addNewOrderPaymentAffirm();
                    xBean = paymentAffirmConverter.entityToXBean((IOrderPaymentAffirm) payment, xBean);
                }
            }
        }else {
            //throw new IntegrationException("Export Guard Rail:  No payments found for order with code = " + pOrderEntity.getCode());
        	IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_PAYMENT, "Export Guard Rail:  No payments found for order with code = " + pOrderEntity.getCode());
        	ie.getErrorFields().put("Payment","");
        	throw ie;
        }

        //MLPB-10098
        List<Authorization> authorizations = null;
        authorizations = authorizationDAO.findCreditCardAuthorizationByOrderID(new Long(pOrderEntity.getPk().getAsString()));
        
        if (authorizations != null && authorizations.size() > 0) {
            OrderXBean.AuthorizationsRequests authorizationsRequests = pOrderXBean.addNewAuthorizationsRequests();
            for (Iterator iter = authorizations.iterator(); iter.hasNext();) {
	            XmlObject authorizationsRequest = authorizationsRequests.addNewAuthorizationsRequest();
	            XmlCursor cursor = authorizationsRequest.newCursor();
	            cursor.toFirstContentToken();           
                Authorization authorization = (Authorization) iter.next();
                addElement(cursor, new QName(Constants.MARKETLIVE_INTEGRATION_NS, "authRequestId"),authorization.getRequestID());
                addElement(cursor, new QName(Constants.MARKETLIVE_INTEGRATION_NS, "authCode"),authorization.getAuthorizationCode());
                Map customFields = authorization.getCustomFields();
                if (customFields.size() > 0) {
                    Set entries = customFields.entrySet();
                    Iterator i = entries.iterator();
                    while (i.hasNext()){
                        Map.Entry m = (Map.Entry)i.next();
                        addElement(cursor, new QName(Constants.MARKETLIVE_INTEGRATION_NS, (String)m.getKey()),(String)m.getValue());
                    }
                }
            }
        }
        //MLPB-10098        

    	if (pOrderEntity.getItems() == null || pOrderEntity.getItems().size() == 0)  {
            //throw new IntegrationException("Export Guard Rail:  No order items found for order with code = " + pOrderEntity.getCode());
    		IntegrationException ie = new IntegrationException(Constants.ORDER_EXPORT_NO_ORDER_ITEM, "Export Guard Rail:  No order items found for order with code = " + pOrderEntity.getCode());
        	ie.getErrorFields().put("OrderItem","");
        	throw ie;
    	}
        
        if (pOrderEntity.getTrackings() != null && pOrderEntity.getTrackings().size() > 0) {
            OrderXBean.Trackings trackingsXBean = pOrderXBean.addNewTrackings();
            Iterator iter = pOrderEntity.getTrackings().iterator();
            while (iter.hasNext()) {
                IOrderTracking tracking = (IOrderTracking) iter.next();
                OrderTrackingXBean trackingXBean = trackingsXBean.addNewOrderTracking();
                trackingXBean = orderTrackingConverter.entityToXBean(tracking, trackingXBean);
            }
        }

        List orderDiscounts = orderManager.findOrderDiscounts(pOrderEntity.getPk());
        if (orderDiscounts != null && orderDiscounts.size() > 0) {
            OrderXBean.Discounts discountsXBean = pOrderXBean.addNewDiscounts();
            Iterator iter = orderDiscounts.iterator();
            while (iter.hasNext()) {
                IOrderDiscount orderDiscount = (IOrderDiscount) iter.next();
                OrderDiscountXBean discountXBean = discountsXBean.addNewDiscount();
                orderDiscountConverter.entityToXBean(orderDiscount, discountXBean);
            }
        }
        //BEGIN PEBL-11184 Start here
        String orderProviderType = pOrderEntity.getOrderProviderType();
        if(orderProviderType != null && !orderProviderType.isEmpty()) {
        	pOrderXBean.setOrderProviderType(orderProviderType);
        }
        //BEGIN PEBL-11184 End here
        Integer borderFreeOrderFlag = pOrderEntity.getBorderFreeOrderFlag();
        if(borderFreeOrderFlag != null) {
        	pOrderXBean.setBorderFreeOrderFlag(borderFreeOrderFlag);
        }
        if (pOrderEntity.getDateBorderFreeOrderConfirmation() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(pOrderEntity.getDateBorderFreeOrderConfirmation());
            pOrderXBean.setDateBorderFreeOrderConfirmation(cal);
        }
        Integer orderConfAttempt = pOrderEntity.getBorderFreeOrderConfAttempt();
        if(orderConfAttempt != null) {
        	pOrderXBean.setBorderFreeOrderConfAttempt(orderConfAttempt.intValue());
        }        
        //Begin - Added code for Order Queue API tasks after an order export - PCI-DSS compliance changes.//
        IPrimaryKey orderId = pOrderEntity.getPk();
        try {
            Collection orderQueueItems = orderQueueManager.getOrderQueueItems(orderId.getAsString());
            IOrderQueue orderQueue = null;
            if (orderQueueItems != null && orderQueueItems.size() > 1) {
                log.error("More than one order found in the order queue for the order id "+orderId.getAsString());
                log.error("Hence Order Quene not removed & CC_NUMBER not cleard");
            }
            else if (orderQueueItems != null && orderQueueItems.size() == 1){
                orderQueue = (IOrderQueue) orderQueueItems.iterator().next();
                IPrimaryKey orderQueuePk = orderQueue.getPk();
                orderQueueManager.removeOrderFromQueue(orderQueuePk);
                //orderQueueManager.performPreProcessChecks(orderId);
                orderQueueManager.performPostProcessChecks(orderId);
                //orderQueueManager.clearCreditCardNumber(orderId);            
            }
        }
        catch (Exception e) {
            log.error("Failed to remove order from queue / wipe out credit card number form the order payment credit card for Order: " + orderId, e);
        }
        //End - Added code for Order Queue API tasks after an order export - PCI-DSS compliance changes.//
    }

    protected static void addElement(final XmlCursor pCursor,
            final QName pName, final String pValue) {
        if (pValue != null && pValue.length() > 0) {
            pCursor.insertElementWithText(pName, pValue);
        }
    }    

    /**
     * Copy attributes from the XML data bean to the given entity bean.
     * @param pXBean the target XmlBean
     * @param pEntity the source entity
     */
    @Override
    public void xbeanToEntity(final XmlObject pXBean,
            final IEntity pEntity) {

        //
        // Import inherited Entity properties.
        //
        super.xbeanToEntity(pXBean, pEntity);

        OrderXBean orderXBean = (OrderXBean) pXBean;
        IOrder order = (IOrder) pEntity;

        //
        // Import Order properties.
        //

        importSite(orderXBean, order);

        if (orderXBean.isSetLocale()) {
            order.setLocale(LocaleManager.parseLocaleString(orderXBean.getLocale()));
        } else {
            order.setLocale(Locale.US);
        }

        importCustomer(orderXBean, order);

        if (orderXBean.isSetStatus()) {
        	// Order Cancellation Logic:
        	// Business Rule 1 : Order status received during import should be 'Canceled' for Order Cancellation.
        	if ((orderXBean.getStatus() != null) && (orderXBean.getStatus().equalsIgnoreCase("Canceled"))) {
        		// Business Rule 2 : Current status of the Order should be 'Ordered' or 'Warehouse' for Order Cancellation.
        		if ((order.getStatus().equalsIgnoreCase("Ordered")) || (order.getStatus().equalsIgnoreCase("Warehouse"))) {
        			String orderId = order.getPk().getAsString();
        			if (lineItemCancellationManager != null) {
        				lineItemCancellationManager.processOrderCancellation(orderId);
        			}
        		}
        	}
            // Guardrails to not override order with canceled status
            if (order.getStatus() != null && orderXBean.getStatus().equalsIgnoreCase("Canceled")){
                if ( orderXBean.getStatus() != null && !orderXBean.getStatus().equalsIgnoreCase("Canceled")){
                    log.warn("An attempt has been made to update status of Order : " + order.getPk() +  " with Canceled status to status : " + orderXBean.getStatus());
                }
            } else{
                order.setStatus(orderXBean.getStatus());
            }
        }

        if (orderXBean.isSetAdditionalAddressTotal()) {
            order.setAdditionalAddressTotal(new Amount(orderXBean.getAdditionalAddressTotal()));
        }

        if (orderXBean.isSetAdditionalChargesTotal()) {
            order.setAdditionalChargesTotal(new Amount(orderXBean.getAdditionalChargesTotal()));
        }

        if (orderXBean.isSetCampaignSourceCode()) {
            order.setCampaignSourceCode(orderXBean.getCampaignSourceCode());
        }

        if (orderXBean.isSetComment1()) {
            order.setComment1(orderXBean.getComment1());
        }

        if (orderXBean.isSetComment2()) {
            order.setComment2(orderXBean.getComment2());
        }

        if (orderXBean.isSetDateDeleted()) {
            order.setDateDeleted(orderXBean.xgetDateDeleted().getDateValue());
        }

        if (orderXBean.isSetDateOrdered()) {
            order.setDateOrdered(orderXBean.xgetDateOrdered().getDateValue());
        }

        if (orderXBean.isSetDeferred()) {
            order.setDeferred(orderXBean.getDeferred());
        }

        if (orderXBean.isSetDeleted()) {
            order.setDeleted(orderXBean.getDeleted());
        }

        if (orderXBean.isSetGiftWrapTotal()) {
            order.setGiftWrapTotal(new Amount(orderXBean.getGiftWrapTotal()));
        }

        if (orderXBean.isSetInvoiceNumber()) {
            order.setInvoiceNumber(orderXBean.getInvoiceNumber());
        }

        if (orderXBean.isSetMultipleAddresses()) {
            order.setMultipleAddresses(orderXBean.getMultipleAddresses());
        }

        if (orderXBean.isSetShippingCostTotal()) {
            order.setShippingCostTotal(new Amount(orderXBean.getShippingCostTotal()));
        }

        if (orderXBean.isSetShippingLocationTotal()) {
            order.setShippingLocationTotal(new Amount(orderXBean.getShippingLocationTotal()));
        }

        if (orderXBean.isSetShippingMethodTotal()) {
            order.setShippingMethodTotal(new Amount(orderXBean.getShippingMethodTotal()));
        }

        if (orderXBean.isSetShippingStateTotal()) {
            order.setShippingStateTotal(new Amount(orderXBean.getShippingStateTotal()));
        }

        if (orderXBean.isSetShippingTotal()) {
            order.setShippingTotal(new Amount(orderXBean.getShippingTotal()));
        }

        if (orderXBean.isSetShippingWeightTotal()) {
            order.setShippingWeightTotal(new Amount(orderXBean.getShippingWeightTotal()));
        }

        if (orderXBean.isSetSourceCodesAsString()) {
            order.setSourceCodesAsString(orderXBean.getSourceCodesAsString());
        }

        if (orderXBean.isSetTaxTotal()) {
            order.setTaxTotal(new Amount(orderXBean.getTaxTotal()));
        }

        if (orderXBean.isSetSubTotal()) {
            order.setSubTotal(new Amount(orderXBean.getSubTotal()));
        }

        if (orderXBean.isSetWeightSurchargeTotal()) {
            order.setWeightSurchargeTotal(new Amount(orderXBean.getWeightSurchargeTotal()));
        }

        if (orderXBean.isSetTotal()) {
            order.setTotal(new Amount(orderXBean.getTotal()));
        }

        if (orderXBean.isSetMerchandiseTotal()) {
            order.setMerchandiseTotal(new Amount(orderXBean.getMerchandiseTotal()));
        }
        if (orderXBean.isSetRefund()) {
            order.setRefund(orderXBean.getRefund());
        }
        if (orderXBean.isSetRefundAmount()) {
        	order.setRefundAmount(new Amount(orderXBean.getRefundAmount()));
        }
        
        importBillToInfo(orderXBean, order);
        importShipments(orderXBean, order);
        importPayments(orderXBean, order);
        importTrackings(orderXBean, order);
        importDiscounts(orderXBean, order);

        //BEGIN PEBL-11184 Start here
        String orderProviderType = orderXBean.getOrderProviderType();
        if(orderProviderType != null && !orderProviderType.isEmpty()) {
        	order.setOrderProviderType(orderProviderType);
        }
        //BEGIN PEBL-11184 End here 
        Integer borderFreeOrderFlag = orderXBean.getBorderFreeOrderFlag();
        if(borderFreeOrderFlag != null) {
        	order.setBorderFreeOrderFlag(borderFreeOrderFlag);
        }       

        if (orderXBean.isSetDateBorderFreeOrderConfirmation()) {
            order.setDateBorderFreeOrderConfirmation(orderXBean.xgetDateBorderFreeOrderConfirmation().getDateValue());
        }        
        Integer orderConfAttempt = orderXBean.getBorderFreeOrderConfAttempt();
        if(orderConfAttempt != null) {
        	order.setBorderFreeOrderConfAttempt(orderConfAttempt.intValue());
        }
        // Adjust order payment in case of line item cancellation

        // Send Order Update in case of partial shipment or line item cancellation
        if (orderUpdateConfirmationProcessor != null){
            OrderUpdatePage orderUpdatePage = (OrderUpdatePage) order.getPage("orderUpdate");
            if (orderUpdatePage.isUpdateOrderPaymentAmount()){
                lineItemCancellationManager.updateOrderPaymentAmount(order);
            }
            if (orderUpdatePage.isSendOrderUpdateConfirmation() && lineItemCancellationManager.isOrderUpdateConfirmationEmailEnabled() ){
                orderUpdateConfirmationProcessor.sendOrderStatusUpdateEmail(order);
            }
        }
        
    }

    /**
     * Set Site for Order.
     * @param pOrderXBean the order xml bean
     * @param pOrderEntity the order entity
     */
    private void importSite(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        String siteCode = pOrderXBean.getSiteCode();
        if ((siteCode == null) || (siteCode.length() == 0)) {
            throw new IntegrationException(Constants.COMMON_MISSING_SITE_CODE,"Code for site is required.");
        }

        ISite site = siteHome.findByCode(siteCode);

        if (site == null) {
            throw new IntegrationException(Constants.COMMON_SITE_DOES_NOT_EXIST,"Site not found for code = " + siteCode);
        }

        pOrderEntity.setSite(site);
    }

    /**
     * Copies Customer data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importCustomer(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        String customerCode = pOrderXBean.getCustomerCode();
        if ((customerCode == null) || (customerCode.length() == 0)) {
            IntegrationException ie= new IntegrationException(Constants.ORDER_CUSTOMER_CODE_IS_REQUIRED,"Code for customer is required.");
            ie.getErrorFields().put("Customer","");
            throw ie;
        }

        ICustomer customer = customerConverter.lookupCustomer(customerCode);
        if (customer == null) {
            IntegrationException ie =  new IntegrationException(Constants.ORDER_CUSTOMER_CODE_NOT_FOUND,"Customer not found for code = " + customerCode);
            ie.getErrorFields().put("Customer",customerCode);
            throw ie;
        }
        pOrderEntity.setCustomer(customer);
    }

    /**
     * Copies Bill To data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importBillToInfo(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        if (!pOrderXBean.isSetBillToInfo()) {
            return;
        }

        OrderBillShipInfoXBean orderBillShipInfoXBean = pOrderXBean.getBillToInfo();
        String billShipInfoCode = orderBillShipInfoXBean.getCode();
        if ((billShipInfoCode == null) || (billShipInfoCode.length() == 0)) {
            IntegrationException ie = new IntegrationException(Constants.ORDER_BILL_SHIP_INFO_CODE_IS_REQUIRED,"Code for bill ship info is required.");
            ie.getErrorFields().put("BillShipInfo","");
            throw ie;
        }

        /**
         * Copies Bill Ship To data from Order Xbean to Order entity.
         *
         * @param pOrderXBean the order xbean to copy from
         * @param pOrderEntity the order entity to copy to
         */
        IOrderBillShipInfo orderBillShipInfo = billInfoConverter.lookupBillShipInfo(billShipInfoCode);
        if (orderBillShipInfo == null) {
            // create the new bill info object
            orderBillShipInfo = (IOrderBillShipInfo) billToHome.get(null);
            billToHome.create(orderBillShipInfo);
            orderBillShipInfo.addOrder(pOrderEntity);
            pOrderEntity.setBillToInfo(orderBillShipInfo);
        }
        billInfoConverter.xbeanToEntity(orderBillShipInfoXBean, orderBillShipInfo);
    }

    /**
     * Copies Shipment data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importShipments(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        if (!pOrderXBean.isSetShipments()) {
            return;
        }

        OrderShipmentXBean[] arr = pOrderXBean.getShipments().getOrderShipmentArray();
        boolean overwrite = pOrderXBean.getShipments().getOverwrite();

        ArrayList deleteList = null;
        deleteList = new ArrayList();
        if (overwrite) {
            deleteList.addAll(pOrderEntity.getShipments());
        }

        for (int i = 0; i < arr.length; i++) {

            OrderShipmentXBean orderShipmentXBean = arr[i];

            String orderShipmentCode = orderShipmentXBean.getCode();

            if ((orderShipmentCode == null) || (orderShipmentCode.length() == 0)) {
                IntegrationException ie = new IntegrationException(Constants.ORDER_SHIPMENT_CODE_IS_REQUIRED,"Code for order shipment is required.");
                ie.getErrorFields().put("Shipment","");
                throw ie;
            }

            IOrderShipment pOrderShipmentEntity = shipmentConverter.lookupShipment(orderShipmentCode);

            if (pOrderShipmentEntity == null) {
                // create the new shipment
                pOrderShipmentEntity = (IOrderShipment) orderShipmentHome.get(null);
                orderShipmentHome.create(pOrderShipmentEntity);
                pOrderEntity.addShipment(pOrderShipmentEntity);
            } else if (overwrite) {
                deleteList.remove(pOrderShipmentEntity);
            }

            pOrderShipmentEntity.setCart(pOrderEntity);
            shipmentConverter.xbeanToEntity(orderShipmentXBean, pOrderShipmentEntity);

        }

        // now delete order shipments no longer required
        if (overwrite) {
            for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                pOrderEntity.getShipments().remove(iter.next());
            }
        }
    }

    /**
     * Copies Payment data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importPayments(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        if (!pOrderXBean.isSetPayments()) {
            return;
        }

        OrderXBean.Payments.Payment[] arr = pOrderXBean.getPayments().getPaymentArray();

        List payments = new ArrayList();
        for (int i = 0; i < arr.length; i++) {
            OrderXBean.Payments.Payment orderPaymentXBean = arr[i];

            if (orderPaymentXBean.isSetOrderPaymentCreditCard()) {
                OrderPaymentCreditCardXBean orderPaymentCreditCardXBean = orderPaymentXBean.getOrderPaymentCreditCard();
                String orderPaymentCode = orderPaymentCreditCardXBean.getOrderPayment().getCode();
                IOrderPaymentCreditCard orderPayment = null;
                if (orderPaymentCode != null && orderPaymentCode.length() > 0) {
                    orderPayment = (IOrderPaymentCreditCard) paymentConverter.lookupPayment(orderPaymentCode);
                }
                if (orderPayment == null) {
                    orderPayment = (IOrderPaymentCreditCard) orderPaymentHome.get(IOrderPaymentCreditCard.class, null);
                }
                orderPayment.setOrder(pOrderEntity);
                paymentCreditCardConverter.xbeanToEntity(orderPaymentCreditCardXBean, orderPayment);
                payments.add(orderPayment);
            } else if (orderPaymentXBean.isSetOrderPaymentGiftCertificate()) {
                OrderPaymentGiftCertificateXBean orderPaymentGiftCertificateXBean = orderPaymentXBean
                .getOrderPaymentGiftCertificate();
                String orderPaymentCode = orderPaymentGiftCertificateXBean.getOrderPayment().getCode();
                IOrderPaymentGiftCertificate orderPayment = null;
                if (orderPaymentCode != null && orderPaymentCode.length() > 0) {
                    orderPayment = (IOrderPaymentGiftCertificate) paymentConverter.lookupPayment(orderPaymentCode);
                }
                if (orderPayment == null) {
                    orderPayment = (IOrderPaymentGiftCertificate) orderPaymentHome.get(IOrderPaymentGiftCertificate.class, null);
                }
                orderPayment.setOrder(pOrderEntity);
                paymentGiftCertificateConverter.xbeanToEntity(orderPaymentGiftCertificateXBean, orderPayment);
                payments.add(orderPayment);
            } else if (orderPaymentXBean.isSetOrderPaymentCheck()) {
                OrderPaymentCheckXBean orderPaymentCheckXBean = orderPaymentXBean.getOrderPaymentCheck();
                String orderPaymentCode = orderPaymentCheckXBean.getOrderPayment().getCode();
                IOrderPaymentCheck orderPayment = null;
                if (orderPaymentCode != null && orderPaymentCode.length() > 0) {
                    orderPayment = (IOrderPaymentCheck) paymentConverter.lookupPayment(orderPaymentCode);
                }
                if (orderPayment == null) {
                    orderPayment = (IOrderPaymentCheck) orderPaymentHome.get(IOrderPaymentCheck.class, null);
                }
                orderPayment.setOrder(pOrderEntity);
                paymentCheckConverter.xbeanToEntity(orderPaymentCheckXBean, orderPayment);
                payments.add(orderPayment);
            }
        }
        pOrderEntity.setPayments(payments);

    }

    /**
     * Copies Tracking data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importTrackings(OrderXBean pOrderXBean, IOrder pOrderEntity) {

        if (!pOrderXBean.isSetTrackings()) {
            return;
        }

        OrderTrackingXBean[] arr = pOrderXBean.getTrackings().getOrderTrackingArray();
        boolean overwrite = pOrderXBean.getTrackings().getOverwrite();

        ArrayList deleteList = null;
        deleteList = new ArrayList();
        if (overwrite) {
            deleteList.addAll(pOrderEntity.getTrackings());
        }

        for (int i = 0; i < arr.length; i++) {
            OrderTrackingXBean orderTrackingXBean = arr[i];
            String code = orderTrackingXBean.getCode();

            if ((code == null) || (code.length() == 0)) {
                IntegrationException ie = new IntegrationException(Constants.ORDER_TRACKING_CODE_IS_REQUIRED,"Code for order tracking is required.");
                ie.getErrorFields().put("OrderTracking","");
                throw ie;
            }

            IOrderTracking orderTracking = orderTrackingConverter.lookupTracking(code);
            if (orderTracking == null) {
                // create the new tracking
                orderTracking = new OrderTracking();
            } else if (overwrite) {
                deleteList.remove(orderTracking);
            }

            orderTrackingConverter.xbeanToEntity(orderTrackingXBean, orderTracking);
            pOrderEntity.addTracking(orderTracking);
        }

        // now delete order trackings no longer required
        if (overwrite) {
            for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                pOrderEntity.getTrackings().remove(iter.next());
            }
        }
    }

    /**
     * Copies Discount data from Order Xbean to Order entity.
     *
     * @param pOrderXBean the order xbean to copy from
     * @param pOrderEntity the order entity to copy to
     */
    private void importDiscounts(final OrderXBean pOrderXBean,
            final IOrder pOrderEntity) {

        if (!pOrderXBean.isSetDiscounts()) {
            return;
        }

        OrderDiscountXBean[] arr = pOrderXBean.getDiscounts().getDiscountArray();

        if (arr != null && arr.length > 0) {
            IntegrationException ie = new IntegrationException(Constants.ORDER_DISCOUNT_NOT_SUPPORT_TO_IMPORT,"OrderDiscount importing is not supported.");
            ie.getErrorFields().put("OrderDiscount", arr[0].getSourceCodesAsString());
            throw ie;
        }
    }

    /**
     * Looks up an order entity with the given code.
     *
     * @param code entity code to look up
     * @return order entity or null if none exists
     */
    protected IOrder lookupOrder(final String code) {
        return orderHome.findByCode(code);
    }

}
