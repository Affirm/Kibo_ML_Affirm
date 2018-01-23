package com.deplabs.affirm.integration.cart.order;

import org.deplabs.entity.cart.order.IOrderPaymentAffirm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.marketlive.integration.cart.order.OrderPaymentConverter;
import com.marketlive.integration.xmlbean.OrderPaymentAffirmXBean;
import com.marketlive.integration.xmlbean.OrderPaymentXBean;

@Component
public class OrderPaymentAffirmConverter {

  @Autowired
  private OrderPaymentConverter paymentConverter;


  	/**
	 * Copies data from the given entity object to the XBean object
	 * @param pEntity - the source entity
	 * @param pXBean - the target xml bean
	 * @return - the target xml bean
	 */
	public OrderPaymentAffirmXBean entityToXBean(IOrderPaymentAffirm pEntity, OrderPaymentAffirmXBean pXBean){

	    pXBean.setPk(Integer.parseInt(pEntity.getPk().getAsString()));

	    OrderPaymentXBean baseXBean = pXBean.addNewOrderPayment();
	    baseXBean = paymentConverter.entityToXBean(pEntity, baseXBean);

	    return pXBean;
  }

  /**
   * Copy attributes from the XML data bean to the given entity bean.
   * @param pXBean
   * @param pEntity
   * @return OrderPaymentGiftCertificate with attributes from
   * the OrderPaymentGiftCertificateXBean.
   */
	public IOrderPaymentAffirm xbeanToEntity(final OrderPaymentAffirmXBean pXBean,
          final IOrderPaymentAffirm pEntity) {

      if (pXBean.getOrderPayment() != null) {
          copyPayment(pXBean, pEntity);
      }

      return pEntity;

  }

   /**
   *
   * @param pXBean the Xml bean
   * @param pEntity the entity
   */
  private void copyPayment(final OrderPaymentAffirmXBean pXBean,
          final IOrderPaymentAffirm pEntity) {
      OrderPaymentXBean orderPaymentXBean = pXBean.getOrderPayment();
      paymentConverter.xbeanToEntity(orderPaymentXBean, pEntity);
  }

}
