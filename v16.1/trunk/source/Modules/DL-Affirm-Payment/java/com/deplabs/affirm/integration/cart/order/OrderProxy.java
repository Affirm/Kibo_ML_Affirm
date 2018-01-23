package com.deplabs.affirm.integration.cart.order;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.marketlive.entity.account.ICustomer;
import org.marketlive.entity.cart.order.IOrder;
import org.marketlive.entity.cart.order.IOrderHome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.marketlive.integration.Constants;
import com.marketlive.integration.EntityProxy;
import com.marketlive.integration.IntegrationException;
import com.marketlive.integration.account.CustomerConverter;
import com.marketlive.integration.log4j.SMTPEvaluator;
import com.marketlive.integration.xmlbean.CommandResultXBean;
import com.marketlive.integration.xmlbean.CommandResultXBean.Summary;
import com.marketlive.integration.xmlbean.FailureXBean;
import com.marketlive.integration.xmlbean.FindByCriteriaParametersDocumentXBean;
import com.marketlive.integration.xmlbean.FindByCriteriaParametersXBean;
import com.marketlive.integration.xmlbean.OrderDocumentXBean;
import com.marketlive.integration.xmlbean.OrderXBean;
import com.marketlive.integration.xmlbean.OrdersDocumentXBean;
import com.marketlive.integration.xmlbean.OrdersXBean;


@Component
@Transactional(propagation= Propagation.SUPPORTS, readOnly=true)
@Primary
public class OrderProxy extends EntityProxy {

	/** Logger */
	private static Log log = LogFactory.getLog(OrderProxy.class);

    @Autowired
	private IOrderHome orderHome;

    @Autowired
	private ExtendedOrderConverter orderConverter;

    @Autowired
	private CustomerConverter customerConverter;

	/**
	 * Setter for OrderConverter
	 * 
	 * @param orderConverter
	 */
	public void setOrderConverter(final ExtendedOrderConverter orderConverter) {
		this.orderConverter = orderConverter;
	}

	public void setCustomerConverter(final CustomerConverter customerConverter) {
		this.customerConverter = customerConverter;
	}

	/**
	 * Setter for OrderHome.
	 * 
	 * @param orderHome
	 */
	public void setOrderHome(final IOrderHome orderHome) {
		this.orderHome = orderHome;
	}

	/**
	 * @param pXmlElement
	 *            the xml element.
	 */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public String save(final String pXmlElement) {
		String code = "";
		try {
			OrderDocumentXBean doc = OrderDocumentXBean.Factory
					.parse(pXmlElement);
			// doc.validate();
			OrderXBean pOrderXBean = doc.getOrder();

			// check required attributes
			code = pOrderXBean.getCode();
			if (code == null || code.length() == 0) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_IS_REQUIRED,
						"Order Save: Code is required for lookup.");
				ie.getErrorFields().put("Order", "");
				throw ie;
			}

			IOrder pOrderEntity = orderConverter.lookupOrder(code);
			if (pOrderEntity != null) {
				log.info("An Order with code [" + code
						+ "] already exists in the database.");
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_ALREADY_EXISTS,
						"An Order with code [" + code
								+ "] already exists in the database.");
				ie.getErrorFields().put("Order", code);
				throw ie;
			}
			pOrderEntity = createOrder(pOrderXBean);

		} catch (Exception ex) {
			throw newIntegrationException(code, ex);
		}

		// TODO set return string
		return null;
	}

	/**
	 * Updates the Order entity with using the xml structure.
	 * 
	 * @param pXmlElement
	 *            the xml element to update
	 * @return the result string, if any.
	 */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public String update(final String pXmlElement) {
		String code = "";
		try {
			OrderDocumentXBean doc = OrderDocumentXBean.Factory
					.parse(pXmlElement);
			// doc.validate();
			OrderXBean pOrderXBean = doc.getOrder();

			// check required attributes
			code = pOrderXBean.getCode();
			if (code == null || code.length() == 0) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_IS_REQUIRED,
						"Order Update: Code is required for lookup.");
				ie.getErrorFields().put("Order", "");
				throw ie;
			}

			IOrder pOrderEntity = orderConverter.lookupOrder(code);
			if (pOrderEntity == null) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_NOT_FOUND, "An Order with code "
								+ code + " does not exist in the database.");
				ie.getErrorFields().put("Order", code);
				throw ie;
			}

			orderConverter.xbeanToEntity(pOrderXBean, pOrderEntity);
			log.debug("Updating order with code " + code);
			this.orderHome.update(pOrderEntity);

		} catch (Exception ex) {
			throw newIntegrationException(code, ex);
		}

		// TODO set return string
		return null;
	}

	/**
	 * Save or updates the entity using the xml bean structure.
	 * 
	 * @param pXmlElement
	 *            the xml structure.
	 * @return the result string, if any.
	 */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public String saveOrUpdate(final String pXmlElement) {
		String code = "";
		try {
			OrderDocumentXBean doc = OrderDocumentXBean.Factory
					.parse(pXmlElement);
			// doc.validate();
			OrderXBean pOrderXBean = doc.getOrder();

			// check required attributes
			code = pOrderXBean.getCode();
			if (code == null || code.length() == 0) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_IS_REQUIRED,
						"Order Save or Update: Code is required for lookup.");
				ie.getErrorFields().put("Order", "");
				throw ie;
			}

			IOrder pOrderEntity = orderConverter.lookupOrder(code);
			if (pOrderEntity == null) {
				pOrderEntity = createOrder(pOrderXBean);
			} else {
				orderConverter.xbeanToEntity(pOrderXBean, pOrderEntity);
				this.orderHome.update(pOrderEntity);
			}

		} catch (Exception ex) {
			throw newIntegrationException(code, ex);
		}

		// TODO set return string
		return null;
	}

	/**
	 * Deletes an entity from the database.
	 * 
	 * @param pXmlElement
	 *            the xml bean that holds the code to look up the entity for
	 *            deletion.
	 * @return the result string, if any.
	 */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
	public String delete(final String pXmlElement) {
		String code = "";
		try {
			OrderDocumentXBean doc = OrderDocumentXBean.Factory
					.parse(pXmlElement);
			// doc.validate();
			OrderXBean pOrderXBean = doc.getOrder();

			// check required attributes
			code = pOrderXBean.getCode();
			if (code == null || code.length() == 0) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_IS_REQUIRED,
						"Order Delete: Code is required for lookup.");
				ie.getErrorFields().put("OrderCode", "");
				throw ie;
			}

			IOrder pOrderEntity = orderConverter.lookupOrder(code);
			if (pOrderEntity == null) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_CODE_NOT_FOUND,
						"Order Delete: an Order with code " + code
								+ " does not exist in the database.");
				ie.getErrorFields().put("Order", code);
				throw ie;
			}

			pOrderEntity.setDeleted(true);
			this.orderHome.update(pOrderEntity);

		} catch (Exception ex) {
			throw newIntegrationException(code, ex);
		}

		// TODO set return string
		return null;
	}

	/**
	 * @param code
	 *            the code to use for looking up the Order.
	 * @param locale
	 *            the locale needed to find the correct record.
	 * @return the xml document encompassing the entity data.
	 */
	public String findByCode(final String code, final Locale locale) {
		try {
			// look up the order
			if (code == null || code.length() == 0) {
				IntegrationException ie = new IntegrationException(
						Constants.ORDER_EXPORT_NO_ORDER_CODE,
						"Order Code is required.");
				ie.getErrorFields().put("Order", "");
				throw ie;
			}

			if (locale == null) {
				throw IntegrationException.newNullLocaleException();
			}

			IOrder order = orderConverter.lookupOrder(code);

			XmlOptions xmlOptions = new XmlOptions()
					.setCharacterEncoding("UTF-8");
			OrderDocumentXBean newDoc = OrderDocumentXBean.Factory
					.newInstance(xmlOptions);

			if (order == null) {
				return newDoc.toString();
			}

			OrderXBean exportOrderXBean = newDoc.addNewOrder();
			orderConverter.entityToXBean(order, exportOrderXBean, locale);

			return newDoc.toString();
		} catch (Exception ex) {
			throw newIntegrationException(code, ex);
		}
	}

	/**
	 * Used by the export. Finds and returns an export-ready xml based on the
	 * search criteria provided by the Xml query argument.
	 * 
	 * @param pXmlQuery
	 *            the xml query criteria to use.
	 * @param locale
	 *            the specific locale.
	 * @return the result string, if any.
	 */
	public String findByCriteria(final String pXmlQuery, final Locale locale) {

		try {
			if (locale == null) {
			throw IntegrationException.newNullLocaleException();
		}
			FindByCriteriaParametersDocumentXBean doc = FindByCriteriaParametersDocumentXBean.Factory
					.parse(pXmlQuery);
			// doc.validate();
			FindByCriteriaParametersXBean xBean = doc
					.getFindByCriteriaParameters();

			// Create new empty order doc
			XmlOptions xmlOptions = new XmlOptions()
					.setCharacterEncoding("UTF-8");
			OrdersDocumentXBean newOrdersDoc = OrdersDocumentXBean.Factory
					.newInstance(xmlOptions);
			OrdersXBean ordersXBean = newOrdersDoc.addNewOrders();

			if (xBean.isSetHql()) {
				String hql = xBean.getHql();
				Collection orders = orderHome.findByQuery(hql);
				Iterator iter = orders.iterator();
				while (iter.hasNext()) {
					IOrder order = (IOrder) iter.next();
					OrderXBean orderXBean = ordersXBean.addNewOrder();
					orderConverter.entityToXBean(order, orderXBean, locale);
				}
			}
			return newOrdersDoc.toString();
		} catch (XmlException e) {
			throw new IntegrationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void findByCriteria(final String pXmlQuery, final Locale locale,
			Writer resultWriter, CommandResultXBean pCommandResult) {

		log.debug("OrderProxy: Called findByCriteria with resultWriter.");
		if (locale == null) {
			throw IntegrationException.newNullLocaleException();
		}

		int totalCount = 0;
		int failureCount = 0;
		try {
			FindByCriteriaParametersDocumentXBean doc = FindByCriteriaParametersDocumentXBean.Factory
					.parse(pXmlQuery);
			// doc.validate();
			FindByCriteriaParametersXBean xBean = doc
					.getFindByCriteriaParameters();

			// Create new empty order doc
			XmlOptions xmlOptions = new XmlOptions()
					.setCharacterEncoding("UTF-8").setSaveOuter()
					.setSavePrettyPrint().setSavePrettyPrintIndent(4)
					.setUseDefaultNamespace();

			OrdersDocumentXBean newOrdersDoc = OrdersDocumentXBean.Factory
					.newInstance(xmlOptions);
			OrdersXBean ordersXBean = newOrdersDoc.addNewOrders();

			String ordersWrapper = newOrdersDoc.toString();
			resultWriter.write(ordersWrapper.substring(0,
					ordersWrapper.indexOf("/>")));
			resultWriter.write(">");
			resultWriter.write(System.getProperty("line.separator"));

			if (xBean.isSetHql()) {
				String hql = xBean.getHql();
				Collection<IOrder> orders = orderHome.findByQuery(hql);
				for (IOrder order : orders) {
					OrderDocumentXBean orderDoc = OrderDocumentXBean.Factory
							.newInstance(xmlOptions);
					OrderXBean orderXBean = orderDoc.addNewOrder();
					try {
						orderConverter.entityToXBean(order, orderXBean, locale);
						orderDoc.save(resultWriter, xmlOptions);
						resultWriter
								.write(System.getProperty("line.separator"));
					} catch (Exception e) {
						SMTPEvaluator.setSMTPLoggerTriggered(true);
						failureCount++;
						log.error("Failure detected for order with id = "
								+ order.getPk().getAsString() + " and code = "
								+ order.getCode(), e);
						addFailureToExportResult(pCommandResult, e, order.getCode());
						continue;
					}
				}
				totalCount = orders.size();
			}
		} catch (XmlException e) {
			throw new IntegrationException(e);
		} catch (IOException e) {
			throw new IntegrationException(e);
		} finally {
			// Close out <orders> element
			try {
				resultWriter.write(System.getProperty("line.separator"));
				resultWriter.write("</orders>");
				resultWriter.write(System.getProperty("line.separator"));
				Summary summary = getCommandResultSummary(pCommandResult);
				log.debug("totalCount = " + totalCount);
				log.debug("failureCount = " + failureCount);
				if (pCommandResult.getFailures() != null && pCommandResult.getFailures().getFailureArray() != null 
						&& pCommandResult.getFailures().getFailureArray().length > 0) {
					for (FailureXBean failure : pCommandResult.getFailures().getFailureArray()) {
						if (failure.getFailedRecord() != null) {
							log.error("Export Failed for entity = " + failure.getFailedRecord().getEntity() + " having id = " + failure.getFailedRecord().getID());
						}
						log.error("Failure code = " + failure.getCode() + " and failure message = " + failure.getMessage());
					}
					pCommandResult.setFailures(null);
					failureCount = 0;
				}
				summary.setTotal(totalCount);
				summary.setFailed(failureCount);
			} catch (IOException e) {
				throw new IntegrationException(e);
			}

		}
	}

	/**
	 * Creates an order using the xml bean structure.
	 * 
	 * @param orderXBean
	 * @return the new Order entity
	 */
	private IOrder createOrder(final OrderXBean orderXBean) {

		// Check required attributes.
		if (orderXBean.isSetDeleted() && orderXBean.getDeleted() == true) {
			IntegrationException ie = new IntegrationException(
					Constants.COMMON_DELETE_FLAG_IS_TRUE_FOR_NEW_ENTITY,
					"Order cannot be created if marked for delete flag is true.");
			ie.getErrorFields().put("DeleteFlag", "True");
			throw ie;
		}

		// check required attributes for create
		String code = orderXBean.getCode();
		if (code == null || code.length() == 0) {
			IntegrationException ie = new IntegrationException(
					Constants.ORDER_CODE_IS_REQUIRED,
					"Order create: code is required.");
			ie.getErrorFields().put("Order", "");
			throw ie;
		}

		String customerCode = orderXBean.getCustomerCode();
		if (customerCode == null || customerCode.length() == 0) {
			IntegrationException ie = new IntegrationException(
					Constants.CREATE_ORDER_NO_CUSTOMER_CODE,
					"Order create: customer code is required.");
			ie.getErrorFields().put("Customer", "");
			throw ie;
		}

		ICustomer customer = customerConverter.lookupCustomer(customerCode);
		if (customer == null) {
			IntegrationException ie = new IntegrationException(
					Constants.CREATE_ORDER_NOT_EXISTED_CUSTOMER,
					"Order create: customer is required.");
			ie.getErrorFields().put("Customer", customerCode);
			throw ie;
		}

		IOrder pOrderEntity = orderHome.create(customer);
		log.debug("Creating order with code " + code);

		orderConverter.xbeanToEntity(orderXBean, pOrderEntity);
		orderHome.save(pOrderEntity);

		return pOrderEntity;
	}

}
