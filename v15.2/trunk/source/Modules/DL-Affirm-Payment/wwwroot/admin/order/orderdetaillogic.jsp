<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mmlive.service.log.ILogger,
                 java.util.*,
                 com.marketlive.util.DateFormatter,
                 com.marketlive.entity.cart.order.Order,
                 org.springframework.web.context.WebApplicationContext,
                 org.springframework.web.context.support.WebApplicationContextUtils,
                 org.marketlive.biz.account.IAccountManager,
                 org.marketlive.entity.account.ICustomer,
                 org.marketlive.entity.cart.basket.IBasketHome,
                 java.text.NumberFormat,
                 com.marketlive.entity.cart.order.OrderItem,
                 com.marketlive.biz.discount.DiscountDefinitionGraph,
                 com.marketlive.admin.order.OrderUtilities,
                 com.deplabs.affirm.admin.order.ExtendedOrderUtilities,
                 org.marketlive.entity.cart.order.*,
                 org.marketlive.entity.cart.ICartItem,
                 org.marketlive.biz.cart.order.IOrderManager,
                 org.marketlive.entity.cart.order.IOrderTracking,
                 org.marketlive.system.config.IConfigurationManager,
                 com.marketlive.entity.currency.Amount,
                 com.marketlive.system.text.AmountFormat,
                 org.marketlive.entity.currency.IAmount,
                 org.marketlive.entity.shipping.IShippingMethodItemDescriptionPage,
                 com.marketlive.entity.shipping.ShippingMethodItemDescriptionPage,
                 com.marketlive.system.locale.LocaleManager,
                 org.marketlive.entity.discount.IDiscount,
                 org.marketlive.entity.cart.ICartShipment"
                 %>

<%--
    //(C) Copyright MarketLive. 2006. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>

<%@ page import="org.apache.commons.collections.functors.WhileClosure" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.apache.struts.util.MessageResources" %>
<%@ page import="org.apache.struts.Globals" %>
<%
    // File Name	:  orderdetaillogic.jsp
// Created By	:  05/18/03, Uday Devata
%>

<%
    ILogger log = com.mmlive.framework.log.LoggerHome.Get().getLogger("jsp.orderhistory");

    WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
    IAccountManager accountManager = wac.getBean(IAccountManager.class);
    IOrderManager   orderManager   = wac.getBean(IOrderManager.class);
    IOrderHome orderHome = wac.getBean(IOrderHome.class);
    IConfigurationManager configManager = wac.getBean("configurationManager", IConfigurationManager.class);

    boolean showOrderStatus = configManager.getAsBoolean("app.b2c.account.OrderHistory.orderDetail_showOrderStatus");
    boolean showOrderTracking = configManager.getAsBoolean("app.b2c.account.OrderHistory.orderDetail_showOrderTracking");
    String shipmentCarrierWxH = configManager.getAsString("app.b2c.common.FlyOpen.shipmentCarrier");
    String sAffirmPaymentMerchantUrl = configManager.getAsString("custom.affirmpayment_merchant_detail_url");
    String sAffirmPaymentPublicKey = configManager.getAsString("custom.affirmpayment_public_api_key");
    
    String sDateOrdered = "N/A";
    String sStatus = "";
    StringBuffer sShipmentTracking = new StringBuffer();
    String sBillInfo = "";
    String sPaymentMethod = "";
    String sPaymentRequestId = "";
    ICustomer customer = null;
    String customerID = request.getParameter("iCustomerID") != null ? request.getParameter("iCustomerID") : "0";
    String orderID = request.getParameter("iOrderID") != null ? request.getParameter("iOrderID") : "0";
    String sSequentialNo = request.getParameter("sSequentialNo") != null ? request.getParameter("sSequentialNo") : "";
    int shipmentCount;

    if (!customerID.equals("0")) {
        try {
            customer = accountManager.findCustomer(customerID);
        } catch (Exception e) {
            log.error("orderhistorylogic.jsp - Error found at loading customer- " + customerID + ":" + e.toString());
        }
    } else {
        customer = (ICustomer) session.getAttribute("customer");
    }



    IOrder orderReview = (IOrder) orderHome.findByPk(orderID);
    List discounts = orderManager.findOrderDiscounts(orderReview.getPk());
    Locale locale = orderReview.getLocale();
    Currency currency = orderReview.getSite().getCurrency();

    // -- Get Date Ordered --
    Date date = orderReview.getDateOrdered();
    DateFormatter dateFormatter = DateFormatter.getDateFormatter();

    if (date != null) {
      sDateOrdered = dateFormatter.getFormatForDate(orderReview.getDateOrdered(), DateFormatter.DATE_4Y_TIME);
    }

    // Order Status
    if (showOrderStatus && (orderReview.getStatus() == null || "".equals(orderReview.getStatus()))) {
      sStatus = "Status: No status available.";
    }
    else if (showOrderStatus) {
      sStatus = "Status: " + orderReview.getStatus();
    }

    // Order Comments
    String orderComments = "None";
    if (orderReview.getComment1() != null && !"".equals(orderReview.getComment1())) {
        orderComments = orderReview.getComment1();
    }
    if (orderReview.getComment2() != null) {
        if ("None".equals(orderComments)) {
            orderComments = "";
        } else {
            orderComments += "<br/>";
        }
        orderComments += orderReview.getComment2();
    }

    // Order Tracking
    if (showOrderTracking && orderReview.getTrackings().isEmpty()) {
      sShipmentTracking.append("<div class=text-normal>No tracking information available.</div>");
    }
    else {
      List trackings = orderReview.getTrackings();
      for (int trackingIdx=0; trackingIdx<trackings.size(); trackingIdx++) {
        IOrderTracking tracking = (IOrderTracking) trackings.get(trackingIdx);
        sShipmentTracking.append("<div class=text-normal>" + tracking.getCarrierName());
        if (tracking.getTrackingNumber() != null && tracking.getTrackingNumber().length() > 0) {
          sShipmentTracking.append(" - " + tracking.getTrackingNumber());
          if (tracking.getCarrierURL() != null && tracking.getCarrierURL().length() > 0) {
            sShipmentTracking.append("&nbsp<a href=\"#\" onClick=\"return flyopen(" + shipmentCarrierWxH + ", '" + tracking.getCarrierURL() + "', 'tracking')\">(track it)</a></div>");
          }
        }
      }
    }

    // Get related shipments
    List shipments = orderReview.getShipments();

    // Use String arrays sized at number of shipments
    shipmentCount = shipments.size();
    String[] sDateShipped = new String[shipmentCount];
    String[] sShipInfo = new String[shipmentCount];
    String[] sShipMethod  = new String[shipmentCount];
    String[] sShipWeightFees  = new String[shipmentCount];
    String[] sShipGiftMessage = new String[shipmentCount];

    // For building order item strings for each shipment
    StringBuffer[] sLineItems = new StringBuffer[shipmentCount];
    // For building order total strings
    StringBuffer sOrderTotals;

    // Create billing/shipping address string
    sBillInfo = OrderUtilities.getBillShipInfoText(orderReview.getBillToInfo());
    // Create payment string
    sPaymentMethod = ExtendedOrderUtilities.getPaymentMethodText(orderReview);
    sPaymentRequestId = ExtendedOrderUtilities.getPaymentAuthorizationRequestId(orderReview);

    // -- Shipment Level Details --
    for (int i=0; i <shipmentCount; i++) {
        IOrderShipment shipment = (IOrderShipment) shipments.get(i);
        sShipInfo[i] = OrderUtilities.getShipInfoText(shipment.getShipToInfo());
        sShipMethod[i] = OrderUtilities.checkNull(shipment.getSnapshot().getShippingMethodName());
		sShipGiftMessage[i] = shipment.getGiftMessage();

        if (shipment.getShippingWeightTotal() != null) {
            sShipWeightFees[i] = AmountFormat.format(shipment.getShippingWeightTotal(), locale, currency);
        } else {
            sShipWeightFees[i] = "N/A";
        }

        if (shipment.getDateShipped() != null) {
            sDateShipped[i] = dateFormatter.getFormatForDate(shipment.getDateShipped(), DateFormatter.DATE_4Y_TIME);
        } else {
            sDateShipped[i] = "Not shipped";
        }

        if (shipment.getSnapshot().getShippingMethodDescription() != null) {
            sShipMethod[i] = sShipMethod[i] + "&nbsp;" + shipment.getSnapshot().getShippingMethodDescription();
        }

        // Order Status
        if (shipment.getStatus() != null && ! "".equals(shipment.getStatus())) {
			sShipMethod[i] = sShipMethod[i] + "<div class=text-normal>Shipment Status: " + shipment.getStatus()+ "</div>";
        }

        // Order Tracking
        if (!shipment.getTrackings().isEmpty()) {
          List trackings = shipment.getTrackings();
          sShipMethod[i] = sShipMethod[i] + "<br><div class=text-bold>Shipment Tracking:</div>";
          for (int trackingIdx=0; trackingIdx<trackings.size(); trackingIdx++) {
            IOrderTracking tracking = (IOrderTracking) trackings.get(trackingIdx);
            sShipMethod[i] = sShipMethod[i] + "<div class=text-normal>" + tracking.getCarrierName();
            if (tracking.getTrackingNumber() != null && tracking.getTrackingNumber().length() > 0) {
              sShipMethod[i] = sShipMethod[i] +  " - " + tracking.getTrackingNumber();
              if (tracking.getCarrierURL() != null && tracking.getCarrierURL().length() > 0) {
                sShipMethod[i] = sShipMethod[i] +  "&nbsp<a href=\"#\" onClick=\"return flyopen(" + shipmentCarrierWxH + ", '" + tracking.getCarrierURL() + "', 'tracking')\">(track it)</a></div>";
              }
            }
          }
        }

        // -- Order Item Level Details --
        List orderDetailList = shipment.getItems();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            sLineItems[i] = new StringBuffer();
          //sLineItems[i].append("<tr><td colspan='8' class='text-normal'><b>Items Ordered:</b></td></tr>");
            sLineItems[i].append("<tr>");
            sLineItems[i].append("<td class='grid-rows-table' align='left' nowrap><b>Item #</b>&nbsp;&nbsp;</td>");
            sLineItems[i].append("<td class='grid-rows-table' align='left' wrap><b>Product</b></td>");
            sLineItems[i].append("<td class='grid-rows-table' align='center' nowrap><b>Qty</b></td>");
          //sLineItems[i].append("<td class='grid-rows-table' align='center' nowrap><b>Reg.<br>Price</b></td>");
            sLineItems[i].append("<td class='grid-rows-table' align='center' nowrap><b>Price<br>Each</b></td>");
            sLineItems[i].append("<td class='grid-rows-table' align='center' nowrap><b>Discount</b></td>");
            sLineItems[i].append("<td class='grid-rows-table' align='center' nowrap><b>Total<br>Price</b></td>");
            sLineItems[i].append("</tr>");
            IAmount discount = new Amount();

            for (Iterator ii = orderDetailList.iterator(); ii.hasNext();) {
                IOrderItem orderItem = (IOrderItem) ii.next();
                sLineItems[i].append("<tr OnMouseOver=\"this.style.backgroundColor='#e5e5d1'\" OnMouseOut=\"this.style.backgroundColor='#FFFFFF'\" BGCOLOR='#FFFFFF'>");

                if (orderItem.getSku() != null) {
                    sLineItems[i].append("<td class='text-normal' nowrap>" + orderItem.getSku() + "</td>");
                } else  {
                    sLineItems[i].append("<td class='text-normal'align=left>N/A</td>");
                }
				

                StringBuffer discountMsg = new StringBuffer("");
				StringBuffer discountAmount  = new StringBuffer("");
				OrderUtilities.getDiscountText(orderItem, true, locale, currency, discountAmount, discountMsg);
                if (orderItem.getProduct() != null) {
                    sLineItems[i].append(" <td class='text-normal'><div>" + OrderUtilities.checkNull(orderItem.getProductName()));
                    sLineItems[i].append("</div>");
					if (orderItem.getGiftWrapping() != null) {
						if (orderItem.getGiftWrapping().getGiftWrap() != null) {
							sLineItems[i].append("<div><b>Gift Wrap: </b>"+OrderUtilities.checkNull(orderItem.getGiftWrapping().getGiftWrap().getName())+"</div>");
						}
					}
                    if ( orderItem.getProduct().hasActiveKits() ) {
                      for (Iterator kitParts = orderItem.getKitParts().iterator() ; kitParts.hasNext(); ) {
                        IOrderItemPart kitPart = (IOrderItemPart) kitParts.next();
                          if ( kitPart.getDisplayTypeId() != 3 ) {
                            sLineItems[i].append("<div>");
                            sLineItems[i].append(OrderUtilities.checkNull(kitPart.getProductName()));
                            sLineItems[i].append(" ");
                            if ( kitPart.getSelectedOptions().length() > 0 ) {
                              sLineItems[i].append("(" + OrderUtilities.getLocalizedOptions(kitPart, locale) + ")");
                            }
                            if ( kitPart.getQty() > 1 ) {
                              sLineItems[i].append("(Qty " + kitPart.getQty() + ")");
                            }
                            sLineItems[i].append("</div>");
                          } // End if - Checking for display type id = 3, Should Not Display Hidden Element
                      }
                    }
                    if (orderItem.getSnapshot().getOptionNames() != null) {
                      sLineItems[i].append("(" + orderItem.getSnapshot().getOptionNames() + ")");
                    }
                    sLineItems[i].append("</div>");

/*                   // Order Status
                    if (orderItem.getStatus() != null && ! "".equals(orderItem.getStatus())) {
                      sLineItems[i].append("<div class=text-bold><div class=text-highlight>" + orderItem.getStatus()+ "</div></div>");
                    }
*/
					// Discount msg
					if(discountMsg.length()>0){
						sLineItems[i].append("<div class='itemLevelDiscountMsg'>Item-level discount message</div>");
						sLineItems[i].append("<div class='itemLevelDiscountMsg'>"+discountMsg+"</div>");
					}
					
                    // Order Tracking
                    if (!orderItem.getTrackings().isEmpty()) {
                      List trackings = orderItem.getTrackings();
                      for (int trackingIdx=0; trackingIdx<trackings.size(); trackingIdx++) {
                        IOrderTracking tracking = (IOrderTracking) trackings.get(trackingIdx);
                        sLineItems[i].append("<div class=text-normal>" + tracking.getCarrierName());
                        if (tracking.getTrackingNumber() != null && tracking.getTrackingNumber().length() > 0) {
                          sLineItems[i].append(" - " + tracking.getTrackingNumber());
                          if (tracking.getCarrierURL() != null && tracking.getCarrierURL().length() > 0) {
                            sLineItems[i].append("&nbsp<a href=\"#\" onClick=\"return flyopen(" + shipmentCarrierWxH + ", '" + tracking.getCarrierURL() + "', 'tracking')\">(track it)</a></div>");
                          }
                        }
                      }
                    }
                } else {
                    sLineItems[i].append("<td class='text-normal'align=left>N/A");
                }

                sLineItems[i].append("</td>");

                if (orderItem.getProduct() != null) {
                    sLineItems[i].append("<td class='text-normal'align='center'>" + orderItem.getQty() + "</td>");
                } else {
                    sLineItems[i].append("<td class='text-normal'align='center'>N/A</td>");
                }

                IAmount extendedPrice = new Amount(orderItem.getSellPrice());
                extendedPrice.multiply(orderItem.getQty());
                discount.add(extendedPrice);
/*
                if (orderItem.getRegularPrice() != null) {
                    sLineItems[i].append("<td  class='text-normal'align=right>" + (!orderItem.isFreeGift() ? AmountFormat.format(orderItem.getRegularPrice(), locale, currency) : "FREE") + "</td>");
                } else {
                    sLineItems[i].append("<td  class='text-normal'align=center>N/A</td>");
                }
*/

                String sellPrice=!orderItem.isFreeGift() ? AmountFormat.format(orderItem.getSellPrice(), locale, currency) : "FREE";
               	String regularPrice=!orderItem.isFreeGift() ? AmountFormat.format(orderItem.getRegularPrice(), locale, currency) : "FREE";		

                
               	sLineItems[i].append("<td  class='text-normal'align='center'><div>" + sellPrice + "</div>");
                if(!sellPrice.equals(regularPrice)){
                	 sLineItems[i].append("<strike>"+regularPrice+"</strike>");
                }
                sLineItems[i].append("</td>");
                
				sLineItems[i].append("<td class='text-normal'align=center><font color='red'>" + discountAmount + "</font></td>");
                sLineItems[i].append("<td  class='text-normal'align='center'>" + (!orderItem.isFreeGift() ? AmountFormat.format(orderItem.getSubTotal(), locale, currency) : "FREE") + "</td>");
                sLineItems[i].append("<!-- td class='text-normal' colspan='2'><b>" + orderItem.getStatus() + "</b></td -->");
                sLineItems[i].append("</tr>");
            }

            // Only display Shipment Totals if more than one shipment
            if (shipmentCount > 1) {
                // -- Add Shipment Merchandise Subtotal --
                if (shipment.getSubTotal() != null) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'><br>Merchandise Subtotal&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'><br>" + AmountFormat.format(shipment.getSubTotal(), locale, currency) +
                            "</td><td colspan='2'>&nbsp;</td></tr>");
                }

                // -- Add Shipment Shipping Total --
                if (shipment.getShippingTotal() != null) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'>Shipping&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getShippingTotal(), locale, currency) +
                            "</td><td colspan='2'>&nbsp;</td></tr>");
                }

                List orderShipmentDiscounts = orderManager.findOrderShipmentDiscounts(shipment.getPk());
                if (orderShipmentDiscounts != null) {
	                Iterator iter = orderShipmentDiscounts.iterator();
	                while (iter.hasNext()) {
	                  IOrderDiscount orderShipmentDiscount = (IOrderDiscount) iter.next();
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'>" +
                        orderShipmentDiscount.getMessage() + "&nbsp;" + "</td><td align='right' class='text-normal'>("
                        +  AmountFormat.format(orderShipmentDiscount.getAmount(), locale, currency) + ")</td></tr>");
                  }
                }

                // -- Add Shipment Weight Surcharge (Oversized Shipping) Total --
                if (shipment.getWeightSurchargeTotal() != null && shipment.getWeightSurchargeTotal().compareTo(Amount.ZERO) > 0) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'>Oversized Shipping&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getWeightSurchargeTotal(), locale, currency) +
                            "</td><td colspan='2'>&nbsp;</td></tr>");
                }

                // -- Add Shipment Gift Wrap Total --
                if (shipment.getGiftWrapTotal() != null && shipment.getGiftWrapTotal().compareTo(Amount.ZERO) > 0) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'>Gift Wrap&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getGiftWrapTotal(), locale, currency) +
                            "</td><td colspan='2'>&nbsp;</td></tr>");
                }

                // -- Add Shipment Tax Total --
                if (shipment.getTaxTotal() != null) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'>Tax&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getTaxTotal(), locale, currency) +
                            "</td><td colspan='2'>&nbsp;</td></tr>");
                }

                sLineItems[i].append("<tr><td colspan='4' class='text-normal'>&nbsp;</td><td colspan='2'><hr></td></tr>");

                // -- Add Shipment Total --
                if (shipment.getTotal() != null) {
                    sLineItems[i].append("<tr><td colspan='5' align='right' class='text-normal'><b>Total</b>&nbsp;&nbsp;" +
                            "</td><td align='right' class='text-normal'><b>" + AmountFormat.format(shipment.getTotal(), locale, currency) +
                            "</b></td><td colspan='2'>&nbsp;</td></tr>");
                }
            }
        }
    }

    // -- Build Order Totals --
    sOrderTotals = new StringBuffer();
    IAmount discountTotal = new Amount();
    StringBuffer sOrderDiscountMsg = new StringBuffer();
    boolean showItemizedDiscounts = false;
    if(discounts != null)
    {
        showItemizedDiscounts = true;
        Iterator disIter = discounts.iterator();
        IOrderDiscount discount;
        while(disIter.hasNext())
        {
            discount = (IOrderDiscount)disIter.next();
            IAmount amount = new Amount(discount.getAmount());
          String msg = discount.getMessage();
          if (msg == null) {
            msg = "";
          }

            sOrderDiscountMsg.append("<tr><td align='right' class='text-normal'>" + msg + "&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>(" + AmountFormat.format(amount, locale, currency) +
                ")</td><td>&nbsp;</td></tr>");

            discountTotal.add(amount);
        }
    }

    // -- Add Order Subtotal --
    if (orderReview.getSubTotal() != null) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Merchandise Total&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getMerchandiseTotal(), locale, currency) +
                "</td><td>&nbsp;</td></tr>");
    }

    // -- Add Order Discounts
    if( discountTotal.compareTo(Amount.ZERO) > 0)
    {
        sOrderTotals.append(sOrderDiscountMsg);

    }

    // -- Add Order Shipping Total --

    //for multiple shipments
    if(shipmentCount > 1) {
      if (orderReview.getSummarizedShippingTotal() != null) {
          sOrderTotals.append("<tr><td align='right' class='text-normal'>Shipping Total&nbsp;&nbsp;" +
                  "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getSummarizedShippingTotal(), locale, currency) +
                  "</td><td>&nbsp;</td></tr>");
      }
    }
    //for single shipments
    else {

        ICartShipment shipment = (ICartShipment)orderReview.getShipments().get(0);

        // -- Shipping Charge before discounts and surcharges
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Shipping&nbsp;&nbsp;" +
                      "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getShippingTotal(), locale, currency) +
                      "</td><td>&nbsp;</td></tr>");

        // -- Shipping discounts
        List shipmentDiscounts = orderManager.findOrderShipmentDiscounts(shipment.getPk());
        if(shipmentDiscounts != null) {
          Iterator iter = shipmentDiscounts.iterator();
          while(iter.hasNext()) {
			IOrderDiscount shipmentDiscount = (IOrderDiscount)iter.next();
            if(shipmentDiscount.getMessage() != null && shipmentDiscount.getMessage().length() > 0) {
              sOrderTotals.append("<tr><td align='right' class='text-normal'>" + shipmentDiscount.getMessage() + "&nbsp;&nbsp;" +
                      "</td><td align='right' class='text-normal'>(" + AmountFormat.format(shipmentDiscount.getAmount(), locale, currency) +
                      ")</td><td>&nbsp;</td></tr>");
            }else{
				sOrderTotals.append("<tr><td align='right' class='text-normal'> &nbsp;&nbsp; </td><td align='right' class='text-normal'>(" + AmountFormat.format(shipmentDiscount.getAmount(), locale, currency) +
                      ")</td><td>&nbsp;</td></tr>");
			}
          }
        }
        // Overweight surcharge
        if(shipment.getWeightSurchargeTotal() != null && shipment.getWeightSurchargeTotal().compareTo(Amount.ZERO) > 0) {
          sOrderTotals.append("<tr><td align='right' class='text-normal'>Oversized Shipping&nbsp;&nbsp;" +
                      "</td><td align='right' class='text-normal'>" + AmountFormat.format(shipment.getWeightSurchargeTotal(), locale, currency) +
                      "</td><td>&nbsp;</td></tr>");
        }
    }

    // -- Add Order Gift Wrap Total --
    if (orderReview.getGiftWrapTotal() != null && orderReview.getGiftWrapTotal().compareTo(Amount.ZERO) > 0) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Gift Wrap Total&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getGiftWrapTotal(), locale, currency) +
                "</td><td>&nbsp;</td></tr>");
    }



    // -- Add Order Tax Total --
    if (orderReview.getTaxTotal() != null) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Tax Total&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getTaxTotal(), locale, currency) +
                "</td><td>&nbsp;</td></tr>");
    }

    // -- Add Order Additional Address Total --
    if (orderReview.getAdditionalAddressTotal() != null && orderReview.getAdditionalAddressTotal().compareTo(Amount.ZERO) > 0) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Additional Address Total&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getAdditionalAddressTotal(), locale, currency) +
                "</td><td>&nbsp;</td></tr>");
    }

    sOrderTotals.append("<tr><td colspan='2'><hr></td><td>&nbsp;</td></tr>");

    // -- Add Order Additional Charges Total --
    if (orderReview.getAdditionalChargesTotal() != null && orderReview.getAdditionalChargesTotal().compareTo(Amount.ZERO) > 0) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'>Additional Charges Total&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'>" + AmountFormat.format(orderReview.getAdditionalChargesTotal(), locale, currency) +
                "</td><td>&nbsp;</td></tr>");
    }

    // -- Add Order Total --
    if (orderReview.getTotal() != null) {
        sOrderTotals.append("<tr><td align='right' class='text-normal'><b>Order Total</b>&nbsp;&nbsp;" +
                "</td><td align='right' class='text-normal'><b>" + AmountFormat.format(orderReview.getTotal(), locale, currency) +
                "</b></td><td>&nbsp;</td></tr>");
    }

%>
