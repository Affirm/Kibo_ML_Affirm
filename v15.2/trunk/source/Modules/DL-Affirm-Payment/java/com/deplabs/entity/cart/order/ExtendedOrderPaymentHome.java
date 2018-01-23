package com.deplabs.entity.cart.order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.marketlive.entity.IEntity;
import org.marketlive.entity.IPrimaryKey;
import org.marketlive.entity.cart.order.IOrderPaymentAmazon;
import org.marketlive.entity.cart.order.IOrderPaymentCheck;
import org.marketlive.entity.cart.order.IOrderPaymentCreditCard;
import org.marketlive.entity.cart.order.IOrderPaymentGiftCertificate;
import org.marketlive.entity.cart.order.IOrderPaymentHome;
import org.marketlive.entity.cart.order.IOrderPaymentPayPal;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.marketlive.entity.Entity;
import com.marketlive.entity.cart.order.OrderPaymentAmazon;
import com.marketlive.entity.cart.order.OrderPaymentCheck;
import com.marketlive.entity.cart.order.OrderPaymentCreditCard;
import com.marketlive.entity.cart.order.OrderPaymentGiftCertificate;
import com.marketlive.entity.cart.order.OrderPaymentHome;
import com.marketlive.entity.cart.order.OrderPaymentPayPal;
import com.marketlive.system.config.spring.RecycledApplicationContextAware;

/**
 * Home object of available order payment types.
 */
@Repository
@Primary
public class ExtendedOrderPaymentHome extends OrderPaymentHome implements IOrderPaymentHome, RecycledApplicationContextAware {

	private static Log log = LogFactory.getLog(ExtendedOrderPaymentHome.class);
	
	 public ExtendedOrderPaymentHome() throws Exception {
			 super();
	 }
	 
	 /**
     * Returns an Entity given a Primary Key object.
     * Replace "Entity" with your entity class name (such as "Category" or "State").
     * @param paymentInterface the interface of the order payment
     * @param pk the primary key object
     * @return an entity matching the interface and PK
     */
    public IEntity get(final Class paymentInterface, final IPrimaryKey pk) {
        Entity payment;
        //todo:figure out better way to check if the parameter is of the required type
        if (paymentInterface.getName().equals(IOrderPaymentCreditCard.class.getName())) {
            payment = new OrderPaymentCreditCard();
        } else if (paymentInterface.getName().equals(IOrderPaymentGiftCertificate.class.getName())) {
            payment = new OrderPaymentGiftCertificate();
        } else if (paymentInterface.getName().equals(IOrderPaymentCheck.class.getName())) {
            payment = new OrderPaymentCheck();
        } else if(paymentInterface.getName().equals(IOrderPaymentPayPal.class.getName())) {
            payment = new OrderPaymentPayPal();
        } else if(paymentInterface.getName().equals(IOrderPaymentAmazon.class.getName())) {
            payment = new OrderPaymentAmazon();
        } else if(paymentInterface.getName().equals(IOrderPaymentAffirm.class.getName())) {
        	payment = new OrderPaymentAffirm();	
        }	else {
            throw new RuntimeException("Not a recognized payment type:" + paymentInterface.getName());
        }

        payment.setPk(pk);
        return payment;
    }
}
