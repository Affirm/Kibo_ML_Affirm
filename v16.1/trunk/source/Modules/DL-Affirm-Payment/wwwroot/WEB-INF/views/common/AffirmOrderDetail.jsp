<%@ include file="/WEB-INF/views/common/TagLibs.jsp" %>


<%----------------------------------------------------------------------------------------------------------------------
  -    Description: Order History_Order Detail
  -  Storyboard ID: ACC0090
  - Pre-conditions:
  -     Model/Form: com.marketlive.app.b2c.common.OrderDetailModel
  - URLs Posted To:
  -   Current Tile: .tile.account.OrderDetail
  - Tile Variables: payments, basket
  -  Child Tile(s):	orderShipment
  -      Copyright: (c) 2014 MarketLive, Inc. All Rights Reserved.
  --------------------------------------------------------------------------------------------------------------------%>

<%--
    //(C) Copyright MarketLive. 2014. All rights reserved.
    //MarketLive is a trademark of MarketLive, Inc.
    //Warning: This computer program is protected by copyright law and international treaties.
    //Unauthorized reproduction or distribution of this program, or any portion of it, may result
    //in severe civil and criminal penalties, and will be prosecuted to the maximum extent
    //possible under the law.
--%>

<tilesx:useAttribute id="payments" name="payments"/>
<tilesx:useAttribute id="basket" name="basket"/>
<c:set var="ZERO" value="${PricingModel.zero}" />
<c:set var="colspan7" value="5" />
<c:set var="colspan5" value="3" />
<c:if test="${OrderDetailModel.showDiscount}">
    <c:set var="colspan7" value="6" />
    <c:set var="colspan5" value="4" />
</c:if>

<%--  Billing Info--%>
	<div class="ml-order-detail-left-container">
          <c:choose>
          <c:when test="${OrderDetailModel.pastOrder}">
            <c:set var="billContact" value="${basket.billToInfo}" scope="page"/>
            <div class="order-headers"><fmt:message key="hdr.order.billingInfo" /></div>
          </c:when>
          <c:otherwise>
          	<c:set var="billContact" value="${accountRegisterForm.billContact}" scope="page"/>
            <div class="order-headers"><fmt:message key="hdr.order.billingInfo" /><a href="<c:url value="${mlnav:urlWrapper('/checkout/billing.do', pageContext)}" />"> <fmt:message key="msg.order.edit" /></a></div>
          </c:otherwise>
          </c:choose>
		<div><c:out value="${billContact.person.firstName}" /> <c:if test="${!empty billContact.person.middleName}">${billContact.person.middleName}.</c:if> <c:out value="${billContact.person.lastName}" /></div>
            <div><c:out value="${billContact.address.street1}" /></div>
            <div><c:out value="${billContact.address.street2}" /></div>
            <div><c:out value="${billContact.address.street3}" /></div>
            <div>
				  <c:choose>
		          	<c:when test="${(billContact.address.country.code=='US') || (billContact.address.country.code=='CA')}">
		                    <c:out value="${billContact.address.city}" />,
		                    <c:out value="${billContact.address.state.stateCode}" />
					</c:when>
				    <c:otherwise>
				      	<c:out value="${billContact.address.city}" /><c:if test="${!empty billContact.address.postalCode}">,</c:if>
					</c:otherwise>
				  </c:choose>
				  <mlpstcode:formatPostalCode value="${billContact.address.postalCode}"/>
            </div>
            <div>${billContact.address.country.code}</div>
            <div><mlphone:formatPhone value="${billContact.phone1}" countryCode="${billContact.address.country.code}"/></div>
            <div><mlphone:formatPhone value="${billContact.phone2}" countryCode="${billContact.address.country.code}"/></div>
            <div><c:out value="${billContact.email}" /></div>
	</div>

	<div class="ml-order-detail-right-container">
		<c:choose>
          <c:when test="${OrderDetailModel.pastOrder}">
            <c:set var="billContact" value="${basket.billToInfo}" scope="page"/>
            <div class="order-headers"><fmt:message key="hdr.order.paymentMethod" /></div>
          </c:when>
          <c:otherwise>
          	<c:set var="billContact" value="${accountRegisterForm.billContact}" scope="page"/>
            <div class="order-headers"><fmt:message key="hdr.order.paymentMethod" /><a href="<c:url value="${mlnav:urlWrapper('/checkout/payment.do', pageContext)}" />"> <fmt:message key="msg.order.edit" /></a></div>
          </c:otherwise>
          </c:choose>
<%--  Credit Card --%>
            <c:forEach var="payment" items="${payments}" >
              <div>
                  <c:choose>
                      <c:when test="${payment.description == 'CREDIT CARD' || payment.description == 'CARD PRESENT' || payment.description == 'CARD TOKEN'}">
                          <c:choose>
                              <c:when test="${payment.description == 'CARD TOKEN'}">
                                  ${payment.creditCardType}
                              </c:when>
                              <c:otherwise>
                                  ${payment.type}
                              </c:otherwise>
                          </c:choose>
                          ${payment.maskedNumber}
                          <fmt:message key="msg.common.for" /> <mlamt:formatAmount value="${payment.amount}" />
                      </c:when>
                      <c:when test="${payment.description == 'GIFT CERTIFICATE'}">
                          <fmt:message key="msg.checkout.submit.giftCertificate" />
                          ${payment.number}
                          <fmt:message key="msg.common.for" /> <mlamt:formatAmount value="${payment.amount}" />
                      </c:when>
                      <c:when test="${payment.description == 'AFFIRM'}">
		                  ${payment.description}
		                  <fmt:message key="msg.common.for" /> <mlamt:formatAmount value="${payment.amount}" />
		                  <c:set var="affirmPaymentCustomerDetail" value="${OrderDetailModel.attributeMap['affirmPaymentCustomerDetail']}" />
		                  <c:set var="affirmRequestId" value="${OrderDetailModel.attributeMap['affirmRequestId']}" />
		                  <c:set var="affirmPaymentPublicKey" value="${OrderDetailModel.attributeMap['affirmPaymentPublicKey']}" />
		                  <c:if test="${affirmRequestId != null and affirmPaymentCustomerDetail != null and affirmPaymentPublicKey != null}">
		                  	<p><a target="_blank" href="${affirmPaymentCustomerDetail}${affirmRequestId}?trk=${affirmPaymentPublicKey}">${affirmRequestId}</a></p>
		                  </c:if>   
                      </c:when>
                      <c:otherwise>
                          ${payment.description}
                          <fmt:message key="msg.common.for" /> <mlamt:formatAmount value="${payment.amount}" />
                      </c:otherwise>

                  </c:choose>

              </div>
            </c:forEach>

<%--  Source Codes Used for Order History --%>
<%--  Source Codes Used for Review Invoice --%>
            <tiles:insertDefinition name=".common.SourceCodeList" flush="false"><tiles:putAttribute name="rowClasses" value="tableitem" /></tiles:insertDefinition>
	</div>
	<div class="ml-horizontal-separator"></div>
	<div class="ml-order-detail-spacer"></div>

 

<%--  Order Shipment Info --%>
<%--  check if page is for My Account or Checkout --%>

<c:choose>
  <c:when test="${OrderDetailModel.pastOrder}">
    <c:set var="shipmentList" value="${accountRegisterForm.orderShipments}" />
    <c:set var="account" value="true" scope="request" />
  </c:when>
  <c:otherwise>
    <c:set var="shipmentList" value="${accountRegisterForm.basket.shipmentList}" />
  </c:otherwise>
</c:choose>


<div id="ml-no-tables">
<table class="table-bordered table-striped table-condensed ml-order-detail-order-content">
<caption class="ml-order-detail-sub-header">Order Contents</caption>
<c:set var="colspan" value="4" />
<c:if test="${OrderDetailModel.showDiscount}">
    <c:set var="colspan" value="5" />  
</c:if>
	
<%--  Item Table Headers --%>
  <thead>
  <tr>
		<th><fmt:message key="hdr.itemTable.itemNumber" /></th>
		<th><fmt:message key="hdr.itemTable.qty" /></th>
		<th class="text-right-align"><fmt:message key="hdr.itemTable.priceEach" /></th>
		<c:if test="${OrderDetailModel.showDiscount}">
		<th class="text-right-align"><fmt:message key="hdr.itemTable.discount" /></th>
		</c:if>
		<th class="col-xs-4 col-sm-3 col-md-2 text-right-align"><fmt:message key="hdr.itemTable.totalPrice" /></th>
	</tr>
	</thead>
	<c:set var="shipmentListCount" value="0" />
	<c:set var="pickupListCount" value="0" />
	<c:forEach var="shipment" varStatus="status" items="${shipmentList}">
			<c:if test="${!shipment.pickupFromStore}">
				<c:set var="shipmentListCount" value="${shipmentListCount + 1}" />
			</c:if>
			<c:if test="${shipment.pickupFromStore}">
				<c:set var="pickupListCount" value="${pickupListCount + 1}" />
			</c:if>
	</c:forEach>
	<tbody>
		<c:set var="shipmentCounter" value="0" />
		<c:forEach var="shipment" varStatus="status" items="${shipmentList}">
			<c:if test="${!shipment.pickupFromStore}">
			  <tiles:insertAttribute name="orderShipment" >
				<tiles:putAttribute name="index" value="${shipmentCounter}" />
				<tiles:putAttribute name="basket" value="${basket}" />
				<tiles:putAttribute name="shipment" value="${shipment}"/>
				<tiles:putAttribute name="payments" value="${payments}"/>
				<tiles:putAttribute name="shipmentListCount" value="${shipmentListCount}"/>
			  </tiles:insertAttribute>
			  <c:set var="shipmentCounter" value="${shipmentCounter + 1}" />
			</c:if>
		</c:forEach>
		<c:set var="pickupShipmentCounter" value="0" />
		<c:forEach var="shipment" varStatus="status" items="${shipmentList}">
			<c:if test="${shipment.pickupFromStore}">
			  <tiles:insertAttribute name="orderPickup" >
				<tiles:putAttribute name="index" value="${pickupShipmentCounter}" />
				<tiles:putAttribute name="basket" value="${basket}" />
				<tiles:putAttribute name="shipment" value="${shipment}"/>
				<tiles:putAttribute name="payments" value="${payments}"/>
				<tiles:putAttribute name="pickupListCount" value="${pickupListCount}"/>
			  </tiles:insertAttribute>
			  <c:set var="pickupShipmentCounter" value="${pickupShipmentCounter + 1}" />
			</c:if>
		</c:forEach>
	</tbody>

<%--  Order Totals (Used to display totals when Multiple Ship Tos have been assigned)--%>
<c:if test="${basket.multipleShipping}" >
  
<tr>
	<td colspan="${colspan7}" class="order-detail-no-padding">
		<%-- Merchandise Total --%>
		<div class="ml-horizontal-separator"></div>
		<div class="col-xs-8 col-sm-9 col-md-10 order-detail-bold-font text-right-align"><fmt:message key="msg.order.subtotal" /></div>
		<div class="col-xs-4 col-sm-3 col-md-2 order-detail-bold-font text-right-align"><mlamt:formatAmount value="${basket.merchandiseTotal}" /></div>
		<div class="ml-horizontal-separator"></div>
	<c:forEach var="orderDiscount" varStatus="status" items="${basket.discounts}">
		<div class="col-xs-8 col-sm-9 col-md-10 order-detail-discount-message text-right-align">
		<c:if test="${!empty orderDiscount.message}" >
			<c:out value="${orderDiscount.message}" escapeXml="false" />
		</c:if>
		</div>
		<div class="col-xs-4 col-sm-3 col-md-2 order-detail-discount-message text-right-align"> -<mlamt:formatAmount value="${orderDiscount.amount}"/></div>
	</c:forEach>
	
	<div class="col-xs-8 col-sm-9 col-md-10 text-right-align"><fmt:message key="msg.order.shippingTotal" /></div>
	<div class="col-xs-4 col-sm-3 col-md-2 text-right-align"><mlamt:formatAmount value='${basket.summarizedShippingTotal}' /></div>
  
	<c:if test="${basket.giftWrapTotal > ZERO}">
		<div class="col-xs-8 col-sm-9 col-md-10 text-right-align"><fmt:message key="msg.order.giftWrapTotal" /></div>
		<div class="col-xs-4 col-sm-3 col-md-2 text-right-align" id="orderGiftWrapTotal"><mlamt:formatAmount value="${basket.giftWrapTotal}" /></div>
	</c:if>
	
	<div class="col-xs-8 col-sm-9 col-md-10 text-right-align"><fmt:message key="msg.order.taxTotal" /></div>
	<div class="col-xs-4 col-sm-3 col-md-2 text-right-align"><mlamt:formatAmount value="${basket.taxTotal}" /></div>
  
	<c:if test="${basket.additionalChargesTotal > ZERO}">
		<div class="col-xs-8 col-sm-9 col-md-10 text-right-align"><fmt:message key="msg.order.additionalCharges" /></div>
		<div class="col-xs-4 col-sm-3 col-md-2 text-right-align"><mlamt:formatAmount value="${basket.additionalChargesTotal}" /></div>
	</c:if>
	
	<c:if test="${basket.additionalAddressTotal > ZERO}">
		<div class="col-xs-8 col-sm-9 col-md-10 text-right-align"><fmt:message key="msg.order.addressTotal" /></div>
		<div class="col-xs-4 col-sm-3 col-md-2 text-right-align"><mlamt:formatAmount value="${basket.additionalAddressTotal}" /></div>
	</c:if>
<%--  Gift Certificate(s) --%>
<c:forEach var="payment" items="${payments}" >
	<c:if test="${payment.description == 'GIFT CERTIFICATE'}">
		<c:if test="${payment.amount > ZERO}">
		<%-- Order Total --%>
			<div class="ml-horizontal-separator"></div>
			<div class="col-xs-8 col-sm-9 col-md-10 order-detail-total-font-size text-right-align"><fmt:message key="msg.order.orderTotal" /></div>
			<div class="col-xs-4 col-sm-3 col-md-2 order-detail-total-font-size text-right-align"><mlamt:formatAmount value="${basket.total}" /></div>
			<div class="ml-horizontal-separator"></div>
		
		<%-- Gift Certificate --%>
		<div class="col-xs-8 col-sm-9 col-md-10 order-detail-discount-message text-right-align"><fmt:message key="hdr.order.giftcertificate" /></div>
		<div class="col-xs-4 col-sm-3 col-md-2 order-detail-discount-message text-right-align">(<mlamt:formatAmount value="${payment.amount}" />)</div>
		</c:if>
	</c:if>
</c:forEach>

<%-- Total Charge --%>
	<c:forEach var="payment" items="${payments}" >
		<c:if test="${payment.description == 'CREDIT CARD' or payment.description == 'CARD TOKEN' or payment.description == 'PAYPAL' or payment.description == 'CASH'}">
			<div class="col-xs-8 col-sm-9 col-md-10 order-detail-total-font-size text-right-align"><fmt:message key="msg.order.totalCharge" /></div>
			<div class="col-xs-4 col-sm-3 col-md-2 order-detail-total-font-size text-right-align"><mlamt:formatAmount value="${payment.amount}" /></div>
		</c:if>
	</c:forEach>

<%-- You Saved --%>
<c:if test="${OrderDetailModel.showYouSaved}">
	<div class="col-xs-8 col-sm-9 col-md-10 order-detail-total-font-size text-right-align"><fmt:message key="msg.order.discount.youSaved" /></div>
	<div class="col-xs-4 col-sm-3 col-md-2 order-detail-total-font-size text-right-align"><mlamt:formatAmount value="${OrderDetailModel.discountTotal}" /></div>
</c:if>


</td>
</tr>
</c:if>
</table>
</div>